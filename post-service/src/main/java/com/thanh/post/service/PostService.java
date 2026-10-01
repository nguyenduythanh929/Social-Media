package com.thanh.post.service;

import com.thanh.post.dto.PageResponse;
import com.thanh.post.dto.event.PostCreatedEvent;
import com.thanh.post.dto.event.PostDeletedEvent;
import com.thanh.post.dto.request.PostRequest;
import com.thanh.post.dto.response.LikeResponse;
import com.thanh.post.dto.response.PostResponse;
import com.thanh.post.dto.response.UserProfileResponse;
import com.thanh.post.entity.Post;
import com.thanh.post.exception.AppException;
import com.thanh.post.exception.ErrorCode;
import com.thanh.post.mapper.PostMapper;
import com.thanh.post.repository.CommentRepository;
import com.thanh.post.repository.PostRepository;
import com.thanh.post.repository.httpclient.ProfileClient;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.thanh.post.service.ServiceSupport.getCurrentUserId;
import static com.thanh.post.service.ServiceSupport.optionalContent;
import static com.thanh.post.service.ServiceSupport.requireContent;
import static com.thanh.post.service.ServiceSupport.toPageable;
import static com.thanh.post.service.ServiceSupport.validateMediaUrls;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class PostService {
    private static final int MAX_POST_LENGTH = 5000;
    private static final Sort NEWEST_FIRST = Sort.by("createdDate").descending();

    PostRepository postRepository;
    CommentRepository commentRepository;
    MongoTemplate mongoTemplate;
    PostMapper postMapper;
    DateTimeFormatter dateTimeFormatter;
    ProfileClient profileClient;
    AuthorLookupService authorLookupService;
    KafkaTemplate<String, Object> kafkaTemplate;

    @NonFinal
    @Value("${app.file.download-prefix}")
    String mediaDownloadPrefix;

    public PostResponse createPost(PostRequest request){
        String userId = getCurrentUserId();
        List<String> mediaUrls = validateMediaUrls(request.getMediaUrls(), mediaDownloadPrefix);

        Post post = Post.builder()
                .content(resolveContent(request.getContent(), mediaUrls))
                .mediaUrls(mediaUrls)
                .userId(userId)
                .createdDate(Instant.now())
                .modifiedDate(Instant.now())
                .build();

        post = postRepository.save(post);

        // Publish event for search indexing
        kafkaTemplate.send("post-created", toIndexEvent(post));

        // Return the post in the same shape as feed items so the client can prepend it directly
        return toPostResponse(post, findAuthors(List.of(post)), userId);
    }

    public PostResponse updatePost(String postId, PostRequest request){
        String userId = getCurrentUserId();
        Post post = getOwnedPost(postId, userId);

        // Editing changes the text only; images stay as posted.
        // Update only content fields so concurrent likes/comment counts are not overwritten
        Post updated = mongoTemplate.findAndModify(
                byId(post.getId()),
                new Update()
                        .set("content", resolveContent(request.getContent(), post.getMediaUrls()))
                        .set("modifiedDate", Instant.now()),
                FindAndModifyOptions.options().returnNew(true),
                Post.class);

        if (updated == null) throw new AppException(ErrorCode.POST_NOT_FOUND);

        // Keep the search index in sync
        kafkaTemplate.send("post-updated", toIndexEvent(updated));

        return toPostResponse(updated, findAuthors(List.of(updated)), userId);
    }

    public void deletePost(String postId){
        String userId = getCurrentUserId();
        Post post = getOwnedPost(postId, userId);

        postRepository.deleteById(post.getId());
        commentRepository.deleteAllByPostId(post.getId());

        // Remove the post from the search index
        kafkaTemplate.send("post-deleted", PostDeletedEvent.builder().postId(post.getId()).build());

        log.info("User {} deleted post {}", userId, postId);
    }

    public LikeResponse likePost(String postId){
        return updateLike(postId, true);
    }

    public LikeResponse unlikePost(String postId){
        return updateLike(postId, false);
    }

    public PageResponse<PostResponse> getMyPosts(int page, int size){
        return getUserPosts(getCurrentUserId(), page, size);
    }

    public PageResponse<PostResponse> getUserPosts(String userId, int page, int size){
        Page<Post> pageData = postRepository.findAllByUserId(userId, toPageable(page, size, NEWEST_FIRST));

        return toPageResponse(pageData);
    }

    // Fan-out on read: the feed is built at request time from the people the user follows plus the user's own posts
    public PageResponse<PostResponse> getFeed(int page, int size){
        String userId = getCurrentUserId();

        Set<String> authorIds = new LinkedHashSet<>();
        authorIds.add(userId);

        try {
            List<String> followingIds = profileClient.getFollowingIds(userId).getResult();
            if (followingIds != null) authorIds.addAll(followingIds);
        } catch (Exception e) {
            // Degrade to the user's own posts rather than failing the whole feed
            log.error("Error while getting following ids for user {}", userId, e);
        }

        Page<Post> pageData = postRepository.findAllByUserIdIn(authorIds, toPageable(page, size, NEWEST_FIRST));

        return toPageResponse(pageData);
    }

    // $addToSet / $pull are idempotent and atomic: double clicks or concurrent likes never duplicate or lose a like
    private LikeResponse updateLike(String postId, boolean like){
        String userId = getCurrentUserId();

        Update update = like ? new Update().addToSet("likedBy", userId) : new Update().pull("likedBy", userId);

        Post post = mongoTemplate.findAndModify(
                byId(postId), update, FindAndModifyOptions.options().returnNew(true), Post.class);

        if (post == null) throw new AppException(ErrorCode.POST_NOT_FOUND);

        Set<String> likedBy = Objects.requireNonNullElse(post.getLikedBy(), Set.of());

        return LikeResponse.builder()
                .postId(post.getId())
                .likeCount(likedBy.size())
                .likedByMe(likedBy.contains(userId))
                .build();
    }

    // A post needs text, images, or both
    private String resolveContent(String content, List<String> mediaUrls){
        boolean hasMedia = mediaUrls != null && !mediaUrls.isEmpty();

        return hasMedia ? optionalContent(content, MAX_POST_LENGTH) : requireContent(content, MAX_POST_LENGTH);
    }

    private Post getOwnedPost(String postId, String userId){
        Post post = postRepository.findById(postId).orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));

        if (!userId.equals(post.getUserId())) throw new AppException(ErrorCode.UNAUTHORIZED);

        return post;
    }

    // Criteria on the "id" property (not "_id") so the id is converted exactly as the repository does
    private Query byId(String postId){
        return Query.query(Criteria.where("id").is(postId));
    }

    private PostCreatedEvent toIndexEvent(Post post){
        return PostCreatedEvent.builder()
                .postId(post.getId())
                .userId(post.getUserId())
                .content(post.getContent())
                .createdDate(post.getCreatedDate())
                .build();
    }

    private PageResponse<PostResponse> toPageResponse(Page<Post> pageData){
        String currentUserId = getCurrentUserId();
        Map<String, UserProfileResponse> authors = findAuthors(pageData.getContent());

        var postList = pageData.getContent().stream()
                .map(post -> toPostResponse(post, authors, currentUserId))
                .toList();

        return PageResponse.<PostResponse>builder()
                .currentPage(pageData.getNumber() + 1)
                .pageSize(pageData.getSize())
                .totalPages(pageData.getTotalPages())
                .totalElements(pageData.getTotalElements())
                .data(postList)
                .build();
    }

    private PostResponse toPostResponse(Post post, Map<String, UserProfileResponse> authors, String currentUserId){
        var postResponse = postMapper.toPostResponse(post);
        postResponse.setCreated(dateTimeFormatter.format(post.getCreatedDate()));

        UserProfileResponse author = authors.get(post.getUserId());
        if (author != null) {
            postResponse.setUsername(author.getUsername());
            postResponse.setAvatar(author.getAvatar());
        }

        Set<String> likedBy = Objects.requireNonNullElse(post.getLikedBy(), Set.of());
        postResponse.setLikeCount(likedBy.size());
        postResponse.setLikedByMe(likedBy.contains(currentUserId));
        postResponse.setCommentCount(Objects.requireNonNullElse(post.getCommentCount(), 0L));
        postResponse.setMediaUrls(Objects.requireNonNullElse(post.getMediaUrls(), List.of()));
        postResponse.setOwnedByMe(currentUserId.equals(post.getUserId()));
        // createdDate and modifiedDate are set a few nanoseconds apart on creation
        postResponse.setEdited(post.getModifiedDate() != null
                && post.getModifiedDate().isAfter(post.getCreatedDate().plusSeconds(1)));

        return postResponse;
    }

    private Map<String, UserProfileResponse> findAuthors(List<Post> posts){
        return authorLookupService.findAuthors(posts.stream().map(Post::getUserId).toList());
    }
}

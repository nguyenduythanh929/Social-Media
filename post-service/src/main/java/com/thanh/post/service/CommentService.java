package com.thanh.post.service;

import com.thanh.post.dto.PageResponse;
import com.thanh.post.dto.request.CommentRequest;
import com.thanh.post.dto.response.CommentResponse;
import com.thanh.post.dto.response.UserProfileResponse;
import com.thanh.post.entity.Comment;
import com.thanh.post.entity.Post;
import com.thanh.post.exception.AppException;
import com.thanh.post.exception.ErrorCode;
import com.thanh.post.repository.CommentRepository;
import com.thanh.post.repository.PostRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static com.thanh.post.service.ServiceSupport.getCurrentUserId;
import static com.thanh.post.service.ServiceSupport.requireContent;
import static com.thanh.post.service.ServiceSupport.toPageable;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CommentService {
    private static final int MAX_COMMENT_LENGTH = 1000;
    // Oldest first, so a conversation reads top to bottom
    private static final Sort OLDEST_FIRST = Sort.by("createdDate").ascending();

    CommentRepository commentRepository;
    PostRepository postRepository;
    MongoTemplate mongoTemplate;
    DateTimeFormatter dateTimeFormatter;
    AuthorLookupService authorLookupService;

    public CommentResponse createComment(String postId, CommentRequest request){
        String userId = getCurrentUserId();
        String content = requireContent(request.getContent(), MAX_COMMENT_LENGTH);

        Post post = postRepository.findById(postId).orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));

        Comment comment = commentRepository.save(Comment.builder()
                .postId(post.getId())
                .userId(userId)
                .content(content)
                .createdDate(Instant.now())
                .build());

        changeCommentCount(post.getId(), 1);

        return toCommentResponse(comment, authorLookupService.findAuthors(List.of(userId)), userId);
    }

    public PageResponse<CommentResponse> getComments(String postId, int page, int size){
        String userId = getCurrentUserId();

        Page<Comment> pageData = commentRepository.findAllByPostId(postId, toPageable(page, size, OLDEST_FIRST));

        Map<String, UserProfileResponse> authors = authorLookupService.findAuthors(
                pageData.getContent().stream().map(Comment::getUserId).toList());

        var comments = pageData.getContent().stream()
                .map(comment -> toCommentResponse(comment, authors, userId))
                .toList();

        return PageResponse.<CommentResponse>builder()
                .currentPage(pageData.getNumber() + 1)
                .pageSize(pageData.getSize())
                .totalPages(pageData.getTotalPages())
                .totalElements(pageData.getTotalElements())
                .data(comments)
                .build();
    }

    // The comment's author or the post's owner may delete a comment
    public void deleteComment(String commentId){
        String userId = getCurrentUserId();

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND));

        boolean isCommentAuthor = userId.equals(comment.getUserId());
        boolean isPostOwner = postRepository.findById(comment.getPostId())
                .map(post -> userId.equals(post.getUserId()))
                .orElse(false);

        if (!isCommentAuthor && !isPostOwner) throw new AppException(ErrorCode.UNAUTHORIZED);

        commentRepository.deleteById(comment.getId());
        changeCommentCount(comment.getPostId(), -1);
    }

    // $inc keeps the counter correct under concurrent comments
    private void changeCommentCount(String postId, int delta){
        mongoTemplate.updateFirst(
                Query.query(Criteria.where("id").is(postId)), new Update().inc("commentCount", delta), Post.class);
    }

    private CommentResponse toCommentResponse(
            Comment comment, Map<String, UserProfileResponse> authors, String currentUserId){
        UserProfileResponse author = authors.get(comment.getUserId());

        return CommentResponse.builder()
                .id(comment.getId())
                .postId(comment.getPostId())
                .userId(comment.getUserId())
                .username(author != null ? author.getUsername() : null)
                .avatar(author != null ? author.getAvatar() : null)
                .content(comment.getContent())
                .created(dateTimeFormatter.format(comment.getCreatedDate()))
                .createdDate(comment.getCreatedDate())
                .ownedByMe(currentUserId.equals(comment.getUserId()))
                .build();
    }
}

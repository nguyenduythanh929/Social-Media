package com.thanh.post.controller;

import com.thanh.post.dto.ApiResponse;
import com.thanh.post.dto.PageResponse;
import com.thanh.post.dto.request.PostRequest;
import com.thanh.post.dto.response.LikeResponse;
import com.thanh.post.dto.response.PostResponse;
import com.thanh.post.service.PostService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PostController {
    PostService postService;

    @PostMapping("/create")
    ApiResponse<PostResponse> createPost(@RequestBody PostRequest request){
        return ApiResponse.<PostResponse>builder()
                .result(postService.createPost(request))
                .build();
    }

    @GetMapping("/my-posts")
    ApiResponse<PageResponse<PostResponse>> myPosts(
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size
    ){
        return ApiResponse.<PageResponse<PostResponse>>builder()
                .result(postService.getMyPosts(page, size))
                .build();
    }

    @GetMapping("/feed")
    ApiResponse<PageResponse<PostResponse>> feed(
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size
    ){
        return ApiResponse.<PageResponse<PostResponse>>builder()
                .result(postService.getFeed(page, size))
                .build();
    }

    @GetMapping("/users/{userId}")
    ApiResponse<PageResponse<PostResponse>> userPosts(
            @PathVariable String userId,
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size
    ){
        return ApiResponse.<PageResponse<PostResponse>>builder()
                .result(postService.getUserPosts(userId, page, size))
                .build();
    }

    @PutMapping("/{postId}")
    ApiResponse<PostResponse> updatePost(@PathVariable String postId, @RequestBody PostRequest request){
        return ApiResponse.<PostResponse>builder()
                .result(postService.updatePost(postId, request))
                .build();
    }

    @DeleteMapping("/{postId}")
    ApiResponse<Void> deletePost(@PathVariable String postId){
        postService.deletePost(postId);
        return ApiResponse.<Void>builder()
                .message("Post has been deleted")
                .build();
    }

    @PostMapping("/{postId}/like")
    ApiResponse<LikeResponse> likePost(@PathVariable String postId){
        return ApiResponse.<LikeResponse>builder()
                .result(postService.likePost(postId))
                .build();
    }

    @DeleteMapping("/{postId}/like")
    ApiResponse<LikeResponse> unlikePost(@PathVariable String postId){
        return ApiResponse.<LikeResponse>builder()
                .result(postService.unlikePost(postId))
                .build();
    }
}

package com.thanh.post.controller;

import com.thanh.post.dto.ApiResponse;
import com.thanh.post.dto.PageResponse;
import com.thanh.post.dto.request.CommentRequest;
import com.thanh.post.dto.response.CommentResponse;
import com.thanh.post.service.CommentService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CommentController {
    CommentService commentService;

    @GetMapping("/{postId}/comments")
    ApiResponse<PageResponse<CommentResponse>> getComments(
            @PathVariable String postId,
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "size", required = false, defaultValue = "20") int size
    ){
        return ApiResponse.<PageResponse<CommentResponse>>builder()
                .result(commentService.getComments(postId, page, size))
                .build();
    }

    @PostMapping("/{postId}/comments")
    ApiResponse<CommentResponse> createComment(@PathVariable String postId, @RequestBody CommentRequest request){
        return ApiResponse.<CommentResponse>builder()
                .result(commentService.createComment(postId, request))
                .build();
    }

    @DeleteMapping("/comments/{commentId}")
    ApiResponse<Void> deleteComment(@PathVariable String commentId){
        commentService.deleteComment(commentId);
        return ApiResponse.<Void>builder()
                .message("Comment has been deleted")
                .build();
    }
}

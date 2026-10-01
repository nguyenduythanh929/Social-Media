package com.thanh.profileservice.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.thanh.profileservice.dto.ApiResponse;
import com.thanh.profileservice.dto.response.UserProfileDetailResponse;
import com.thanh.profileservice.dto.response.UserProfileResponse;
import com.thanh.profileservice.service.FollowService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

// All paths stay under /users/** so the API gateway's existing profile route covers them
@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FollowController {
    FollowService followService;

    @GetMapping("/users/{userId}/detail")
    ApiResponse<UserProfileDetailResponse> getProfileDetail(@PathVariable String userId) {
        return ApiResponse.<UserProfileDetailResponse>builder()
                .result(followService.getProfileDetail(userId))
                .build();
    }

    @PostMapping("/users/{userId}/follow")
    ApiResponse<UserProfileDetailResponse> follow(@PathVariable String userId) {
        return ApiResponse.<UserProfileDetailResponse>builder()
                .result(followService.follow(userId))
                .build();
    }

    @DeleteMapping("/users/{userId}/follow")
    ApiResponse<UserProfileDetailResponse> unfollow(@PathVariable String userId) {
        return ApiResponse.<UserProfileDetailResponse>builder()
                .result(followService.unfollow(userId))
                .build();
    }

    @GetMapping("/users/{userId}/followers")
    ApiResponse<List<UserProfileResponse>> getFollowers(@PathVariable String userId) {
        return ApiResponse.<List<UserProfileResponse>>builder()
                .result(followService.getFollowers(userId))
                .build();
    }

    @GetMapping("/users/{userId}/following")
    ApiResponse<List<UserProfileResponse>> getFollowing(@PathVariable String userId) {
        return ApiResponse.<List<UserProfileResponse>>builder()
                .result(followService.getFollowing(userId))
                .build();
    }

    @GetMapping("/users/{userId}/friends")
    ApiResponse<List<UserProfileResponse>> getFriends(@PathVariable String userId) {
        return ApiResponse.<List<UserProfileResponse>>builder()
                .result(followService.getFriends(userId))
                .build();
    }
}

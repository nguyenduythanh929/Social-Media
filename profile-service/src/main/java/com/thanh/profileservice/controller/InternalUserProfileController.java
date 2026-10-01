package com.thanh.profileservice.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.thanh.profileservice.dto.ApiResponse;
import com.thanh.profileservice.dto.request.ProfileCreationRequest;
import com.thanh.profileservice.dto.response.UserProfileResponse;
import com.thanh.profileservice.service.FollowService;
import com.thanh.profileservice.service.UserProfileService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InternalUserProfileController {
    UserProfileService userProfileService;
    FollowService followService;

    @PostMapping("/internal/users")
    ApiResponse<UserProfileResponse> createProfile(@RequestBody ProfileCreationRequest request) {
        return ApiResponse.<UserProfileResponse>builder()
                .result(userProfileService.createProfile(request))
                .build();
    }

    @GetMapping("/internal/users/{userId}")
    ApiResponse<UserProfileResponse> getProfile(@PathVariable String userId) {
        return ApiResponse.<UserProfileResponse>builder()
                .result(userProfileService.getByUserId(userId))
                .build();
    }

    @PostMapping("/internal/users/batch")
    ApiResponse<List<UserProfileResponse>> getProfiles(@RequestBody List<String> userIds) {
        return ApiResponse.<List<UserProfileResponse>>builder()
                .result(userProfileService.getByUserIds(userIds))
                .build();
    }

    @GetMapping("/internal/users/{userId}/following-ids")
    ApiResponse<List<String>> getFollowingIds(@PathVariable String userId) {
        return ApiResponse.<List<String>>builder()
                .result(followService.getFollowingIds(userId))
                .build();
    }
}

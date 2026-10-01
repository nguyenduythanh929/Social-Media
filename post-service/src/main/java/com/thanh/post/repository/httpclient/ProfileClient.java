package com.thanh.post.repository.httpclient;

import com.thanh.post.dto.ApiResponse;
import com.thanh.post.dto.response.UserProfileResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "profile-service", url = "${app.services.profile.url}")
public interface ProfileClient {
    @GetMapping("/internal/users/{userId}")
    ApiResponse<UserProfileResponse> getProfile(@PathVariable String userId);

    @PostMapping("/internal/users/batch")
    ApiResponse<List<UserProfileResponse>> getProfiles(@RequestBody List<String> userIds);

    @GetMapping("/internal/users/{userId}/following-ids")
    ApiResponse<List<String>> getFollowingIds(@PathVariable String userId);
}

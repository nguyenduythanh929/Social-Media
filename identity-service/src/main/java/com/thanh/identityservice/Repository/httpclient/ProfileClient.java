package com.thanh.identityservice.Repository.httpclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.thanh.identityservice.DTO.ApiResponse;
import com.thanh.identityservice.DTO.Request.ProfileCreationRequest;
import com.thanh.identityservice.DTO.Response.UserProfileResponse;
import com.thanh.identityservice.configuration.AuthenticationRequestInterceptor;

@FeignClient(
        name = "profile-service",
        url = "${app.services.profile}",
        configuration = {AuthenticationRequestInterceptor.class})
public interface ProfileClient {
    @PostMapping(value = "/internal/users", produces = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<UserProfileResponse> createProfile(@RequestBody ProfileCreationRequest request);
}

package com.thanh.search.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.thanh.search.document.PostDocument;
import com.thanh.search.document.UserDocument;
import com.thanh.search.dto.ApiResponse;
import com.thanh.search.service.SearchService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SearchController {

    SearchService searchService;

    @GetMapping("/users")
    ApiResponse<List<UserDocument>> searchUsers(
            @RequestParam(value = "q") String keyword) {
        return ApiResponse.<List<UserDocument>>builder()
                .result(searchService.searchUsers(keyword))
                .build();
    }

    @GetMapping("/posts")
    ApiResponse<List<PostDocument>> searchPosts(
            @RequestParam(value = "q") String keyword) {
        return ApiResponse.<List<PostDocument>>builder()
                .result(searchService.searchPosts(keyword))
                .build();
    }
}

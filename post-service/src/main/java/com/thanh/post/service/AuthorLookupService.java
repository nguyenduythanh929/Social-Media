package com.thanh.post.service;

import com.thanh.post.dto.response.UserProfileResponse;
import com.thanh.post.repository.httpclient.ProfileClient;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

// Resolves author usernames/avatars for posts and comments with one batch call to profile-service
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AuthorLookupService {
    ProfileClient profileClient;

    public Map<String, UserProfileResponse> findAuthors(Collection<String> userIds){
        List<String> distinctIds = userIds.stream().filter(Objects::nonNull).distinct().toList();
        if (distinctIds.isEmpty()) return Map.of();

        try {
            List<UserProfileResponse> profiles = profileClient.getProfiles(distinctIds).getResult();
            if (profiles == null) return Map.of();

            return profiles.stream()
                    .filter(profile -> Objects.nonNull(profile.getUserId()))
                    .collect(Collectors.toMap(UserProfileResponse::getUserId, Function.identity(), (a, b) -> a));
        } catch (Exception e) {
            // Missing author info should not break the listing
            log.error("Error while getting author profiles", e);
            return Map.of();
        }
    }
}

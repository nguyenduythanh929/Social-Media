package com.thanh.profileservice.service;

import java.util.List;
import java.util.Objects;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.thanh.profileservice.dto.response.UserProfileDetailResponse;
import com.thanh.profileservice.dto.response.UserProfileResponse;
import com.thanh.profileservice.entity.UserProfile;
import com.thanh.profileservice.exception.AppException;
import com.thanh.profileservice.exception.ErrorCode;
import com.thanh.profileservice.mapper.UserProfileMapper;
import com.thanh.profileservice.repository.UserProfileRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class FollowService {
    UserProfileRepository userProfileRepository;
    UserProfileMapper userProfileMapper;

    public UserProfileDetailResponse getProfileDetail(String userId) {
        String currentUserId = getCurrentUserId();

        UserProfile profile = userProfileRepository
                .findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        boolean me = currentUserId.equals(userId);

        return UserProfileDetailResponse.builder()
                .profile(userProfileMapper.toUserProfileResponse(profile))
                .followersCount(userProfileRepository.countFollowers(userId))
                .followingCount(userProfileRepository.countFollowing(userId))
                .followedByMe(!me && userProfileRepository.countFollowRelation(currentUserId, userId) > 0)
                .me(me)
                .build();
    }

    public UserProfileDetailResponse follow(String userId) {
        String currentUserId = getCurrentUserId();

        if (currentUserId.equals(userId)) throw new AppException(ErrorCode.CANNOT_FOLLOW_YOURSELF);

        // MERGE is idempotent; 0 means one of the two profiles does not exist
        Long created = userProfileRepository.follow(currentUserId, userId);
        if (created == null || created == 0) throw new AppException(ErrorCode.USER_NOT_EXISTED);

        log.info("User {} followed {}", currentUserId, userId);

        return getProfileDetail(userId);
    }

    public UserProfileDetailResponse unfollow(String userId) {
        String currentUserId = getCurrentUserId();

        if (!userProfileRepository.existsByUserId(userId)) throw new AppException(ErrorCode.USER_NOT_EXISTED);

        userProfileRepository.unfollow(currentUserId, userId);

        log.info("User {} unfollowed {}", currentUserId, userId);

        return getProfileDetail(userId);
    }

    public List<UserProfileResponse> getFollowers(String userId) {
        return userProfileRepository.findFollowers(userId).stream()
                .map(userProfileMapper::toUserProfileResponse)
                .toList();
    }

    public List<UserProfileResponse> getFollowing(String userId) {
        return userProfileRepository.findFollowing(userId).stream()
                .map(userProfileMapper::toUserProfileResponse)
                .toList();
    }

    public List<UserProfileResponse> getFriends(String userId) {
        return userProfileRepository.findFriends(userId).stream()
                .map(userProfileMapper::toUserProfileResponse)
                .toList();
    }

    public List<String> getFollowingIds(String userId) {
        return userProfileRepository.findFollowing(userId).stream()
                .map(UserProfile::getUserId)
                .toList();
    }

    private String getCurrentUserId() {
        return Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication())
                .getName();
    }
}

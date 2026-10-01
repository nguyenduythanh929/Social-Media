package com.thanh.profileservice.service;

import java.util.List;
import java.util.Objects;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.thanh.profileservice.dto.event.ProfileUpdatedEvent;
import com.thanh.profileservice.dto.request.ProfileCreationRequest;
import com.thanh.profileservice.dto.request.SearchUserRequest;
import com.thanh.profileservice.dto.request.UpdateProfileRequest;
import com.thanh.profileservice.dto.response.UserProfileResponse;
import com.thanh.profileservice.entity.UserProfile;
import com.thanh.profileservice.exception.AppException;
import com.thanh.profileservice.exception.ErrorCode;
import com.thanh.profileservice.mapper.UserProfileMapper;
import com.thanh.profileservice.repository.UserProfileRepository;
import com.thanh.profileservice.repository.httpclient.FileClient;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserProfileService {
    UserProfileRepository userProfileRepository;
    UserProfileMapper userProfileMapper;
    FileClient fileClient;
    KafkaTemplate<String, Object> kafkaTemplate;

    public UserProfileResponse createProfile(ProfileCreationRequest request) {
        log.info("Username: {}", request.getUsername());
        UserProfile userProfile = userProfileMapper.toUserProfile(request);
        userProfile = userProfileRepository.save(userProfile);

        // Publish event for search indexing
        kafkaTemplate.send(
                "profile-updated",
                ProfileUpdatedEvent.builder()
                        .userId(userProfile.getUserId())
                        .username(userProfile.getUsername())
                        .firstName(userProfile.getFirstName())
                        .lastName(userProfile.getLastName())
                        .avatar(userProfile.getAvatar())
                        .city(userProfile.getCity())
                        .dob(userProfile.getDob())
                        .build());

        return userProfileMapper.toUserProfileResponse(userProfile);
    }

    public UserProfileResponse getProfile(String id) {
        UserProfile userProfile =
                userProfileRepository.findById(id).orElseThrow(() -> new RuntimeException("Profile not found"));

        return userProfileMapper.toUserProfileResponse(userProfile);
    }

    public UserProfileResponse getByUserId(String userId) {
        log.info("UserId : {}", userId);
        UserProfile userProfile = userProfileRepository
                .findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return userProfileMapper.toUserProfileResponse(userProfile);
    }

    public List<UserProfileResponse> getByUserIds(List<String> userIds) {
        if (userIds == null || userIds.isEmpty()) return List.of();

        return userProfileRepository.findAllByUserIdIn(userIds).stream()
                .map(userProfileMapper::toUserProfileResponse)
                .toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<UserProfileResponse> getAllProfiles() {
        var profiles = userProfileRepository.findAll();

        return profiles.stream().map(userProfileMapper::toUserProfileResponse).toList();
    }

    public UserProfileResponse getMyProfile() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        assert authentication != null;
        String userId = authentication.getName();

        log.info("Userid {}", userId);

        var profile = userProfileRepository
                .findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return userProfileMapper.toUserProfileResponse(profile);
    }

    public UserProfileResponse updateMyProfile(UpdateProfileRequest request) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        assert authentication != null;
        String userId = authentication.getName();

        var profile = userProfileRepository
                .findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        userProfileMapper.update(profile, request);

        UserProfileResponse updated = userProfileMapper.toUserProfileResponse(userProfileRepository.save(profile));

        // Publish event for search indexing
        kafkaTemplate.send(
                "profile-updated",
                ProfileUpdatedEvent.builder()
                        .userId(profile.getUserId())
                        .username(profile.getUsername())
                        .firstName(profile.getFirstName())
                        .lastName(profile.getLastName())
                        .avatar(profile.getAvatar())
                        .city(profile.getCity())
                        .dob(profile.getDob())
                        .build());

        return updated;
    }

    public UserProfileResponse updateAvatar(MultipartFile file) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        assert authentication != null;
        String userId = authentication.getName();

        var profile = userProfileRepository
                .findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        var response = fileClient.uploadMedia(file);

        profile.setAvatar(response.getResult().getUrl());

        return userProfileMapper.toUserProfileResponse(userProfileRepository.save(profile));
    }

    public List<UserProfileResponse> search(SearchUserRequest request) {
        var userId = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication())
                .getName();
        List<UserProfile> userProfiles = userProfileRepository.findAllByUsernameLike(request.getKeyword());
        return userProfiles.stream()
                .filter(userProfile -> !userId.equals(userProfile.getUserId()))
                .map(userProfileMapper::toUserProfileResponse)
                .toList();
    }
}

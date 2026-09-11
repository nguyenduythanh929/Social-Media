package com.thanh.profileservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import com.thanh.profileservice.dto.request.ProfileCreationRequest;
import com.thanh.profileservice.dto.request.UpdateProfileRequest;
import com.thanh.profileservice.dto.response.UserProfileResponse;
import com.thanh.profileservice.entity.UserProfile;

@Mapper(componentModel = "spring")
public interface UserProfileMapper {
    UserProfile toUserProfile(ProfileCreationRequest request);

    UserProfileResponse toUserProfileResponse(UserProfile entity);

    void update(@MappingTarget UserProfile entity, UpdateProfileRequest request);
}

package com.thanh.identityservice.Mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.thanh.identityservice.DTO.Request.UserCreationRequest;
import com.thanh.identityservice.DTO.Request.UserUpdateRequest;
import com.thanh.identityservice.DTO.Response.UserResponse;
import com.thanh.identityservice.Entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser(UserCreationRequest request);

    UserResponse toUserResponse(User user);

    @Mapping(target = "roles", ignore = true)
    void updateUser(@MappingTarget User user, UserUpdateRequest request);
}

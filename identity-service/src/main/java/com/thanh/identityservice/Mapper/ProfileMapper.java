package com.thanh.identityservice.Mapper;

import org.mapstruct.Mapper;

import com.thanh.identityservice.DTO.Request.ProfileCreationRequest;
import com.thanh.identityservice.DTO.Request.UserCreationRequest;

@Mapper(componentModel = "spring")
public interface ProfileMapper {
    ProfileCreationRequest toProfileCreationRequest(UserCreationRequest userCreationRequest);
}

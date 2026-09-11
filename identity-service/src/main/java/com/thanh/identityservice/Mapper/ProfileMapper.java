package com.thanh.identityservice.Mapper;

import com.thanh.identityservice.DTO.Request.ProfileCreationRequest;
import com.thanh.identityservice.DTO.Request.UserCreationRequest;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProfileMapper {
    ProfileCreationRequest toProfileCreationRequest(UserCreationRequest userCreationRequest);
}

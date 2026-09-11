package com.thanh.identityservice.Mapper;

import org.mapstruct.Mapper;

import com.thanh.identityservice.DTO.Request.PermissionRequest;
import com.thanh.identityservice.DTO.Response.PermissionResponse;
import com.thanh.identityservice.Entity.Permission;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    Permission toPermission(PermissionRequest request);

    PermissionResponse toPermissionResponse(Permission permission);
}

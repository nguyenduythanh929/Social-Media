package com.thanh.identityservice.Mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.thanh.identityservice.DTO.Request.RoleRequest;
import com.thanh.identityservice.DTO.Response.RoleResponse;
import com.thanh.identityservice.Entity.Role;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    @Mapping(target = "permissions", ignore = true)
    Role toRole(RoleRequest request);

    RoleResponse toRoleResponse(Role role);
}

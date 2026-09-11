package com.thanh.identityservice.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.thanh.identityservice.Entity.Permission;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, String> {}

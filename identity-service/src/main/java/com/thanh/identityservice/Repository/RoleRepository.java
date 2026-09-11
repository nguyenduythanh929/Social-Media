package com.thanh.identityservice.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.thanh.identityservice.Entity.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role, String> {}

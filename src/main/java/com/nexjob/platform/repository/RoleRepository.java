package com.nexjob.platform.repository;

import com.nexjob.platform.entity.Role;
import com.nexjob.platform.enums.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleName name);
}

package com.nexjob.platform.repository;

import com.nexjob.platform.entity.User;
import com.nexjob.platform.enums.RoleName;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    Page<User> findByRole_Name(RoleName roleName, Pageable pageable);
    Page<User> findByRole_NameAndEmailContainingIgnoreCase(RoleName roleName, String email, Pageable pageable);
    long countByRole_Name(RoleName roleName);
}

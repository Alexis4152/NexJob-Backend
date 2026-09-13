package com.nexjob.platform.repository;

import com.nexjob.platform.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    List<PasswordResetToken> findByUser_EmailAndUsedAtIsNull(String email);
    long countByUser_EmailAndCreatedAtAfter(String email, LocalDateTime since);
}

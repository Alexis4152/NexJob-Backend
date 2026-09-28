package com.nexjob.platform.repository;

import com.nexjob.platform.entity.EmailVerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface EmailVerificationCodeRepository extends JpaRepository<EmailVerificationCode, Long> {
    List<EmailVerificationCode> findByUser_IdAndUsedAtIsNull(Long userId);
    long countByUser_IdAndCreatedAtAfter(Long userId, LocalDateTime since);
}

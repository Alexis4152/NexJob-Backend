package com.nexjob.platform.repository;

import com.nexjob.platform.entity.QuoteRequest;
import com.nexjob.platform.enums.QuoteRequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface QuoteRequestRepository extends JpaRepository<QuoteRequest, Long> {
    Page<QuoteRequest> findByClient_IdOrderByCreatedAtDesc(Long clientId, Pageable pageable);
    Optional<QuoteRequest> findByIdAndClient_Id(Long id, Long clientId);
    List<QuoteRequest> findByStatusAndCreatedAtBefore(QuoteRequestStatus status, LocalDateTime cutoff);
}

package com.nexjob.platform.repository;

import com.nexjob.platform.entity.QuoteRequestRecipient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuoteRequestRecipientRepository extends JpaRepository<QuoteRequestRecipient, Long> {
    List<QuoteRequestRecipient> findByQuoteRequest_IdOrderByCreatedAtAsc(Long quoteRequestId);
    Page<QuoteRequestRecipient> findByProvider_IdOrderByCreatedAtDesc(Long providerId, Pageable pageable);
    Optional<QuoteRequestRecipient> findByQuoteRequest_IdAndProvider_Id(Long quoteRequestId, Long providerId);
}

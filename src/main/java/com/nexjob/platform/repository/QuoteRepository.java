package com.nexjob.platform.repository;

import com.nexjob.platform.entity.Quote;
import com.nexjob.platform.enums.QuoteStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuoteRepository extends JpaRepository<Quote, Long> {
    List<Quote> findByQuoteRequest_IdOrderByPriceAsc(Long quoteRequestId);
    Optional<Quote> findByIdAndQuoteRequest_Id(Long id, Long quoteRequestId);
    Optional<Quote> findByQuoteRequest_IdAndProvider_Id(Long quoteRequestId, Long providerId);
    List<Quote> findByQuoteRequest_IdAndStatus(Long quoteRequestId, QuoteStatus status);
}

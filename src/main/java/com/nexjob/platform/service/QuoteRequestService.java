package com.nexjob.platform.service;

import com.nexjob.platform.dto.request.QuoteRequestCreateRequest;
import com.nexjob.platform.dto.request.QuoteSubmitRequest;
import com.nexjob.platform.dto.response.BookingDetailResponse;
import com.nexjob.platform.dto.response.ProviderQuoteRequestDetailResponse;
import com.nexjob.platform.dto.response.ProviderQuoteRequestSummaryResponse;
import com.nexjob.platform.dto.response.QuoteRequestDetailResponse;
import com.nexjob.platform.dto.response.QuoteRequestSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface QuoteRequestService {

    // Cliente
    QuoteRequestDetailResponse create(QuoteRequestCreateRequest request);

    Page<QuoteRequestSummaryResponse> getMine(Pageable pageable);

    QuoteRequestDetailResponse getMineDetail(Long id);

    QuoteRequestDetailResponse cancel(Long id);

    BookingDetailResponse choose(Long id, Long quoteId);

    // Prestador
    Page<ProviderQuoteRequestSummaryResponse> getProviderInbox(Pageable pageable);

    ProviderQuoteRequestDetailResponse getProviderDetail(Long id);

    ProviderQuoteRequestDetailResponse submitQuote(Long id, QuoteSubmitRequest request);

    ProviderQuoteRequestDetailResponse decline(Long id);
}

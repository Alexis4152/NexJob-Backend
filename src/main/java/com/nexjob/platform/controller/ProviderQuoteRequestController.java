package com.nexjob.platform.controller;

import com.nexjob.platform.dto.ApiResponse;
import com.nexjob.platform.dto.PageResponse;
import com.nexjob.platform.dto.request.QuoteSubmitRequest;
import com.nexjob.platform.dto.response.ProviderQuoteRequestDetailResponse;
import com.nexjob.platform.dto.response.ProviderQuoteRequestSummaryResponse;
import com.nexjob.platform.service.QuoteRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

/** Bandeja del prestador para responder solicitudes de cotizacion dirigidas a el. */
@RestController
@RequestMapping("/api/provider/quote-requests")
@RequiredArgsConstructor
public class ProviderQuoteRequestController {

    private final QuoteRequestService quoteRequestService;

    @GetMapping
    public ApiResponse<PageResponse<ProviderQuoteRequestSummaryResponse>> inbox(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(PageResponse.of(quoteRequestService.getProviderInbox(PageRequest.of(page, size))));
    }

    @GetMapping("/{id}")
    public ApiResponse<ProviderQuoteRequestDetailResponse> detail(@PathVariable Long id) {
        return ApiResponse.ok(quoteRequestService.getProviderDetail(id));
    }

    @PostMapping("/{id}/quote")
    public ApiResponse<ProviderQuoteRequestDetailResponse> submitQuote(@PathVariable Long id, @Valid @RequestBody QuoteSubmitRequest request) {
        return ApiResponse.ok(quoteRequestService.submitQuote(id, request), "Cotizacion enviada");
    }

    @PostMapping("/{id}/decline")
    public ApiResponse<ProviderQuoteRequestDetailResponse> decline(@PathVariable Long id) {
        return ApiResponse.ok(quoteRequestService.decline(id), "Solicitud descartada");
    }
}

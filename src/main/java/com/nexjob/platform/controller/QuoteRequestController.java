package com.nexjob.platform.controller;

import com.nexjob.platform.dto.ApiResponse;
import com.nexjob.platform.dto.PageResponse;
import com.nexjob.platform.dto.request.QuoteChooseRequest;
import com.nexjob.platform.dto.request.QuoteRequestCreateRequest;
import com.nexjob.platform.dto.response.BookingDetailResponse;
import com.nexjob.platform.dto.response.QuoteRequestDetailResponse;
import com.nexjob.platform.dto.response.QuoteRequestSummaryResponse;
import com.nexjob.platform.service.QuoteRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Flujo del cliente para "Solicitar varias cotizaciones" (punto 18): describe una vez lo que
 * necesita, se envia automaticamente a varios prestadores recomendados de esa categoria, y
 * compara precio/disponibilidad antes de elegir uno (lo que crea una contratacion real).
 */
@RestController
@RequestMapping("/api/quote-requests")
@RequiredArgsConstructor
public class QuoteRequestController {

    private final QuoteRequestService quoteRequestService;

    @PostMapping
    public ResponseEntity<ApiResponse<QuoteRequestDetailResponse>> create(@Valid @RequestBody QuoteRequestCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(quoteRequestService.create(request), "Solicitud de cotizacion enviada"));
    }

    @GetMapping("/mine")
    public ApiResponse<PageResponse<QuoteRequestSummaryResponse>> mine(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.ok(PageResponse.of(quoteRequestService.getMine(PageRequest.of(page, size))));
    }

    @GetMapping("/{id}")
    public ApiResponse<QuoteRequestDetailResponse> detail(@PathVariable Long id) {
        return ApiResponse.ok(quoteRequestService.getMineDetail(id));
    }

    @PatchMapping("/{id}/cancel")
    public ApiResponse<QuoteRequestDetailResponse> cancel(@PathVariable Long id) {
        return ApiResponse.ok(quoteRequestService.cancel(id), "Solicitud cancelada");
    }

    @PostMapping("/{id}/choose")
    public ApiResponse<BookingDetailResponse> choose(@PathVariable Long id, @Valid @RequestBody QuoteChooseRequest request) {
        return ApiResponse.ok(quoteRequestService.choose(id, request.getQuoteId()), "Cotizacion elegida, contratacion creada");
    }
}

package com.nexjob.platform.controller;

import com.nexjob.platform.dto.ApiResponse;
import com.nexjob.platform.dto.PageResponse;
import com.nexjob.platform.dto.request.BookingCancelRequest;
import com.nexjob.platform.dto.request.BookingRequest;
import com.nexjob.platform.dto.request.ReviewRequest;
import com.nexjob.platform.dto.response.BookingDetailResponse;
import com.nexjob.platform.dto.response.BookingSummaryResponse;
import com.nexjob.platform.dto.response.ReviewResponse;
import com.nexjob.platform.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Flujo de contratacion visto por el cliente: solicitar el servicio (con fecha de visita y
 * metodo de pago pactado), consultar sus contrataciones, cancelar, y al final validar el
 * trabajo concluido para liberar el pago y dejar una resena.
 */
@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<ApiResponse<BookingDetailResponse>> create(@Valid @RequestBody BookingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(bookingService.create(request), "Solicitud enviada al prestador"));
    }

    @GetMapping("/mine")
    public ApiResponse<PageResponse<BookingSummaryResponse>> mine(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.ok(PageResponse.of(bookingService.getMine(PageRequest.of(page, size))));
    }

    @GetMapping("/{id}")
    public ApiResponse<BookingDetailResponse> detail(@PathVariable Long id) {
        return ApiResponse.ok(bookingService.getMineDetail(id));
    }

    @PatchMapping("/{id}/cancel")
    public ApiResponse<BookingDetailResponse> cancel(@PathVariable Long id, @RequestBody(required = false) BookingCancelRequest request) {
        String reason = request == null ? null : request.getReason();
        return ApiResponse.ok(bookingService.cancel(id, reason), "Contratacion cancelada");
    }

    @PostMapping(value = "/{id}/approve", consumes = "multipart/form-data")
    public ApiResponse<BookingDetailResponse> approve(@PathVariable Long id,
                                                        @RequestParam(required = false) String cardNumber,
                                                        @RequestParam(value = "file", required = false) MultipartFile proofFile) {
        return ApiResponse.ok(bookingService.approveAndReleasePayment(id, cardNumber, proofFile), "Servicio validado y pago liberado");
    }

    @PostMapping("/{id}/review")
    public ApiResponse<ReviewResponse> review(@PathVariable Long id, @Valid @RequestBody ReviewRequest request) {
        return ApiResponse.ok(bookingService.addReview(id, request.getRating(), request.getComment()), "Gracias por tu resena");
    }
}

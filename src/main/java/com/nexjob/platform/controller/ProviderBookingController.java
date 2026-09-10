package com.nexjob.platform.controller;

import com.nexjob.platform.dto.ApiResponse;
import com.nexjob.platform.dto.request.BookingStatusUpdateRequest;
import com.nexjob.platform.dto.response.BookingDetailResponse;
import com.nexjob.platform.dto.response.BookingSummaryResponse;
import com.nexjob.platform.dto.response.ProviderDashboardResponse;
import com.nexjob.platform.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Tablero Kanban del prestador: {@code GET /bookings} regresa todas sus contrataciones (el
 * frontend las agrupa por columna segun {@code status}), y {@code PATCH /{id}/status} es el
 * "mover la tarjeta" que dispara la transicion de estado en el backend.
 */
@RestController
@RequestMapping("/api/provider/bookings")
@RequiredArgsConstructor
public class ProviderBookingController {

    private final BookingService bookingService;

    @GetMapping
    public ApiResponse<List<BookingSummaryResponse>> board() {
        return ApiResponse.ok(bookingService.getProviderBoard());
    }

    @GetMapping("/calendar")
    public ApiResponse<List<BookingSummaryResponse>> calendar(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ApiResponse.ok(bookingService.getProviderCalendar(from, to));
    }

    @GetMapping("/{id}")
    public ApiResponse<BookingDetailResponse> detail(@PathVariable Long id) {
        return ApiResponse.ok(bookingService.getProviderDetail(id));
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<BookingDetailResponse> updateStatus(@PathVariable Long id, @Valid @RequestBody BookingStatusUpdateRequest request) {
        return ApiResponse.ok(bookingService.updateStatus(id, request.getNewStatus(), request.getNote()), "Estado actualizado");
    }

    @PostMapping(value = "/{id}/evidence", consumes = "multipart/form-data")
    public ApiResponse<BookingDetailResponse> addEvidence(@PathVariable Long id,
                                                            @RequestParam("file") MultipartFile file,
                                                            @RequestParam(required = false) String description) {
        return ApiResponse.ok(bookingService.addEvidence(id, file, description), "Evidencia subida");
    }
}

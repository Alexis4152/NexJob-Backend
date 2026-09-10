package com.nexjob.platform.controller;

import com.nexjob.platform.dto.ApiResponse;
import com.nexjob.platform.dto.PageResponse;
import com.nexjob.platform.dto.response.ProviderDetailResponse;
import com.nexjob.platform.dto.response.ProviderSummaryResponse;
import com.nexjob.platform.service.ProviderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** Busqueda y ficha publica de prestadores: como la ama de casa encuentra y evalua a un carpintero, plomero, etc. */
@RestController
@RequestMapping("/api/public/providers")
@RequiredArgsConstructor
public class PublicProviderController {

    private final ProviderService providerService;

    @GetMapping
    public ApiResponse<PageResponse<ProviderSummaryResponse>> search(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) BigDecimal minRating,
            @RequestParam(required = false, defaultValue = "name") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        var result = providerService.search(categoryId, q, city, minRating, sort, PageRequest.of(page, size));
        return ApiResponse.ok(PageResponse.of(result));
    }

    @GetMapping("/{id}")
    public ApiResponse<ProviderDetailResponse> detail(@PathVariable Long id) {
        return ApiResponse.ok(providerService.getPublicDetail(id));
    }

    @GetMapping("/{id}/busy-slots")
    public ApiResponse<List<LocalDateTime>> busySlots(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ApiResponse.ok(providerService.getBusySlots(id, from, to));
    }
}

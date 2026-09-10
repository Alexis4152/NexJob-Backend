package com.nexjob.platform.controller;

import com.nexjob.platform.dto.ApiResponse;
import com.nexjob.platform.dto.PageResponse;
import com.nexjob.platform.dto.response.ReviewResponse;
import com.nexjob.platform.service.ProviderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

/** Resenas recibidas por el prestador autenticado, consultables por servicio. */
@RestController
@RequestMapping("/api/provider/reviews")
@RequiredArgsConstructor
public class ProviderReviewController {

    private final ProviderService providerService;

    @GetMapping
    public ApiResponse<PageResponse<ReviewResponse>> list(
            @RequestParam(required = false) Long serviceId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var result = providerService.getMyReviews(serviceId, PageRequest.of(page, size));
        return ApiResponse.ok(PageResponse.of(result));
    }
}

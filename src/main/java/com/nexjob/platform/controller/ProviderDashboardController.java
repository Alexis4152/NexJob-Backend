package com.nexjob.platform.controller;

import com.nexjob.platform.dto.ApiResponse;
import com.nexjob.platform.dto.response.ProviderDashboardResponse;
import com.nexjob.platform.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/provider/dashboard")
@RequiredArgsConstructor
public class ProviderDashboardController {

    private final BookingService bookingService;

    @GetMapping
    public ApiResponse<ProviderDashboardResponse> get() {
        return ApiResponse.ok(bookingService.getProviderDashboard());
    }
}

package com.nexjob.platform.controller;

import com.nexjob.platform.dto.ApiResponse;
import com.nexjob.platform.dto.response.AnalyticsSummaryResponse;
import com.nexjob.platform.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/analytics")
@RequiredArgsConstructor
public class AdminAnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/summary")
    public ApiResponse<AnalyticsSummaryResponse> summary() {
        return ApiResponse.ok(analyticsService.getSummary());
    }
}

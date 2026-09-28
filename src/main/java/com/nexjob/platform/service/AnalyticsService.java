package com.nexjob.platform.service;

import com.nexjob.platform.dto.request.PageViewRequest;
import com.nexjob.platform.dto.response.AnalyticsSummaryResponse;
import com.nexjob.platform.dto.response.PageViewResponse;

public interface AnalyticsService {
    PageViewResponse recordPageView(PageViewRequest request);
    void recordDuration(Long id, Integer durationSeconds);
    AnalyticsSummaryResponse getSummary();
}

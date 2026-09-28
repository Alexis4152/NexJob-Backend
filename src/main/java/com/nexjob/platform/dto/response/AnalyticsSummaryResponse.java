package com.nexjob.platform.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AnalyticsSummaryResponse {
    private long visitsToday;
    private long visitsLast7Days;
    private long visitsLast30Days;
    private long uniqueVisitorsToday;
    private long uniqueVisitorsLast7Days;
    private long uniqueVisitorsLast30Days;
    // Promedio en segundos, solo de las visitas de los ultimos 30 dias que alcanzaron a
    // reportar su duracion; null si todavia no hay ninguna.
    private Double avgDurationSecondsLast30Days;
    private List<DailyVisitsPoint> dailyBreakdown;
    private List<TopPathResponse> topPaths;
}

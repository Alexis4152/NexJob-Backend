package com.nexjob.platform.service.impl;

import com.nexjob.platform.dto.request.PageViewRequest;
import com.nexjob.platform.dto.response.AnalyticsSummaryResponse;
import com.nexjob.platform.dto.response.DailyVisitsPoint;
import com.nexjob.platform.dto.response.PageViewResponse;
import com.nexjob.platform.dto.response.TopPathResponse;
import com.nexjob.platform.entity.PageView;
import com.nexjob.platform.repository.PageViewRepository;
import com.nexjob.platform.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.stream.Collectors;

/**
 * Metricas de trafico anonimo del sitio (ver PageView): cuenta visitas y visitantes unicos por
 * dia/7 dias/30 dias en ventanas moviles (no semana/mes de calendario), que es lo que le sirve
 * a un negocio para saber "cuanta gente me esta viendo" en cualquier momento del mes.
 */
@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private static final int CHART_DAYS = 30;
    private static final int TOP_PATHS_LIMIT = 8;

    private final PageViewRepository pageViewRepository;

    @Override
    @Transactional
    public PageViewResponse recordPageView(PageViewRequest request) {
        PageView saved = pageViewRepository.save(PageView.builder()
                .visitorId(request.getVisitorId())
                .path(request.getPath())
                .referrer(request.getReferrer())
                .build());
        return PageViewResponse.builder().id(saved.getId()).build();
    }

    @Override
    @Transactional
    public void recordDuration(Long id, Integer durationSeconds) {
        // Llega desde navigator.sendBeacon al salir de la pagina: si el id ya no existe o llega
        // tarde no hay nada que reportarle al usuario, simplemente se ignora.
        pageViewRepository.findById(id).ifPresent(pv -> {
            pv.setDurationSeconds(durationSeconds);
            pageViewRepository.save(pv);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public AnalyticsSummaryResponse getSummary() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfToday = today.atStartOfDay();
        LocalDateTime from7 = today.minusDays(6).atStartOfDay();
        LocalDateTime from30 = today.minusDays(CHART_DAYS - 1L).atStartOfDay();

        List<PageViewRepository.Stats> rows = pageViewRepository.findStatsSince(from30);

        List<PageViewRepository.Stats> rowsToday = rows.stream().filter(r -> !r.getCreatedAt().isBefore(startOfToday)).toList();
        List<PageViewRepository.Stats> rows7 = rows.stream().filter(r -> !r.getCreatedAt().isBefore(from7)).toList();

        OptionalDouble avgDurationOpt = rows.stream()
                .map(PageViewRepository.Stats::getDurationSeconds)
                .filter(d -> d != null)
                .mapToInt(Integer::intValue)
                .average();
        Double avgDuration = avgDurationOpt.isPresent() ? avgDurationOpt.getAsDouble() : null;

        Map<LocalDate, List<PageViewRepository.Stats>> byDay = rows.stream()
                .collect(Collectors.groupingBy(r -> r.getCreatedAt().toLocalDate()));

        List<DailyVisitsPoint> dailyBreakdown = new ArrayList<>();
        for (int i = CHART_DAYS - 1; i >= 0; i--) {
            LocalDate day = today.minusDays(i);
            List<PageViewRepository.Stats> dayRows = byDay.getOrDefault(day, List.of());
            dailyBreakdown.add(DailyVisitsPoint.builder()
                    .date(day)
                    .visits(dayRows.size())
                    .uniqueVisitors(dayRows.stream().map(PageViewRepository.Stats::getVisitorId).distinct().count())
                    .build());
        }

        List<TopPathResponse> topPaths = rows.stream()
                .collect(Collectors.groupingBy(PageViewRepository.Stats::getPath, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(TOP_PATHS_LIMIT)
                .map(e -> TopPathResponse.builder().path(e.getKey()).visits(e.getValue()).build())
                .toList();

        return AnalyticsSummaryResponse.builder()
                .visitsToday(rowsToday.size())
                .visitsLast7Days(rows7.size())
                .visitsLast30Days(rows.size())
                .uniqueVisitorsToday(rowsToday.stream().map(PageViewRepository.Stats::getVisitorId).distinct().count())
                .uniqueVisitorsLast7Days(rows7.stream().map(PageViewRepository.Stats::getVisitorId).distinct().count())
                .uniqueVisitorsLast30Days(rows.stream().map(PageViewRepository.Stats::getVisitorId).distinct().count())
                .avgDurationSecondsLast30Days(avgDuration)
                .dailyBreakdown(dailyBreakdown)
                .topPaths(topPaths)
                .build();
    }
}

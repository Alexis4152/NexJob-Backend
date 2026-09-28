package com.nexjob.platform.controller;

import com.nexjob.platform.dto.ApiResponse;
import com.nexjob.platform.dto.request.PageViewDurationRequest;
import com.nexjob.platform.dto.request.PageViewRequest;
import com.nexjob.platform.dto.response.PageViewResponse;
import com.nexjob.platform.service.AnalyticsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Registro de visitas anonimas (sin login) para medir trafico real del sitio, ver PageView. */
@RestController
@RequestMapping("/api/public/analytics")
@RequiredArgsConstructor
public class PublicAnalyticsController {

    private final AnalyticsService analyticsService;

    @PostMapping("/pageview")
    public ResponseEntity<ApiResponse<PageViewResponse>> recordPageView(@Valid @RequestBody PageViewRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(analyticsService.recordPageView(request)));
    }

    // Llamado via navigator.sendBeacon al salir/navegar de la pagina: best-effort, nunca debe
    // fallar de forma ruidosa para el visitante (ver AnalyticsServiceImpl.recordDuration).
    @PostMapping("/pageview/{id}/duration")
    public ResponseEntity<Void> recordDuration(@PathVariable Long id, @Valid @RequestBody PageViewDurationRequest request) {
        analyticsService.recordDuration(id, request.getDurationSeconds());
        return ResponseEntity.noContent().build();
    }
}

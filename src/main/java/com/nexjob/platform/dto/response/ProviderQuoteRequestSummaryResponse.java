package com.nexjob.platform.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** Fila de la bandeja "Cotizaciones" del prestador. */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ProviderQuoteRequestSummaryResponse {
    private Long id;
    private String categoryName;
    private String description;
    private String city;
    private String requestStatus;
    /** "PENDIENTE" | "COTIZO" | "DESCARTADO" (lo que el PROPIO prestador hizo con esta solicitud). */
    private String myStatus;
    private LocalDateTime createdAt;
}

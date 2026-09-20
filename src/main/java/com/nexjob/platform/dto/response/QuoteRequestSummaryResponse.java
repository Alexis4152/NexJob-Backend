package com.nexjob.platform.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** Fila de la lista "Mis cotizaciones" del cliente. */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class QuoteRequestSummaryResponse {
    private Long id;
    private String categoryName;
    private String description;
    private String city;
    private String status;
    private int recipientsCount;
    private int quotesCount;
    private LocalDateTime createdAt;
}

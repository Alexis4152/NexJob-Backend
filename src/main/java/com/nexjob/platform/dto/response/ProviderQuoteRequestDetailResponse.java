package com.nexjob.platform.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Detalle de una solicitud de cotizacion visto por el prestador. A proposito NO incluye nombre
 * ni telefono del cliente (mismo criterio del punto 8: el contacto se comparte hasta que el
 * cliente elige una cotizacion y se genera la contratacion real).
 */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ProviderQuoteRequestDetailResponse {
    private Long id;
    private Long categoryId;
    private String categoryName;
    private String description;
    private String city;
    private LocalDateTime scheduledAt;
    private String urgency;
    private String requestStatus;
    private LocalDateTime createdAt;
    private Boolean myDeclined;
    private QuoteResponse myQuote;
}

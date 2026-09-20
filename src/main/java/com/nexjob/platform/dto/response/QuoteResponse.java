package com.nexjob.platform.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Una cotizacion recibida, tal como la ve el cliente en la pantalla de comparacion. */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class QuoteResponse {
    private Long id;
    private Long providerId;
    private String providerBusinessName;
    private String providerProfileImageUrl;
    private BigDecimal providerAverageRating;
    private Integer providerTotalReviews;
    /** "BASICO" | "VERIFICADO" | "DESTACADO" | null, mismo criterio que en busqueda/perfil. */
    private String providerTrustTier;
    private Long providerAverageResponseMinutes;
    private Long serviceOfferingId;
    private String serviceTitle;
    private BigDecimal price;
    private LocalDateTime availableAt;
    private String note;
    private String status;
    private LocalDateTime createdAt;
}

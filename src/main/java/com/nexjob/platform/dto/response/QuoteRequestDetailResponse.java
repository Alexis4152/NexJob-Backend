package com.nexjob.platform.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/** Detalle de una solicitud de cotizacion tal como la ve el cliente (progreso + comparacion). */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class QuoteRequestDetailResponse {
    private Long id;
    private Long categoryId;
    private String categoryName;
    private String description;
    private String addressLine;
    private String city;
    private LocalDateTime scheduledAt;
    private String paymentMethod;
    private String urgency;
    private String status;
    private LocalDateTime createdAt;
    private Long resultingBookingId;
    private List<QuoteRequestRecipientResponse> recipients;
    private List<QuoteResponse> quotes;
}

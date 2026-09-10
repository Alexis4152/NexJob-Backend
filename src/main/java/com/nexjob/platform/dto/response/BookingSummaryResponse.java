package com.nexjob.platform.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class BookingSummaryResponse {
    private Long id;
    private String folio;
    private String serviceTitle;
    private String providerBusinessName;
    private String clientFullName;
    private BigDecimal agreedPrice;
    private String status;
    private LocalDateTime scheduledAt;
    private LocalDateTime createdAt;
}

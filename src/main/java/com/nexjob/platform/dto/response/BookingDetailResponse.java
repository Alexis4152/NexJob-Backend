package com.nexjob.platform.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class BookingDetailResponse {
    private Long id;
    private String folio;

    private Long serviceId;
    private String serviceTitle;
    private Long categoryId;
    private String categoryName;

    private Long providerId;
    private String providerBusinessName;
    private String providerPhone;

    private Long clientId;
    private String clientFirstName;
    private String clientLastName;
    private String clientPhone;

    private BigDecimal agreedPrice;
    private String description;
    private String addressLine;
    private String city;
    private LocalDateTime scheduledAt;
    private String status;
    private String paymentMethod;
    private String urgency;
    private String cancelledReason;
    private LocalDateTime createdAt;

    private List<BookingEvidenceResponse> evidences;
    private List<BookingStatusHistoryResponse> history;
    private PaymentResponse payment;
    private ReviewResponse review;
}

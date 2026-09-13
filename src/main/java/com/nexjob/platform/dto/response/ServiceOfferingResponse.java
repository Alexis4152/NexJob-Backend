package com.nexjob.platform.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ServiceOfferingResponse {
    private Long id;
    private Long providerId;
    private String providerBusinessName;
    private Long categoryId;
    private String categoryName;
    private String title;
    private String description;
    private BigDecimal price;
    private String priceType;
    private Integer estimatedDurationValue;
    private String estimatedDurationUnit;
    private Boolean atClientLocation;
    private Boolean isActive;
    private List<ServiceImageResponse> images;
}

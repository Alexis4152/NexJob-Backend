package com.nexjob.platform.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/** Tarjeta de prestador en resultados de busqueda/catalogo. */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ProviderSummaryResponse {
    private Long id;
    private String businessName;
    private String bio;
    private Integer yearsExperience;
    private String city;
    private String profileImageUrl;
    private BigDecimal averageRating;
    private Integer totalReviews;
    private Boolean isVerified;
    private List<CategoryResponse> categories;
    private BigDecimal fromPrice;
}

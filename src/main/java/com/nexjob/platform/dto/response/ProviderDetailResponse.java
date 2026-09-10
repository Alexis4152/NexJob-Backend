package com.nexjob.platform.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/** Perfil publico completo de un prestador: datos, categorias, servicios activos y resenas. */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ProviderDetailResponse {
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
    private List<ServiceOfferingResponse> services;
    private List<ReviewResponse> recentReviews;
}

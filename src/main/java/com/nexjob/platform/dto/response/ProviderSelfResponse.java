package com.nexjob.platform.dto.response;

import com.nexjob.platform.enums.ServiceDays;
import com.nexjob.platform.enums.ServiceHours;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/** Perfil del prestador visto por el propio prestador (incluye datos de administracion). */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ProviderSelfResponse {
    private Long id;
    private String businessName;
    private String bio;
    private Integer yearsExperience;
    private String city;
    private String postalCode;
    private String profileImageUrl;
    private ServiceDays serviceDays;
    private ServiceHours serviceHours;
    private BigDecimal averageRating;
    private Integer totalReviews;
    private Boolean isVerified;
    private Boolean emailVerified;
    private Boolean phoneVerified;
    private Boolean profileComplete;
    private List<CategoryResponse> categories;
}

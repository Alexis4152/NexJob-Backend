package com.nexjob.platform.mapper;

import com.nexjob.platform.dto.response.ProviderDetailResponse;
import com.nexjob.platform.dto.response.ProviderSelfResponse;
import com.nexjob.platform.dto.response.ProviderSummaryResponse;
import com.nexjob.platform.dto.response.ReviewResponse;
import com.nexjob.platform.dto.response.ServiceOfferingResponse;
import com.nexjob.platform.entity.ProviderProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ProviderMapper {

    private final CategoryMapper categoryMapper;

    public ProviderSummaryResponse toSummary(ProviderProfile p, BigDecimal fromPrice, Boolean hasPhotos, Double distanceKm, Long averageResponseMinutes, String trustTier) {
        return ProviderSummaryResponse.builder()
                .id(p.getId())
                .businessName(p.getBusinessName())
                .bio(p.getBio())
                .yearsExperience(p.getYearsExperience())
                .city(p.getCity())
                .profileImageUrl(p.getProfileImageUrl())
                .serviceDays(p.getServiceDays())
                .serviceHours(p.getServiceHours())
                .averageRating(p.getAverageRating())
                .totalReviews(p.getTotalReviews())
                .isVerified(p.getIsVerified())
                .categories(p.getCategories().stream().map(categoryMapper::toResponse).toList())
                .fromPrice(fromPrice)
                .hasPhotos(hasPhotos)
                .distanceKm(distanceKm)
                .averageResponseMinutes(averageResponseMinutes)
                .trustTier(trustTier)
                .build();
    }

    public ProviderSelfResponse toSelf(ProviderProfile p) {
        return ProviderSelfResponse.builder()
                .id(p.getId())
                .businessName(p.getBusinessName())
                .bio(p.getBio())
                .yearsExperience(p.getYearsExperience())
                .city(p.getCity())
                .postalCode(p.getPostalCode())
                .profileImageUrl(p.getProfileImageUrl())
                .serviceDays(p.getServiceDays())
                .serviceHours(p.getServiceHours())
                .averageRating(p.getAverageRating())
                .totalReviews(p.getTotalReviews())
                .isVerified(p.getIsVerified())
                .emailVerified(p.getEmailVerified())
                .phoneVerified(p.getPhoneVerified())
                .profileComplete(p.getProfileComplete())
                .categories(p.getCategories().stream().map(categoryMapper::toResponse).toList())
                .build();
    }

    public ProviderDetailResponse toDetail(ProviderProfile p, List<ServiceOfferingResponse> services, List<ReviewResponse> reviews, Integer completedJobs, Long averageResponseMinutes, String trustTier) {
        return ProviderDetailResponse.builder()
                .id(p.getId())
                .businessName(p.getBusinessName())
                .bio(p.getBio())
                .yearsExperience(p.getYearsExperience())
                .city(p.getCity())
                .profileImageUrl(p.getProfileImageUrl())
                .serviceDays(p.getServiceDays())
                .serviceHours(p.getServiceHours())
                .averageRating(p.getAverageRating())
                .totalReviews(p.getTotalReviews())
                .isVerified(p.getIsVerified())
                .emailVerified(p.getEmailVerified())
                .phoneVerified(p.getPhoneVerified())
                .profileComplete(p.getProfileComplete())
                .completedJobs(completedJobs)
                .averageResponseMinutes(averageResponseMinutes)
                .trustTier(trustTier)
                .categories(p.getCategories().stream().map(categoryMapper::toResponse).toList())
                .services(services)
                .recentReviews(reviews)
                .build();
    }
}

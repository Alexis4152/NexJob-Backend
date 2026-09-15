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

    public ProviderSummaryResponse toSummary(ProviderProfile p, BigDecimal fromPrice, Boolean hasPhotos, Double distanceKm) {
        return ProviderSummaryResponse.builder()
                .id(p.getId())
                .businessName(p.getBusinessName())
                .bio(p.getBio())
                .yearsExperience(p.getYearsExperience())
                .city(p.getCity())
                .profileImageUrl(p.getProfileImageUrl())
                .averageRating(p.getAverageRating())
                .totalReviews(p.getTotalReviews())
                .isVerified(p.getIsVerified())
                .categories(p.getCategories().stream().map(categoryMapper::toResponse).toList())
                .fromPrice(fromPrice)
                .hasPhotos(hasPhotos)
                .distanceKm(distanceKm)
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
                .averageRating(p.getAverageRating())
                .totalReviews(p.getTotalReviews())
                .isVerified(p.getIsVerified())
                .categories(p.getCategories().stream().map(categoryMapper::toResponse).toList())
                .build();
    }

    public ProviderDetailResponse toDetail(ProviderProfile p, List<ServiceOfferingResponse> services, List<ReviewResponse> reviews) {
        return ProviderDetailResponse.builder()
                .id(p.getId())
                .businessName(p.getBusinessName())
                .bio(p.getBio())
                .yearsExperience(p.getYearsExperience())
                .city(p.getCity())
                .profileImageUrl(p.getProfileImageUrl())
                .averageRating(p.getAverageRating())
                .totalReviews(p.getTotalReviews())
                .isVerified(p.getIsVerified())
                .categories(p.getCategories().stream().map(categoryMapper::toResponse).toList())
                .services(services)
                .recentReviews(reviews)
                .build();
    }
}

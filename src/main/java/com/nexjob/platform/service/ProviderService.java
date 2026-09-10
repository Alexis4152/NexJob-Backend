package com.nexjob.platform.service;

import com.nexjob.platform.dto.request.ProviderProfileRequest;
import com.nexjob.platform.dto.response.ProviderDetailResponse;
import com.nexjob.platform.dto.response.ProviderSelfResponse;
import com.nexjob.platform.dto.response.ProviderSummaryResponse;
import com.nexjob.platform.dto.response.ReviewResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface ProviderService {

    Page<ProviderSummaryResponse> search(Long categoryId, String q, String city, BigDecimal minRating,
                                          String sort, Pageable pageable);

    ProviderDetailResponse getPublicDetail(Long providerId);

    List<LocalDateTime> getBusySlots(Long providerId, LocalDateTime from, LocalDateTime to);

    ProviderSelfResponse getMyProfile();

    Page<ReviewResponse> getMyReviews(Long serviceId, Pageable pageable);

    ProviderSelfResponse updateMyProfile(ProviderProfileRequest request);

    ProviderSelfResponse updateMyProfileImage(MultipartFile file);

    Page<ProviderSelfResponse> adminList(String q, Pageable pageable);

    ProviderSelfResponse adminSetVerified(Long providerId, boolean verified);
}

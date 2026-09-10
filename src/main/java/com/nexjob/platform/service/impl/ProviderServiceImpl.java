package com.nexjob.platform.service.impl;

import com.nexjob.platform.dto.request.ProviderProfileRequest;
import com.nexjob.platform.dto.response.ProviderDetailResponse;
import com.nexjob.platform.dto.response.ProviderSelfResponse;
import com.nexjob.platform.dto.response.ProviderSummaryResponse;
import com.nexjob.platform.dto.response.ReviewResponse;
import com.nexjob.platform.dto.response.ServiceOfferingResponse;
import com.nexjob.platform.entity.Category;
import com.nexjob.platform.entity.ProviderProfile;
import com.nexjob.platform.entity.Review;
import com.nexjob.platform.entity.ServiceOffering;
import com.nexjob.platform.entity.User;
import com.nexjob.platform.enums.BookingStatus;
import com.nexjob.platform.exception.BusinessException;
import com.nexjob.platform.exception.ResourceNotFoundException;
import com.nexjob.platform.mapper.ProviderMapper;
import com.nexjob.platform.mapper.ReviewMapper;
import com.nexjob.platform.mapper.ServiceOfferingMapper;
import com.nexjob.platform.repository.BookingRepository;
import com.nexjob.platform.repository.CategoryRepository;
import com.nexjob.platform.repository.ProviderProfileRepository;
import com.nexjob.platform.repository.ReviewRepository;
import com.nexjob.platform.repository.ServiceImageRepository;
import com.nexjob.platform.repository.ServiceOfferingRepository;
import com.nexjob.platform.security.SecurityUtils;
import com.nexjob.platform.service.FileStorageService;
import com.nexjob.platform.service.ProviderService;
import com.nexjob.platform.service.ProviderSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ProviderServiceImpl implements ProviderService {

    private final ProviderProfileRepository providerProfileRepository;
    private final CategoryRepository categoryRepository;
    private final ServiceOfferingRepository serviceOfferingRepository;
    private final ServiceImageRepository serviceImageRepository;
    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;
    private final FileStorageService fileStorageService;
    private final ProviderMapper providerMapper;
    private final ServiceOfferingMapper serviceOfferingMapper;
    private final ReviewMapper reviewMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<ProviderSummaryResponse> search(Long categoryId, String q, String city, BigDecimal minRating,
                                                 String sort, Pageable pageable) {
        Sort tieBreaker = Sort.by(Sort.Direction.ASC, "id");
        Sort sortSpec;
        if ("rating".equalsIgnoreCase(sort)) {
            sortSpec = Sort.by(Sort.Direction.DESC, "averageRating").and(tieBreaker);
        } else if ("reviews".equalsIgnoreCase(sort)) {
            sortSpec = Sort.by(Sort.Direction.DESC, "totalReviews").and(tieBreaker);
        } else {
            sortSpec = Sort.by(Sort.Direction.ASC, "businessName").and(tieBreaker);
        }
        Pageable finalPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sortSpec);

        Page<ProviderProfile> page = providerProfileRepository.findAll(
                ProviderSpecifications.search(categoryId, q, city, minRating), finalPageable);

        return page.map(p -> {
            BigDecimal fromPrice = serviceOfferingRepository.findByProvider_IdAndIsActiveTrue(p.getId()).stream()
                    .map(ServiceOffering::getPrice)
                    .min(BigDecimal::compareTo)
                    .orElse(null);
            return providerMapper.toSummary(p, fromPrice);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public ProviderDetailResponse getPublicDetail(Long providerId) {
        ProviderProfile provider = providerProfileRepository.findByIdAndIsActiveTrue(providerId)
                .orElseThrow(() -> new ResourceNotFoundException("Prestador no encontrado: " + providerId));

        List<ServiceOfferingResponse> services = serviceOfferingRepository.findByProvider_IdAndIsActiveTrue(providerId)
                .stream()
                .map(s -> serviceOfferingMapper.toResponse(s, serviceImageRepository.findByService_IdOrderBySortOrderAsc(s.getId())))
                .toList();

        List<ReviewResponse> reviews = reviewRepository
                .findByProvider_IdOrderByCreatedAtDesc(providerId, PageRequest.of(0, 10))
                .map(reviewMapper::toResponse)
                .getContent();

        return providerMapper.toDetail(provider, services, reviews);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LocalDateTime> getBusySlots(Long providerId, LocalDateTime from, LocalDateTime to) {
        return bookingRepository.findByProvider_IdAndScheduledAtBetweenAndStatusNotInOrderByScheduledAtAsc(
                        providerId, from, to, BookingStatus.SLOT_RELEASED)
                .stream()
                .map(b -> b.getScheduledAt())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProviderSelfResponse getMyProfile() {
        return providerMapper.toSelf(myProfile());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewResponse> getMyReviews(Long serviceId, Pageable pageable) {
        Long providerId = myProfile().getId();
        Page<Review> page = serviceId != null
                ? reviewRepository.findByProvider_IdAndBooking_Service_IdOrderByCreatedAtDesc(providerId, serviceId, pageable)
                : reviewRepository.findByProvider_IdOrderByCreatedAtDesc(providerId, pageable);
        return page.map(reviewMapper::toResponse);
    }

    @Override
    @Transactional
    public ProviderSelfResponse updateMyProfile(ProviderProfileRequest request) {
        ProviderProfile profile = myProfile();
        Set<Category> categories = new HashSet<>(categoryRepository.findAllById(request.getCategoryIds()));
        if (categories.isEmpty()) {
            throw new BusinessException("Selecciona al menos una categoria valida");
        }
        profile.setBusinessName(request.getBusinessName());
        profile.setBio(request.getBio());
        profile.setYearsExperience(request.getYearsExperience());
        profile.setCity(request.getCity());
        profile.setCategories(categories);
        profile.setUpdatedBy(SecurityUtils.getCurrentUserOrNull());
        return providerMapper.toSelf(providerProfileRepository.save(profile));
    }

    @Override
    @Transactional
    public ProviderSelfResponse updateMyProfileImage(MultipartFile file) {
        ProviderProfile profile = myProfile();
        profile.setProfileImageUrl(fileStorageService.store(file, "providers"));
        return providerMapper.toSelf(providerProfileRepository.save(profile));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProviderSelfResponse> adminList(String q, Pageable pageable) {
        Page<ProviderProfile> page = providerProfileRepository.findAll(
                ProviderSpecifications.search(null, q, null, null), pageable);
        return page.map(providerMapper::toSelf);
    }

    @Override
    @Transactional
    public ProviderSelfResponse adminSetVerified(Long providerId, boolean verified) {
        ProviderProfile profile = providerProfileRepository.findById(providerId)
                .orElseThrow(() -> new ResourceNotFoundException("Prestador no encontrado: " + providerId));
        profile.setIsVerified(verified);
        profile.setUpdatedBy(SecurityUtils.getCurrentUserOrNull());
        return providerMapper.toSelf(providerProfileRepository.save(profile));
    }

    private ProviderProfile myProfile() {
        User user = SecurityUtils.getCurrentUserOrNull();
        if (user == null) {
            throw new ResourceNotFoundException("No hay sesion activa");
        }
        return providerProfileRepository.findByUser_Id(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("No tienes un perfil de prestador"));
    }
}

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
import com.nexjob.platform.service.PostalCodeLookupService;
import com.nexjob.platform.service.ProviderService;
import com.nexjob.platform.service.ProviderSpecifications;
import com.nexjob.platform.util.GeoUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
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
    private final PostalCodeLookupService postalCodeLookupService;
    private final ProviderMapper providerMapper;
    private final ServiceOfferingMapper serviceOfferingMapper;
    private final ReviewMapper reviewMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<ProviderSummaryResponse> search(Long categoryId, String q, String city, BigDecimal minRating,
                                                 Boolean verified, Integer minExperience, BigDecimal minPrice, BigDecimal maxPrice,
                                                 Boolean hasPhotos, String availability, LocalDate date, String serviceType,
                                                 Double lat, Double lng, Double maxDistanceKm,
                                                 String sort, Pageable pageable) {
        Specification<ProviderProfile> spec = ProviderSpecifications.search(
                categoryId, q, city, minRating, verified, minExperience, minPrice, maxPrice, hasPhotos, availability, date, serviceType);

        if (lat != null && lng != null) {
            return searchByDistance(spec, lat, lng, maxDistanceKm, sort, pageable);
        }

        Pageable finalPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sortFor(sort));
        Page<ProviderProfile> page = providerProfileRepository.findAll(spec, finalPageable);
        return page.map(p -> providerMapper.toSummary(p, fromPriceOf(p), hasPhotosOf(p), null));
    }

    /**
     * No hay soporte de distancia geografica nativo en el Specification dinamico de arriba
     * (mezclar un ORDER BY calculado con el Sort de Pageable es fragil). El catalogo de
     * prestadores es pequeno, asi que aqui se trae todo lo que ya cumple el resto de filtros
     * y la distancia (Haversine, aproximada a nivel ciudad) se calcula y pagina en memoria.
     */
    private Page<ProviderSummaryResponse> searchByDistance(Specification<ProviderProfile> spec, double lat, double lng,
                                                            Double maxDistanceKm, String sort, Pageable pageable) {
        record Ranked(ProviderProfile provider, Double distanceKm) {
        }

        Comparator<Ranked> comparator;
        if ("recomendados".equalsIgnoreCase(sort)) {
            comparator = Comparator
                    .comparing((Ranked r) -> Boolean.TRUE.equals(r.provider().getIsVerified()) ? 0 : 1)
                    .thenComparing((Ranked r) -> r.provider().getAverageRating(), Comparator.reverseOrder())
                    .thenComparing((Ranked r) -> r.provider().getTotalReviews(), Comparator.reverseOrder());
        } else if ("rating".equalsIgnoreCase(sort)) {
            comparator = Comparator.comparing((Ranked r) -> r.provider().getAverageRating(), Comparator.reverseOrder());
        } else if ("name".equalsIgnoreCase(sort)) {
            comparator = Comparator.comparing(r -> r.provider().getBusinessName());
        } else {
            // "cercanos" (y cualquier otro valor): ya estamos en la rama con lat/lng, tiene sentido.
            comparator = Comparator.comparing(r -> r.distanceKm() == null ? Double.MAX_VALUE : r.distanceKm());
        }
        comparator = comparator.thenComparing(r -> r.provider().getId());

        List<Ranked> ranked = providerProfileRepository.findAll(spec).stream()
                .map(p -> new Ranked(p, (p.getLatitude() != null && p.getLongitude() != null)
                        ? GeoUtils.distanceKm(lat, lng, p.getLatitude().doubleValue(), p.getLongitude().doubleValue())
                        : null))
                .filter(r -> maxDistanceKm == null || (r.distanceKm() != null && r.distanceKm() <= maxDistanceKm))
                .sorted(comparator)
                .toList();

        int total = ranked.size();
        int from = Math.min(pageable.getPageNumber() * pageable.getPageSize(), total);
        int to = Math.min(from + pageable.getPageSize(), total);

        List<ProviderSummaryResponse> content = ranked.subList(from, to).stream()
                .map(r -> providerMapper.toSummary(r.provider(), fromPriceOf(r.provider()), hasPhotosOf(r.provider()), r.distanceKm()))
                .toList();

        return new PageImpl<>(content, pageable, total);
    }

    private Sort sortFor(String sort) {
        Sort tieBreaker = Sort.by(Sort.Direction.ASC, "id");
        if ("recomendados".equalsIgnoreCase(sort)) {
            return Sort.by(Sort.Direction.DESC, "isVerified")
                    .and(Sort.by(Sort.Direction.DESC, "averageRating"))
                    .and(Sort.by(Sort.Direction.DESC, "totalReviews"))
                    .and(tieBreaker);
        } else if ("rating".equalsIgnoreCase(sort)) {
            return Sort.by(Sort.Direction.DESC, "averageRating").and(tieBreaker);
        } else if ("reviews".equalsIgnoreCase(sort)) {
            return Sort.by(Sort.Direction.DESC, "totalReviews").and(tieBreaker);
        }
        return Sort.by(Sort.Direction.ASC, "businessName").and(tieBreaker);
    }

    private BigDecimal fromPriceOf(ProviderProfile p) {
        return serviceOfferingRepository.findByProvider_IdAndIsActiveTrue(p.getId()).stream()
                .map(ServiceOffering::getPrice)
                .min(BigDecimal::compareTo)
                .orElse(null);
    }

    private boolean hasPhotosOf(ProviderProfile p) {
        return serviceImageRepository.existsByService_Provider_IdAndService_IsActiveTrue(p.getId());
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
        profile.setPostalCode(request.getPostalCode());
        // Solo se sobreescribe lat/lng si el CP existe en el catalogo: si no se encuentra
        // (o se dejo en blanco) se conserva la coordenada previa en vez de borrarla.
        postalCodeLookupService.lookup(request.getPostalCode()).ifPresent(coords -> {
            profile.setLatitude(coords.latitude());
            profile.setLongitude(coords.longitude());
        });
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
                ProviderSpecifications.search(null, q, null, null, null, null, null, null, null, null, null, null), pageable);
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

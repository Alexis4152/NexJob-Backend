package com.nexjob.platform.service.impl;

import com.nexjob.platform.dto.request.ServiceOfferingRequest;
import com.nexjob.platform.dto.response.ServiceOfferingResponse;
import com.nexjob.platform.entity.Category;
import com.nexjob.platform.entity.ProviderProfile;
import com.nexjob.platform.entity.ServiceImage;
import com.nexjob.platform.entity.ServiceOffering;
import com.nexjob.platform.entity.User;
import com.nexjob.platform.enums.DurationUnit;
import com.nexjob.platform.exception.BusinessException;
import com.nexjob.platform.exception.ResourceNotFoundException;
import com.nexjob.platform.mapper.ServiceOfferingMapper;
import com.nexjob.platform.repository.CategoryRepository;
import com.nexjob.platform.repository.ProviderProfileRepository;
import com.nexjob.platform.repository.ServiceImageRepository;
import com.nexjob.platform.repository.ServiceOfferingRepository;
import com.nexjob.platform.security.SecurityUtils;
import com.nexjob.platform.service.FileStorageService;
import com.nexjob.platform.service.ServiceOfferingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ServiceOfferingServiceImpl implements ServiceOfferingService {

    private static final int MAX_IMAGES_PER_SERVICE = 3;

    private final ServiceOfferingRepository serviceOfferingRepository;
    private final ServiceImageRepository serviceImageRepository;
    private final CategoryRepository categoryRepository;
    private final ProviderProfileRepository providerProfileRepository;
    private final FileStorageService fileStorageService;
    private final ServiceOfferingMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public Page<ServiceOfferingResponse> listMine(Pageable pageable) {
        ProviderProfile provider = myProvider();
        return serviceOfferingRepository.findByProvider_Id(provider.getId(), pageable)
                .map(s -> mapper.toResponse(s, images(s.getId())));
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceOfferingResponse getMineById(Long id) {
        return mapper.toResponse(findMine(id), images(id));
    }

    @Override
    @Transactional
    public ServiceOfferingResponse create(ServiceOfferingRequest request) {
        ProviderProfile provider = myProvider();
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoria no encontrada: " + request.getCategoryId()));

        ServiceOffering service = ServiceOffering.builder()
                .provider(provider)
                .category(category)
                .title(request.getTitle())
                .description(request.getDescription())
                .price(request.getPrice())
                .priceType(request.getPriceType())
                .estimatedDurationValue(request.getEstimatedDurationValue())
                .estimatedDurationUnit(request.getEstimatedDurationUnit() != null ? request.getEstimatedDurationUnit() : DurationUnit.MINUTOS)
                .atClientLocation(request.getAtClientLocation() != null ? request.getAtClientLocation() : true)
                .build();
        service.setCreatedBy(SecurityUtils.getCurrentUserOrNull());
        service = serviceOfferingRepository.save(service);
        return mapper.toResponse(service, List.of());
    }

    @Override
    @Transactional
    public ServiceOfferingResponse update(Long id, ServiceOfferingRequest request) {
        ServiceOffering service = findMine(id);
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoria no encontrada: " + request.getCategoryId()));

        service.setCategory(category);
        service.setTitle(request.getTitle());
        service.setDescription(request.getDescription());
        service.setPrice(request.getPrice());
        service.setPriceType(request.getPriceType());
        service.setEstimatedDurationValue(request.getEstimatedDurationValue());
        service.setEstimatedDurationUnit(request.getEstimatedDurationUnit() != null ? request.getEstimatedDurationUnit() : DurationUnit.MINUTOS);
        service.setAtClientLocation(request.getAtClientLocation() != null ? request.getAtClientLocation() : true);
        service.setUpdatedBy(SecurityUtils.getCurrentUserOrNull());
        service = serviceOfferingRepository.save(service);
        return mapper.toResponse(service, images(id));
    }

    @Override
    @Transactional
    public void deactivate(Long id) {
        ServiceOffering service = findMine(id);
        service.setIsActive(false);
        service.setDeletedAt(LocalDateTime.now());
        service.setDeletedBy(SecurityUtils.getCurrentUserOrNull());
        serviceOfferingRepository.save(service);
    }

    @Override
    @Transactional
    public void reactivate(Long id) {
        ServiceOffering service = findMine(id);
        service.setIsActive(true);
        service.setDeletedAt(null);
        service.setDeletedBy(null);
        service.setUpdatedBy(SecurityUtils.getCurrentUserOrNull());
        serviceOfferingRepository.save(service);
    }

    @Override
    @Transactional
    public ServiceOfferingResponse addImage(Long id, MultipartFile file) {
        ServiceOffering service = findMine(id);
        List<ServiceImage> existing = images(id);
        if (existing.size() >= MAX_IMAGES_PER_SERVICE) {
            throw new BusinessException("Ya alcanzaste el maximo de " + MAX_IMAGES_PER_SERVICE + " fotos por servicio. Elimina alguna para subir otra.");
        }
        String url = fileStorageService.store(file, "services");
        serviceImageRepository.save(ServiceImage.builder().service(service).url(url).sortOrder(existing.size()).build());
        return mapper.toResponse(service, images(id));
    }

    @Override
    @Transactional
    public void removeImage(Long serviceId, Long imageId) {
        findMine(serviceId);
        serviceImageRepository.deleteByIdAndService_Id(imageId, serviceId);
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceOfferingResponse getPublicById(Long id) {
        ServiceOffering service = serviceOfferingRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado: " + id));
        return mapper.toResponse(service, images(id));
    }

    private List<ServiceImage> images(Long serviceId) {
        return serviceImageRepository.findByService_IdOrderBySortOrderAsc(serviceId);
    }

    private ServiceOffering findMine(Long id) {
        ProviderProfile provider = myProvider();
        return serviceOfferingRepository.findByIdAndProvider_Id(id, provider.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado: " + id));
    }

    private ProviderProfile myProvider() {
        User user = SecurityUtils.getCurrentUserOrNull();
        if (user == null) {
            throw new ResourceNotFoundException("No hay sesion activa");
        }
        return providerProfileRepository.findByUser_Id(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("No tienes un perfil de prestador"));
    }
}

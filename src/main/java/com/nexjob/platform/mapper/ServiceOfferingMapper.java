package com.nexjob.platform.mapper;

import com.nexjob.platform.dto.response.ServiceImageResponse;
import com.nexjob.platform.dto.response.ServiceOfferingResponse;
import com.nexjob.platform.entity.ServiceImage;
import com.nexjob.platform.entity.ServiceOffering;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ServiceOfferingMapper {

    public ServiceOfferingResponse toResponse(ServiceOffering s, List<ServiceImage> images) {
        return ServiceOfferingResponse.builder()
                .id(s.getId())
                .providerId(s.getProvider().getId())
                .providerBusinessName(s.getProvider().getBusinessName())
                .providerServiceDays(s.getProvider().getServiceDays())
                .providerServiceHours(s.getProvider().getServiceHours())
                .categoryId(s.getCategory().getId())
                .categoryName(s.getCategory().getName())
                .title(s.getTitle())
                .description(s.getDescription())
                .price(s.getPrice())
                .priceType(s.getPriceType().name())
                .estimatedDurationValue(s.getEstimatedDurationValue())
                .estimatedDurationUnit(s.getEstimatedDurationUnit() == null ? null : s.getEstimatedDurationUnit().name())
                .atClientLocation(s.getAtClientLocation())
                .isActive(s.getIsActive())
                .images(images == null ? List.of() : images.stream().map(this::toImageResponse).toList())
                .build();
    }

    public ServiceImageResponse toImageResponse(ServiceImage img) {
        return ServiceImageResponse.builder().id(img.getId()).url(img.getUrl()).sortOrder(img.getSortOrder()).build();
    }
}

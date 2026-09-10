package com.nexjob.platform.service;

import com.nexjob.platform.dto.request.ServiceOfferingRequest;
import com.nexjob.platform.dto.response.ServiceOfferingResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface ServiceOfferingService {
    Page<ServiceOfferingResponse> listMine(Pageable pageable);
    ServiceOfferingResponse getMineById(Long id);
    ServiceOfferingResponse create(ServiceOfferingRequest request);
    ServiceOfferingResponse update(Long id, ServiceOfferingRequest request);
    void deactivate(Long id);
    void reactivate(Long id);
    ServiceOfferingResponse addImage(Long id, MultipartFile file);
    void removeImage(Long serviceId, Long imageId);
    ServiceOfferingResponse getPublicById(Long id);
}

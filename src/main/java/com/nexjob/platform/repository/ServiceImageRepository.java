package com.nexjob.platform.repository;

import com.nexjob.platform.entity.ServiceImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServiceImageRepository extends JpaRepository<ServiceImage, Long> {
    List<ServiceImage> findByService_IdOrderBySortOrderAsc(Long serviceId);
    void deleteByIdAndService_Id(Long id, Long serviceId);
}

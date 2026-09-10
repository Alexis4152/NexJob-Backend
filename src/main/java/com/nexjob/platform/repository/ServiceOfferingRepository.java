package com.nexjob.platform.repository;

import com.nexjob.platform.entity.ServiceOffering;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ServiceOfferingRepository extends JpaRepository<ServiceOffering, Long> {
    List<ServiceOffering> findByProvider_IdAndIsActiveTrue(Long providerId);
    Page<ServiceOffering> findByProvider_Id(Long providerId, Pageable pageable);
    Optional<ServiceOffering> findByIdAndProvider_Id(Long id, Long providerId);
    Optional<ServiceOffering> findByIdAndIsActiveTrue(Long id);
}

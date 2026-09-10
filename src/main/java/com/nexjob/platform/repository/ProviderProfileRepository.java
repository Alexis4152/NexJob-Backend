package com.nexjob.platform.repository;

import com.nexjob.platform.entity.ProviderProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface ProviderProfileRepository extends JpaRepository<ProviderProfile, Long>, JpaSpecificationExecutor<ProviderProfile> {
    Optional<ProviderProfile> findByUser_Id(Long userId);
    Optional<ProviderProfile> findByIdAndIsActiveTrue(Long id);
}

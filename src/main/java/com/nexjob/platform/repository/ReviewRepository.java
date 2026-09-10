package com.nexjob.platform.repository;

import com.nexjob.platform.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    Page<Review> findByProvider_IdOrderByCreatedAtDesc(Long providerId, Pageable pageable);
    Page<Review> findByProvider_IdAndBooking_Service_IdOrderByCreatedAtDesc(Long providerId, Long serviceId, Pageable pageable);
    Optional<Review> findByBooking_Id(Long bookingId);
    boolean existsByBooking_Id(Long bookingId);
}

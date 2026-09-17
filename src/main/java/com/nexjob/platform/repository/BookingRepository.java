package com.nexjob.platform.repository;

import com.nexjob.platform.entity.Booking;
import com.nexjob.platform.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long>, JpaSpecificationExecutor<Booking> {
    Page<Booking> findByClient_IdOrderByCreatedAtDesc(Long clientId, Pageable pageable);
    Optional<Booking> findByIdAndClient_Id(Long id, Long clientId);
    Optional<Booking> findByIdAndProvider_Id(Long id, Long providerId);
    List<Booking> findByProvider_IdOrderByScheduledAtAsc(Long providerId);
    List<Booking> findByProvider_IdAndStatus(Long providerId, BookingStatus status);
    long countByStatus(BookingStatus status);
    long countByProvider_IdAndStatus(Long providerId, BookingStatus status);
    List<Booking> findByStatusAndCreatedAtBefore(BookingStatus status, LocalDateTime cutoff);

    boolean existsByProvider_IdAndScheduledAtAndStatusNotIn(Long providerId, LocalDateTime scheduledAt, Collection<BookingStatus> excludedStatuses);

    List<Booking> findByProvider_IdAndScheduledAtBetweenAndStatusNotInOrderByScheduledAtAsc(
            Long providerId, LocalDateTime from, LocalDateTime to, Collection<BookingStatus> excludedStatuses);
}

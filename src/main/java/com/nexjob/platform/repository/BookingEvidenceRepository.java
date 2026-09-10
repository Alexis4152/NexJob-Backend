package com.nexjob.platform.repository;

import com.nexjob.platform.entity.BookingEvidence;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingEvidenceRepository extends JpaRepository<BookingEvidence, Long> {
    List<BookingEvidence> findByBooking_IdOrderByCreatedAtAsc(Long bookingId);
}

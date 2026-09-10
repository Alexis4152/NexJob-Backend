package com.nexjob.platform.repository;

import com.nexjob.platform.entity.BookingStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingStatusHistoryRepository extends JpaRepository<BookingStatusHistory, Long> {
    List<BookingStatusHistory> findByBooking_IdOrderByChangedAtAsc(Long bookingId);
}

package com.nexjob.platform.repository;

import com.nexjob.platform.entity.BookingStatusHistory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookingStatusHistoryRepository extends JpaRepository<BookingStatusHistory, Long> {
    List<BookingStatusHistory> findByBooking_IdOrderByChangedAtAsc(Long bookingId);

    /**
     * Transiciones donde el prestador realmente respondio (SOLICITADO -> ACEPTADO/RECHAZADO),
     * mas recientes primero. Excluye a proposito las cancelaciones automaticas del cron de
     * expiracion (SOLICITADO -> CANCELADO), que no son una respuesta del prestador.
     */
    @Query("SELECT h FROM BookingStatusHistory h JOIN FETCH h.booking b " +
            "WHERE b.provider.id = :providerId " +
            "AND h.previousStatus = com.nexjob.platform.enums.BookingStatus.SOLICITADO " +
            "AND h.newStatus IN (com.nexjob.platform.enums.BookingStatus.ACEPTADO, com.nexjob.platform.enums.BookingStatus.RECHAZADO) " +
            "ORDER BY h.changedAt DESC")
    List<BookingStatusHistory> findRecentResponseTransitions(@Param("providerId") Long providerId, Pageable pageable);
}

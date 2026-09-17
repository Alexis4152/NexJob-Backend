package com.nexjob.platform.scheduler;

import com.nexjob.platform.entity.Booking;
import com.nexjob.platform.entity.BookingStatusHistory;
import com.nexjob.platform.enums.BookingStatus;
import com.nexjob.platform.repository.BookingRepository;
import com.nexjob.platform.repository.BookingStatusHistoryRepository;
import com.nexjob.platform.service.EmailService;
import com.nexjob.platform.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Cancela automaticamente las solicitudes que el prestador dejo sin responder (estado
 * SOLICITADO) durante mas de {@link #EXPIRATION_HOURS} horas, para que el cliente no se quede
 * esperando indefinidamente y pueda elegir otro prestador. Revisa cada
 * {@link #CHECK_INTERVAL_MS}.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BookingExpirationScheduler {

    private static final long EXPIRATION_HOURS = 48;
    private static final long CHECK_INTERVAL_MS = 15 * 60 * 1000; // 15 minutos

    private final BookingRepository bookingRepository;
    private final BookingStatusHistoryRepository historyRepository;
    private final EmailService emailService;
    private final NotificationService notificationService;

    @Scheduled(fixedDelay = CHECK_INTERVAL_MS)
    @Transactional
    public void expireStaleRequests() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(EXPIRATION_HOURS);
        List<Booking> stale = bookingRepository.findByStatusAndCreatedAtBefore(BookingStatus.SOLICITADO, cutoff);
        if (stale.isEmpty()) {
            return;
        }
        stale.forEach(this::expireOne);
        log.info("Auto-cancelacion de solicitudes vencidas: {} contratacion(es) canceladas", stale.size());
    }

    private void expireOne(Booking booking) {
        BookingStatus previous = booking.getStatus();
        booking.setStatus(BookingStatus.CANCELADO);
        booking.setCancelledReason("El prestador no respondio en " + EXPIRATION_HOURS + " horas");
        bookingRepository.save(booking);

        historyRepository.save(BookingStatusHistory.builder()
                .booking(booking)
                .previousStatus(previous)
                .newStatus(BookingStatus.CANCELADO)
                .changedBy(null)
                .note("Cancelacion automatica: sin respuesta del prestador en " + EXPIRATION_HOURS + " horas")
                .build());

        emailService.sendBookingStatusNotification(booking.getClient().getEmail(), booking.getFolio(), "CANCELADO");

        notificationService.notify(booking.getClient(), booking, "BOOKING_EXPIRED",
                "Tu solicitud se cancelo automaticamente",
                booking.getProvider().getBusinessName() + " no respondio en " + EXPIRATION_HOURS + " horas para \""
                        + booking.getService().getTitle() + "\" (folio " + booking.getFolio() + "). Elige otro prestador.",
                "/prestadores?categoryId=" + booking.getService().getCategory().getId());

        notificationService.notify(booking.getProvider().getUser(), booking, "BOOKING_EXPIRED",
                "Solicitud vencida sin respuesta",
                "La solicitud de \"" + booking.getService().getTitle() + "\" (folio " + booking.getFolio()
                        + ") se cancelo automaticamente por falta de respuesta",
                "/prestador/contrataciones/" + booking.getId());
    }
}

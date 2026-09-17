package com.nexjob.platform.service;

import com.nexjob.platform.dto.PageResponse;
import com.nexjob.platform.dto.response.NotificationResponse;
import com.nexjob.platform.entity.Booking;
import com.nexjob.platform.entity.User;
import org.springframework.data.domain.Pageable;

/** Avisos internos (in-app) para clientes y prestadores sobre cambios en sus contrataciones. */
public interface NotificationService {

    /** Crea un aviso para {@code recipient}. Uso interno desde otros services (ej. BookingService). */
    void notify(User recipient, Booking booking, String type, String title, String body, String link);

    PageResponse<NotificationResponse> listMine(Pageable pageable);

    long countMyUnread();

    void markRead(Long notificationId);

    void markAllRead();
}

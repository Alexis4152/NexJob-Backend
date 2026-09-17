package com.nexjob.platform.service.impl;

import com.nexjob.platform.dto.PageResponse;
import com.nexjob.platform.dto.response.NotificationResponse;
import com.nexjob.platform.entity.Booking;
import com.nexjob.platform.entity.Notification;
import com.nexjob.platform.entity.User;
import com.nexjob.platform.exception.ResourceNotFoundException;
import com.nexjob.platform.mapper.NotificationMapper;
import com.nexjob.platform.repository.NotificationRepository;
import com.nexjob.platform.security.SecurityUtils;
import com.nexjob.platform.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    @Override
    @Transactional
    public void notify(User recipient, Booking booking, String type, String title, String body, String link) {
        notificationRepository.save(Notification.builder()
                .user(recipient)
                .booking(booking)
                .type(type)
                .title(title)
                .body(body)
                .link(link)
                .isRead(false)
                .build());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> listMine(Pageable pageable) {
        return PageResponse.of(notificationRepository
                .findByUser_IdOrderByCreatedAtDesc(currentUserId(), pageable)
                .map(notificationMapper::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public long countMyUnread() {
        return notificationRepository.countByUser_IdAndIsReadFalse(currentUserId());
    }

    @Override
    @Transactional
    public void markRead(Long notificationId) {
        Notification notification = notificationRepository.findByIdAndUser_Id(notificationId, currentUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Notificacion no encontrada: " + notificationId));
        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void markAllRead() {
        notificationRepository.markAllReadForUser(currentUserId());
    }

    private Long currentUserId() {
        User user = SecurityUtils.getCurrentUserOrNull();
        if (user == null) {
            throw new ResourceNotFoundException("No hay sesion activa");
        }
        return user.getId();
    }
}

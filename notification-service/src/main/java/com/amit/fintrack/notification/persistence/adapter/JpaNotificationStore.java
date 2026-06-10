package com.amit.fintrack.notification.persistence.adapter;

import com.amit.fintrack.notification.application.model.NewNotification;
import com.amit.fintrack.notification.application.model.NotificationView;
import com.amit.fintrack.notification.application.port.NotificationStore;
import com.amit.fintrack.notification.exception.NotificationNotFoundException;
import com.amit.fintrack.notification.persistence.entity.Notification;
import com.amit.fintrack.notification.persistence.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JpaNotificationStore implements NotificationStore {

    private final NotificationRepository notificationRepository;

    @Override
    public NotificationView save(NewNotification notification) {
        Notification entity = Notification.builder()
                .userId(notification.userId())
                .title(notification.title())
                .message(notification.message())
                .type(notification.type())
                .read(false)
                .createdAt(notification.createdAt())
                .build();

        return toView(notificationRepository.save(entity));
    }

    @Override
    public List<NotificationView> findByUserId(UUID userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toView)
                .toList();
    }

    @Override
    public Optional<NotificationView> findByIdAndUserId(UUID notificationId, UUID userId) {
        return notificationRepository.findByIdAndUserId(notificationId, userId).map(this::toView);
    }

    @Override
    public NotificationView markAsRead(UUID notificationId, UUID userId) {
        Notification notification = notificationRepository.findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new NotificationNotFoundException("Notification not found"));

        notification.setRead(true);
        notification.setReadAt(LocalDateTime.now());

        return toView(notificationRepository.save(notification));
    }

    private NotificationView toView(Notification notification) {
        return new NotificationView(
                notification.getId(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getType(),
                notification.isRead(),
                notification.getCreatedAt(),
                notification.getReadAt()
        );
    }
}

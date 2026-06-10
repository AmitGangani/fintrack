package com.amit.fintrack.notification.application.port;

import com.amit.fintrack.notification.application.model.NewNotification;
import com.amit.fintrack.notification.application.model.NotificationView;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationStore {

    NotificationView save(NewNotification notification);

    List<NotificationView> findByUserId(UUID userId);

    Optional<NotificationView> findByIdAndUserId(UUID notificationId, UUID userId);

    NotificationView markAsRead(UUID notificationId, UUID userId);
}

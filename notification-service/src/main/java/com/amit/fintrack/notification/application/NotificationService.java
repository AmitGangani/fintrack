package com.amit.fintrack.notification.application;

import com.amit.fintrack.notification.application.model.NotificationView;
import com.amit.fintrack.notification.application.port.NotificationStore;
import com.amit.fintrack.notification.exception.NotificationNotFoundException;
import com.amit.fintrack.notification.application.port.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationStore notificationStore;
    private final CurrentUserProvider currentUserProvider;

    public List<NotificationView> getMyNotifications() {
        return notificationStore.findByUserId(currentUserProvider.getCurrentUserId());
    }

    public NotificationView markAsRead(UUID notificationId) {
        UUID currentUserId = currentUserProvider.getCurrentUserId();
        notificationStore.findByIdAndUserId(notificationId, currentUserId)
                .orElseThrow(() -> new NotificationNotFoundException("Notification not found"));

        return notificationStore.markAsRead(notificationId, currentUserId);
    }
}

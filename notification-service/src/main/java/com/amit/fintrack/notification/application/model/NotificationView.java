package com.amit.fintrack.notification.application.model;

import com.amit.fintrack.notification.domain.NotificationType;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationView(
        UUID id,
        String title,
        String message,
        NotificationType type,
        boolean read,
        LocalDateTime createdAt,
        LocalDateTime readAt
) {
}

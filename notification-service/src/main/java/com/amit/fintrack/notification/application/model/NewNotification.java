package com.amit.fintrack.notification.application.model;

import com.amit.fintrack.notification.domain.NotificationType;

import java.time.LocalDateTime;
import java.util.UUID;

public record NewNotification(
        UUID userId,
        String title,
        String message,
        NotificationType type,
        LocalDateTime createdAt
) {
}

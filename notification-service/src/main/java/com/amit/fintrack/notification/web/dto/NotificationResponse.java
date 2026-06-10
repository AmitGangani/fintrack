package com.amit.fintrack.notification.web.dto;

import com.amit.fintrack.notification.domain.NotificationType;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        String title,
        String message,
        NotificationType type,
        boolean read,
        LocalDateTime createdAt,
        LocalDateTime readAt
) {
}
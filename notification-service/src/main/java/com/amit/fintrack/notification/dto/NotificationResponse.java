package com.amit.fintrack.notification.dto;

import com.amit.fintrack.notification.entity.NotificationType;

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
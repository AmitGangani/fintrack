package com.amit.fintrack.notification.domain;

import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class NotificationTypeResolver {

    public Optional<NotificationType> resolve(String alertType) {
        if (alertType == null || alertType.isBlank()) {
            return Optional.empty();
        }

        try {
            return Optional.of(NotificationType.valueOf(alertType));
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }
}

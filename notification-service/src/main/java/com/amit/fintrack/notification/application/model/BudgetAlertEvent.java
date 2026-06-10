package com.amit.fintrack.notification.application.model;

import java.util.UUID;

public record BudgetAlertEvent(
        UUID eventId,
        UUID userId,
        String alertType,
        String message
) {
}

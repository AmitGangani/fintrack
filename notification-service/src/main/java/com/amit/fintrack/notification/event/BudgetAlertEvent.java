package com.amit.fintrack.notification.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record BudgetAlertEvent(
        UUID eventId,
        UUID userId,
        String category,
        int month,
        int year,
        BigDecimal limitAmount,
        BigDecimal spentAmount,
        BigDecimal percentageUsed,
        String alertType,
        String message,
        LocalDateTime occurredAt
) {
}
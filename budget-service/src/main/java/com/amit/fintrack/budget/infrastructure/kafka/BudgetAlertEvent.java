package com.amit.fintrack.budget.infrastructure.kafka;

import com.amit.fintrack.budget.domain.BudgetCategory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record BudgetAlertEvent(
        UUID eventId,
        UUID userId,
        BudgetCategory category,
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
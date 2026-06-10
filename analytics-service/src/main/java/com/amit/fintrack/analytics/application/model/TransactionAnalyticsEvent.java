package com.amit.fintrack.analytics.application.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record TransactionAnalyticsEvent(
        UUID eventId,
        TransactionEventType eventType,
        UUID userId,
        String oldType,
        String oldCategory,
        BigDecimal oldAmount,
        LocalDate oldTransactionDate,
        String newType,
        String newCategory,
        BigDecimal newAmount,
        LocalDate newTransactionDate
) {
}

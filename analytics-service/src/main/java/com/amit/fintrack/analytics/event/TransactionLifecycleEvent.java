package com.amit.fintrack.analytics.event;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransactionLifecycleEvent(
        UUID eventId,
        TransactionEventType eventType,
        UUID transactionId,
        UUID userId,

        UUID oldAccountId,
        UUID newAccountId,

        String oldType,
        String oldCategory,
        BigDecimal oldAmount,
        LocalDate oldTransactionDate,

        String newType,
        String newCategory,
        BigDecimal newAmount,
        LocalDate newTransactionDate,

        LocalDateTime occurredAt
) {
}
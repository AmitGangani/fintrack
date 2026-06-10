package com.amit.fintrack.budget.application.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record TransactionBudgetEvent(
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

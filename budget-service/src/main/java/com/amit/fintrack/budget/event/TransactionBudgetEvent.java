package com.amit.fintrack.budget.event;

import com.amit.fintrack.budget.entity.BudgetCategory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransactionBudgetEvent(
        UUID eventId,
        TransactionEventType eventType,
        UUID transactionId,
        UUID userId,
        UUID accountId,

        String oldType,
        BudgetCategory oldCategory,
        BigDecimal oldAmount,
        LocalDate oldTransactionDate,

        String newType,
        BudgetCategory newCategory,
        BigDecimal newAmount,
        LocalDate newTransactionDate,

        LocalDateTime occurredAt
) {
}
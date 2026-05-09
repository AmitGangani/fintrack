package com.amit.fintrack.transaction.event;

import com.amit.fintrack.transaction.entity.TransactionCategory;
import com.amit.fintrack.transaction.entity.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransactionBudgetEvent(
        UUID eventId,
        TransactionEventType eventType,
        UUID transactionId,
        UUID userId,

        UUID oldAccountId,
        UUID newAccountId,

        TransactionType oldType,
        TransactionCategory oldCategory,
        BigDecimal oldAmount,
        LocalDate oldTransactionDate,

        TransactionType newType,
        TransactionCategory newCategory,
        BigDecimal newAmount,
        LocalDate newTransactionDate,

        LocalDateTime occurredAt
) {
}
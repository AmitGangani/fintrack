package com.amit.fintrack.transaction.application.model;

import com.amit.fintrack.transaction.domain.TransactionCategory;
import com.amit.fintrack.transaction.domain.TransactionType;

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

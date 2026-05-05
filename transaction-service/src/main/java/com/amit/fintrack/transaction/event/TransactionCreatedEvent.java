package com.amit.fintrack.transaction.event;

import com.amit.fintrack.transaction.entity.TransactionCategory;
import com.amit.fintrack.transaction.entity.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransactionCreatedEvent(
        UUID eventId,
        UUID transactionId,
        UUID userId,
        UUID accountId,
        TransactionType type,
        TransactionCategory category,
        BigDecimal amount,
        LocalDate transactionDate,
        LocalDateTime occurredAt
) {
}
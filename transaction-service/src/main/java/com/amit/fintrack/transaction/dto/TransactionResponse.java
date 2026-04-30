package com.amit.fintrack.transaction.dto;

import com.amit.fintrack.transaction.entity.TransactionCategory;
import com.amit.fintrack.transaction.entity.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransactionResponse(
        UUID id,
        UUID accountId,
        TransactionType type,
        TransactionCategory category,
        BigDecimal amount,
        String description,
        LocalDate transactionDate,
        LocalDateTime createdAt
) {
}
package com.amit.fintrack.transaction.web.dto;

import com.amit.fintrack.transaction.domain.TransactionCategory;
import com.amit.fintrack.transaction.domain.TransactionType;

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
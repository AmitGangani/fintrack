package com.amit.fintrack.transaction.application.model;

import com.amit.fintrack.transaction.domain.TransactionCategory;
import com.amit.fintrack.transaction.domain.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record TransactionCommand(
        UUID accountId,
        TransactionType type,
        TransactionCategory category,
        BigDecimal amount,
        String description,
        LocalDate transactionDate
) {
}

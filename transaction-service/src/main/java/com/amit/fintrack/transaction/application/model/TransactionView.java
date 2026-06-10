package com.amit.fintrack.transaction.application.model;

import com.amit.fintrack.transaction.domain.TransactionCategory;
import com.amit.fintrack.transaction.domain.TransactionSummaryItem;
import com.amit.fintrack.transaction.domain.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransactionView(
        UUID id,
        UUID userId,
        UUID accountId,
        TransactionType type,
        TransactionCategory category,
        BigDecimal amount,
        String description,
        LocalDate transactionDate,
        LocalDateTime createdAt
) {

    public TransactionSummaryItem toSummaryItem() {
        return new TransactionSummaryItem(type, category, amount);
    }
}

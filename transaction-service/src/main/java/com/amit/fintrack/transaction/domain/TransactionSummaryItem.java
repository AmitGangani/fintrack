package com.amit.fintrack.transaction.domain;

import java.math.BigDecimal;

public record TransactionSummaryItem(
        TransactionType type,
        TransactionCategory category,
        BigDecimal amount
) {
}

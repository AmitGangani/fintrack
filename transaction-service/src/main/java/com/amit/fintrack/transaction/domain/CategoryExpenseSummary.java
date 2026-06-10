package com.amit.fintrack.transaction.domain;

import java.math.BigDecimal;

public record CategoryExpenseSummary(
        TransactionCategory category,
        BigDecimal totalExpense
) {
}

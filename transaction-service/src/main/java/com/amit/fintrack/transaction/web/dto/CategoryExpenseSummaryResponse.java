package com.amit.fintrack.transaction.web.dto;

import com.amit.fintrack.transaction.domain.TransactionCategory;

import java.math.BigDecimal;

public record CategoryExpenseSummaryResponse(
        TransactionCategory category,
        BigDecimal totalExpense
) {
}
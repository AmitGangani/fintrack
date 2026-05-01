package com.amit.fintrack.transaction.dto;

import com.amit.fintrack.transaction.entity.TransactionCategory;

import java.math.BigDecimal;

public record CategoryExpenseSummaryResponse(
        TransactionCategory category,
        BigDecimal totalExpense
) {
}
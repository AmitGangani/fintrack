package com.amit.fintrack.budget.dto;

import com.amit.fintrack.budget.entity.BudgetCategory;

import java.math.BigDecimal;

public record CategoryExpenseSummaryResponse(
        BudgetCategory category,
        BigDecimal totalExpense
) {
}
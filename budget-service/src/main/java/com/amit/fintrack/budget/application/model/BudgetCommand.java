package com.amit.fintrack.budget.application.model;

import com.amit.fintrack.budget.domain.BudgetCategory;

import java.math.BigDecimal;

public record BudgetCommand(
        BudgetCategory category,
        int month,
        int year,
        BigDecimal limitAmount
) {
}

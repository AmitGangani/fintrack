package com.amit.fintrack.budget.application.model;

import com.amit.fintrack.budget.domain.BudgetCategory;

import java.math.BigDecimal;
import java.util.UUID;

public record BudgetSpendingRecord(
        UUID userId,
        BudgetCategory category,
        int month,
        int year,
        BigDecimal spentAmount
) {
}

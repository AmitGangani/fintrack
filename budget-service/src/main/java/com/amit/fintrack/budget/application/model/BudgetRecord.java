package com.amit.fintrack.budget.application.model;

import com.amit.fintrack.budget.domain.BudgetCategory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record BudgetRecord(
        UUID id,
        UUID userId,
        BudgetCategory category,
        int month,
        int year,
        BigDecimal limitAmount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}

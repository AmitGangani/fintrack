package com.amit.fintrack.budget.dto;

import com.amit.fintrack.budget.entity.BudgetCategory;
import com.amit.fintrack.budget.entity.BudgetStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record BudgetResponse(
        UUID id,
        BudgetCategory category,
        int month,
        int year,
        BigDecimal limitAmount,
        BigDecimal spentAmount,
        BigDecimal remainingAmount,
        BigDecimal percentageUsed,
        BudgetStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
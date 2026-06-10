package com.amit.fintrack.budget.web.dto;

import com.amit.fintrack.budget.domain.BudgetCategory;
import com.amit.fintrack.budget.domain.BudgetStatus;

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
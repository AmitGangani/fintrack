package com.amit.fintrack.budget.domain;

import java.math.BigDecimal;

public record BudgetAlertDecision(
        BudgetAlertType type,
        BigDecimal percentageUsed,
        String message
) {
}

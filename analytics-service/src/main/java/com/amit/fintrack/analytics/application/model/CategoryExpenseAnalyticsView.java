package com.amit.fintrack.analytics.application.model;

import java.math.BigDecimal;

public record CategoryExpenseAnalyticsView(
        String category,
        BigDecimal totalExpense
) {
}

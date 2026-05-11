package com.amit.fintrack.analytics.dto;

import java.math.BigDecimal;

public record CategoryExpenseAnalyticsResponse(
        String category,
        BigDecimal totalExpense
) {
}
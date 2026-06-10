package com.amit.fintrack.analytics.web.dto;

import java.math.BigDecimal;

public record MonthlyAnalyticsResponse(
        int year,
        int month,
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        BigDecimal netSavings
) {
}
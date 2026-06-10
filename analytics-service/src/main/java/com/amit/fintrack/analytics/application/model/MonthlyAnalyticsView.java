package com.amit.fintrack.analytics.application.model;

import java.math.BigDecimal;

public record MonthlyAnalyticsView(
        int year,
        int month,
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        BigDecimal netSavings
) {
}

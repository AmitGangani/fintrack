package com.amit.fintrack.analytics.application.model;

import java.math.BigDecimal;
import java.util.UUID;

public record MonthlyAnalyticsRecord(
        UUID userId,
        int year,
        int month,
        BigDecimal totalIncome,
        BigDecimal totalExpense
) {
}

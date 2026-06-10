package com.amit.fintrack.analytics.application.model;

import java.math.BigDecimal;
import java.util.UUID;

public record CategoryExpenseAnalyticsRecord(
        UUID userId,
        String category,
        int year,
        int month,
        BigDecimal totalExpense
) {
}

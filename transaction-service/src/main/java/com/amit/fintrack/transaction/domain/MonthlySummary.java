package com.amit.fintrack.transaction.domain;

import java.math.BigDecimal;

public record MonthlySummary(
        int year,
        int month,
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        BigDecimal netSavings,
        int transactionCount
) {
}

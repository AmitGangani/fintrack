package com.amit.fintrack.transaction.web.dto;

import java.math.BigDecimal;

public record MonthlySummaryResponse(
        int year,
        int month,
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        BigDecimal netSavings,
        int transactionCount
) {
}
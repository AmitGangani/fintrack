package com.amit.fintrack.analytics.domain;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class AnalyticsProjectionPolicy {

    private static final String INCOME = "INCOME";
    private static final String EXPENSE = "EXPENSE";

    public boolean isIncome(String transactionType) {
        return INCOME.equals(transactionType);
    }

    public boolean isExpense(String transactionType) {
        return EXPENSE.equals(transactionType);
    }

    public BigDecimal zeroFloor(BigDecimal amount) {
        return amount.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : amount;
    }
}

package com.amit.fintrack.account.domain;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

@Component
public class TransactionBalancePolicy {

    private static final String INCOME = "INCOME";
    private static final String EXPENSE = "EXPENSE";

    public Optional<BigDecimal> amountChange(String transactionType, BigDecimal amount) {
        if (transactionType == null || amount == null) {
            return Optional.empty();
        }

        if (INCOME.equals(transactionType)) {
            return Optional.of(amount);
        }

        if (EXPENSE.equals(transactionType)) {
            return Optional.of(amount.negate());
        }

        return Optional.empty();
    }
}

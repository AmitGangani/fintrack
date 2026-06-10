package com.amit.fintrack.account.application.model;

import com.amit.fintrack.account.domain.AccountType;

import java.math.BigDecimal;

public record AccountCommand(
        String name,
        AccountType type,
        BigDecimal balance,
        String currency
) {
}

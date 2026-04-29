package com.amit.fintrack.account.dto;

import com.amit.fintrack.account.entity.AccountType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record AccountRequest(

        @NotBlank(message = "Account name is required")
        String name,

        @NotNull(message = "Account type is required")
        AccountType type,

        @NotNull(message = "Balance is required")
        @DecimalMin(value = "0.0", message = "Balance cannot be negative")
        BigDecimal balance,

        @NotBlank(message = "Currency is required")
        @Size(min = 3, max = 3, message = "Currency must be 3 characters like INR or USD")
        String currency
) {
}
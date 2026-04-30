package com.amit.fintrack.account.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record BalanceAdjustmentRequest(

        @NotNull(message = "Amount change is required")
        BigDecimal amountChange
) {
}
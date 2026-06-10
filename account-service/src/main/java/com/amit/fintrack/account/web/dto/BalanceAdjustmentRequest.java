package com.amit.fintrack.account.web.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record BalanceAdjustmentRequest(

        @NotNull(message = "Amount change is required")
        BigDecimal amountChange
) {
}
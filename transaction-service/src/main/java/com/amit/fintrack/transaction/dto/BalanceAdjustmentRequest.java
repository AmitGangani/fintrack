package com.amit.fintrack.transaction.dto;

import java.math.BigDecimal;

public record BalanceAdjustmentRequest(
        BigDecimal amountChange
) {
}
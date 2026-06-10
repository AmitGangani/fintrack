package com.amit.fintrack.account.application.model;

import java.math.BigDecimal;
import java.util.UUID;

public record TransactionBalanceEvent(
        UUID eventId,
        TransactionEventType eventType,
        UUID userId,
        UUID oldAccountId,
        UUID newAccountId,
        String oldType,
        BigDecimal oldAmount,
        String newType,
        BigDecimal newAmount
) {
}

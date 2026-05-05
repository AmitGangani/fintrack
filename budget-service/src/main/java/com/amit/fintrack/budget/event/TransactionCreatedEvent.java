package com.amit.fintrack.budget.event;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransactionCreatedEvent(
        UUID eventId,
        UUID transactionId,
        UUID userId,
        UUID accountId,
        String type,
        String category,
        BigDecimal amount,
        LocalDate transactionDate,
        LocalDateTime occurredAt
) {
}

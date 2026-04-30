package com.amit.fintrack.transaction.dto;

import com.amit.fintrack.transaction.entity.TransactionCategory;
import com.amit.fintrack.transaction.entity.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record TransactionRequest(

        @NotNull(message = "Account id is required")
        UUID accountId,

        @NotNull(message = "Transaction type is required")
        TransactionType type,

        @NotNull(message = "Category is required")
        TransactionCategory category,

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
        BigDecimal amount,

        @Size(max = 255, message = "Description cannot exceed 255 characters")
        String description,

        @NotNull(message = "Transaction date is required")
        LocalDate transactionDate
) {
}
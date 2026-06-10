package com.amit.fintrack.transaction.application.port;

import com.amit.fintrack.transaction.application.model.TransactionCommand;
import com.amit.fintrack.transaction.application.model.TransactionView;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionStore {

    TransactionView create(UUID userId, TransactionCommand command);

    List<TransactionView> findByUserId(UUID userId);

    Optional<TransactionView> findByIdAndUserId(UUID transactionId, UUID userId);

    List<TransactionView> findByUserIdAndDateRange(UUID userId, LocalDate startDate, LocalDate endDate);

    TransactionView update(UUID transactionId, UUID userId, TransactionCommand command);

    void delete(UUID transactionId, UUID userId);
}

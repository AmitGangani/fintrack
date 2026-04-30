package com.amit.fintrack.transaction.repository;

import com.amit.fintrack.transaction.entity.FinancialTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<FinancialTransaction, UUID> {

    List<FinancialTransaction> findByUserIdOrderByTransactionDateDesc(UUID userId);

    Optional<FinancialTransaction> findByIdAndUserId(UUID id, UUID userId);

    List<FinancialTransaction> findByUserIdAndTransactionDateBetweenOrderByTransactionDateDesc(
            UUID userId,
            LocalDate startDate,
            LocalDate endDate
    );
}
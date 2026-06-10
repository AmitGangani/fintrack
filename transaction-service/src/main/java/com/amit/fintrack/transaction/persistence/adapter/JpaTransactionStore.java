package com.amit.fintrack.transaction.persistence.adapter;

import com.amit.fintrack.transaction.application.model.TransactionCommand;
import com.amit.fintrack.transaction.application.model.TransactionView;
import com.amit.fintrack.transaction.application.port.TransactionStore;
import com.amit.fintrack.transaction.exception.TransactionNotFoundException;
import com.amit.fintrack.transaction.persistence.entity.FinancialTransaction;
import com.amit.fintrack.transaction.persistence.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JpaTransactionStore implements TransactionStore {

    private final TransactionRepository transactionRepository;

    @Override
    public TransactionView create(UUID userId, TransactionCommand command) {
        FinancialTransaction transaction = FinancialTransaction.builder()
                .userId(userId)
                .accountId(command.accountId())
                .type(command.type())
                .category(command.category())
                .amount(command.amount())
                .description(command.description())
                .transactionDate(command.transactionDate())
                .createdAt(LocalDateTime.now())
                .build();

        return toView(transactionRepository.save(transaction));
    }

    @Override
    public List<TransactionView> findByUserId(UUID userId) {
        return transactionRepository.findByUserIdOrderByTransactionDateDesc(userId)
                .stream()
                .map(this::toView)
                .toList();
    }

    @Override
    public Optional<TransactionView> findByIdAndUserId(UUID transactionId, UUID userId) {
        return transactionRepository.findByIdAndUserId(transactionId, userId).map(this::toView);
    }

    @Override
    public List<TransactionView> findByUserIdAndDateRange(UUID userId, LocalDate startDate, LocalDate endDate) {
        return transactionRepository.findByUserIdAndTransactionDateBetweenOrderByTransactionDateDesc(
                        userId,
                        startDate,
                        endDate
                )
                .stream()
                .map(this::toView)
                .toList();
    }

    @Override
    public TransactionView update(UUID transactionId, UUID userId, TransactionCommand command) {
        FinancialTransaction transaction = findTransaction(transactionId, userId);
        transaction.setAccountId(command.accountId());
        transaction.setType(command.type());
        transaction.setCategory(command.category());
        transaction.setAmount(command.amount());
        transaction.setDescription(command.description());
        transaction.setTransactionDate(command.transactionDate());

        return toView(transactionRepository.save(transaction));
    }

    @Override
    public void delete(UUID transactionId, UUID userId) {
        transactionRepository.delete(findTransaction(transactionId, userId));
    }

    private FinancialTransaction findTransaction(UUID transactionId, UUID userId) {
        return transactionRepository.findByIdAndUserId(transactionId, userId)
                .orElseThrow(() -> new TransactionNotFoundException("Transaction not found"));
    }

    private TransactionView toView(FinancialTransaction transaction) {
        return new TransactionView(
                transaction.getId(),
                transaction.getUserId(),
                transaction.getAccountId(),
                transaction.getType(),
                transaction.getCategory(),
                transaction.getAmount(),
                transaction.getDescription(),
                transaction.getTransactionDate(),
                transaction.getCreatedAt()
        );
    }
}

package com.amit.fintrack.transaction.service;

import com.amit.fintrack.transaction.dto.CategoryExpenseSummaryResponse;
import com.amit.fintrack.transaction.dto.MonthlySummaryResponse;
import com.amit.fintrack.transaction.dto.TransactionRequest;
import com.amit.fintrack.transaction.dto.TransactionResponse;
import com.amit.fintrack.transaction.entity.FinancialTransaction;
import com.amit.fintrack.transaction.entity.TransactionType;
import com.amit.fintrack.transaction.event.TransactionBudgetEvent;
import com.amit.fintrack.transaction.event.TransactionEventType;
import com.amit.fintrack.transaction.exception.TransactionNotFoundException;
import com.amit.fintrack.transaction.repository.TransactionRepository;
import com.amit.fintrack.transaction.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CurrentUserService currentUserService;
    private final OutboxService outboxService;

    @Transactional
    public TransactionResponse createTransaction(TransactionRequest request) {
        UUID currentUserId = currentUserService.getCurrentUserId();

        FinancialTransaction transaction = FinancialTransaction.builder()
                .userId(currentUserId)
                .accountId(request.accountId())
                .type(request.type())
                .category(request.category())
                .amount(request.amount())
                .description(request.description())
                .transactionDate(request.transactionDate())
                .createdAt(LocalDateTime.now())
                .build();

        FinancialTransaction savedTransaction = transactionRepository.save(transaction);

        TransactionBudgetEvent event = new TransactionBudgetEvent(
                UUID.randomUUID(),
                TransactionEventType.CREATED,
                savedTransaction.getId(),
                savedTransaction.getUserId(),

                null,
                savedTransaction.getAccountId(),

                null,
                null,
                null,
                null,

                savedTransaction.getType(),
                savedTransaction.getCategory(),
                savedTransaction.getAmount(),
                savedTransaction.getTransactionDate(),

                LocalDateTime.now()
        );

        outboxService.saveTransactionEvent(event);

        return toResponse(savedTransaction);
    }

    public List<TransactionResponse> getMyTransactions() {
        UUID currentUserId = currentUserService.getCurrentUserId();

        return transactionRepository.findByUserIdOrderByTransactionDateDesc(currentUserId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public TransactionResponse getTransactionById(UUID transactionId) {
        UUID currentUserId = currentUserService.getCurrentUserId();

        FinancialTransaction transaction = transactionRepository.findByIdAndUserId(
                        transactionId,
                        currentUserId
                )
                .orElseThrow(() -> new TransactionNotFoundException("Transaction not found"));

        return toResponse(transaction);
    }

    public List<TransactionResponse> getMonthlyTransactions(int year, int month) {
        UUID currentUserId = currentUserService.getCurrentUserId();

        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        return transactionRepository
                .findByUserIdAndTransactionDateBetweenOrderByTransactionDateDesc(
                        currentUserId,
                        startDate,
                        endDate
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public MonthlySummaryResponse getMonthlySummary(int year, int month) {
        UUID currentUserId = currentUserService.getCurrentUserId();

        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        List<FinancialTransaction> transactions = transactionRepository
                .findByUserIdAndTransactionDateBetweenOrderByTransactionDateDesc(
                        currentUserId,
                        startDate,
                        endDate
                );

        BigDecimal totalIncome = transactions.stream()
                .filter(transaction -> transaction.getType() == TransactionType.INCOME)
                .map(FinancialTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpense = transactions.stream()
                .filter(transaction -> transaction.getType() == TransactionType.EXPENSE)
                .map(FinancialTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal netSavings = totalIncome.subtract(totalExpense);

        return new MonthlySummaryResponse(
                year,
                month,
                totalIncome,
                totalExpense,
                netSavings,
                transactions.size()
        );
    }

    public List<CategoryExpenseSummaryResponse> getCategoryExpenseSummary(int year, int month) {
        UUID currentUserId = currentUserService.getCurrentUserId();

        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        List<FinancialTransaction> transactions = transactionRepository
                .findByUserIdAndTransactionDateBetweenOrderByTransactionDateDesc(
                        currentUserId,
                        startDate,
                        endDate
                );

        Map<?, BigDecimal> categoryTotals = transactions.stream()
                .filter(transaction -> transaction.getType() == TransactionType.EXPENSE)
                .collect(Collectors.groupingBy(
                        FinancialTransaction::getCategory,
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                FinancialTransaction::getAmount,
                                BigDecimal::add
                        )
                ));

        return categoryTotals.entrySet()
                .stream()
                .map(entry -> new CategoryExpenseSummaryResponse(
                        (com.amit.fintrack.transaction.entity.TransactionCategory) entry.getKey(),
                        entry.getValue()
                ))
                .toList();
    }

    @Transactional
    public TransactionResponse updateTransaction(
            UUID transactionId,
            TransactionRequest request
    ) {
        UUID currentUserId = currentUserService.getCurrentUserId();

        FinancialTransaction transaction = transactionRepository.findByIdAndUserId(
                        transactionId,
                        currentUserId
                )
                .orElseThrow(() -> new TransactionNotFoundException("Transaction not found"));

        UUID oldAccountId = transaction.getAccountId();
        BigDecimal oldAmountChange = calculateAmountChange(
                transaction.getType(),
                transaction.getAmount()
        );

        TransactionType oldType = transaction.getType();
        var oldCategory = transaction.getCategory();
        BigDecimal oldAmount = transaction.getAmount();
        var oldTransactionDate = transaction.getTransactionDate();

        UUID newAccountId = request.accountId();
        BigDecimal newAmountChange = calculateAmountChange(
                request.type(),
                request.amount()
        );

        transaction.setAccountId(request.accountId());
        transaction.setType(request.type());
        transaction.setCategory(request.category());
        transaction.setAmount(request.amount());
        transaction.setDescription(request.description());
        transaction.setTransactionDate(request.transactionDate());

        FinancialTransaction updatedTransaction = transactionRepository.save(transaction);

        TransactionBudgetEvent event = new TransactionBudgetEvent(
                UUID.randomUUID(),
                TransactionEventType.UPDATED,
                updatedTransaction.getId(),
                updatedTransaction.getUserId(),

                oldAccountId,
                updatedTransaction.getAccountId(),

                oldType,
                oldCategory,
                oldAmount,
                oldTransactionDate,

                updatedTransaction.getType(),
                updatedTransaction.getCategory(),
                updatedTransaction.getAmount(),
                updatedTransaction.getTransactionDate(),

                LocalDateTime.now()
        );

        outboxService.saveTransactionEvent(event);

        return toResponse(updatedTransaction);
    }

    @Transactional
    public void deleteTransaction(UUID transactionId) {
        UUID currentUserId = currentUserService.getCurrentUserId();

        FinancialTransaction transaction = transactionRepository.findByIdAndUserId(
                        transactionId,
                        currentUserId
                )
                .orElseThrow(() -> new TransactionNotFoundException("Transaction not found"));

        TransactionBudgetEvent event = new TransactionBudgetEvent(
                UUID.randomUUID(),
                TransactionEventType.DELETED,
                transaction.getId(),
                transaction.getUserId(),

                transaction.getAccountId(),
                null,

                transaction.getType(),
                transaction.getCategory(),
                transaction.getAmount(),
                transaction.getTransactionDate(),

                null,
                null,
                null,
                null,

                LocalDateTime.now()
        );

        outboxService.saveTransactionEvent(event);

        transactionRepository.delete(transaction);
    }

    private BigDecimal calculateAmountChange(TransactionType type, BigDecimal amount) {
        if (type == TransactionType.INCOME) {
            return amount;
        }

        return amount.negate();
    }

    private TransactionResponse toResponse(FinancialTransaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
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
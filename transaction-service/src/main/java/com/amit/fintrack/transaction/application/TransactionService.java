package com.amit.fintrack.transaction.application;

import com.amit.fintrack.transaction.application.model.TransactionCommand;
import com.amit.fintrack.transaction.application.model.TransactionEventType;
import com.amit.fintrack.transaction.application.model.TransactionLifecycleEvent;
import com.amit.fintrack.transaction.application.model.TransactionView;
import com.amit.fintrack.transaction.application.port.TransactionEventOutbox;
import com.amit.fintrack.transaction.application.port.TransactionStore;
import com.amit.fintrack.transaction.domain.CategoryExpenseSummary;
import com.amit.fintrack.transaction.domain.DateRange;
import com.amit.fintrack.transaction.domain.MonthlySummary;
import com.amit.fintrack.transaction.domain.TransactionSummaryCalculator;
import com.amit.fintrack.transaction.exception.TransactionNotFoundException;
import com.amit.fintrack.transaction.application.port.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionStore transactionStore;
    private final CurrentUserProvider currentUserProvider;
    private final TransactionEventOutbox transactionEventOutbox;
    private final TransactionSummaryCalculator summaryCalculator;

    @Transactional
    public TransactionView createTransaction(TransactionCommand command) {
        TransactionView savedTransaction = transactionStore.create(currentUserProvider.getCurrentUserId(), command);
        transactionEventOutbox.save(created(savedTransaction));

        return savedTransaction;
    }

    public List<TransactionView> getMyTransactions() {
        return transactionStore.findByUserId(currentUserProvider.getCurrentUserId());
    }

    public TransactionView getTransactionById(UUID transactionId) {
        return getCurrentUserTransaction(transactionId);
    }

    public List<TransactionView> getMonthlyTransactions(int year, int month) {
        return getCurrentUserMonthlyTransactions(year, month);
    }

    public MonthlySummary getMonthlySummary(int year, int month) {
        return summaryCalculator.monthlySummary(
                year,
                month,
                getCurrentUserMonthlyTransactions(year, month).stream()
                        .map(TransactionView::toSummaryItem)
                        .toList()
        );
    }

    public List<CategoryExpenseSummary> getCategoryExpenseSummary(int year, int month) {
        return summaryCalculator.categoryExpenseSummary(
                getCurrentUserMonthlyTransactions(year, month).stream()
                        .map(TransactionView::toSummaryItem)
                        .toList()
        );
    }

    @Transactional
    public TransactionView updateTransaction(UUID transactionId, TransactionCommand command) {
        TransactionView oldTransaction = getCurrentUserTransaction(transactionId);
        TransactionView updatedTransaction = transactionStore.update(
                transactionId,
                currentUserProvider.getCurrentUserId(),
                command
        );

        transactionEventOutbox.save(updated(oldTransaction, updatedTransaction));

        return updatedTransaction;
    }

    @Transactional
    public void deleteTransaction(UUID transactionId) {
        TransactionView transaction = getCurrentUserTransaction(transactionId);

        transactionEventOutbox.save(deleted(transaction));
        transactionStore.delete(transactionId, currentUserProvider.getCurrentUserId());
    }

    private TransactionView getCurrentUserTransaction(UUID transactionId) {
        return transactionStore.findByIdAndUserId(transactionId, currentUserProvider.getCurrentUserId())
                .orElseThrow(() -> new TransactionNotFoundException("Transaction not found"));
    }

    private List<TransactionView> getCurrentUserMonthlyTransactions(int year, int month) {
        DateRange range = DateRange.ofMonth(year, month);

        return transactionStore.findByUserIdAndDateRange(
                currentUserProvider.getCurrentUserId(),
                range.startDate(),
                range.endDate()
        );
    }

    private TransactionLifecycleEvent created(TransactionView transaction) {
        return new TransactionLifecycleEvent(
                UUID.randomUUID(),
                TransactionEventType.CREATED,
                transaction.id(),
                transaction.userId(),
                null,
                transaction.accountId(),
                null,
                null,
                null,
                null,
                transaction.type(),
                transaction.category(),
                transaction.amount(),
                transaction.transactionDate(),
                LocalDateTime.now()
        );
    }

    private TransactionLifecycleEvent updated(TransactionView oldTransaction, TransactionView newTransaction) {
        return new TransactionLifecycleEvent(
                UUID.randomUUID(),
                TransactionEventType.UPDATED,
                newTransaction.id(),
                newTransaction.userId(),
                oldTransaction.accountId(),
                newTransaction.accountId(),
                oldTransaction.type(),
                oldTransaction.category(),
                oldTransaction.amount(),
                oldTransaction.transactionDate(),
                newTransaction.type(),
                newTransaction.category(),
                newTransaction.amount(),
                newTransaction.transactionDate(),
                LocalDateTime.now()
        );
    }

    private TransactionLifecycleEvent deleted(TransactionView transaction) {
        return new TransactionLifecycleEvent(
                UUID.randomUUID(),
                TransactionEventType.DELETED,
                transaction.id(),
                transaction.userId(),
                transaction.accountId(),
                null,
                transaction.type(),
                transaction.category(),
                transaction.amount(),
                transaction.transactionDate(),
                null,
                null,
                null,
                null,
                LocalDateTime.now()
        );
    }
}

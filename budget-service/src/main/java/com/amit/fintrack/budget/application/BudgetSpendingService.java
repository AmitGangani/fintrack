package com.amit.fintrack.budget.application;

import com.amit.fintrack.budget.application.model.BudgetAlertEvent;
import com.amit.fintrack.budget.application.model.BudgetRecord;
import com.amit.fintrack.budget.application.model.BudgetSpendingRecord;
import com.amit.fintrack.budget.application.model.TransactionBudgetEvent;
import com.amit.fintrack.budget.application.port.BudgetAlertHistoryStore;
import com.amit.fintrack.budget.application.port.BudgetAlertOutbox;
import com.amit.fintrack.budget.application.port.BudgetSpendingStore;
import com.amit.fintrack.budget.application.port.BudgetStore;
import com.amit.fintrack.budget.application.port.ProcessedEventStore;
import com.amit.fintrack.budget.domain.BudgetAlertDecision;
import com.amit.fintrack.budget.domain.BudgetAlertPolicy;
import com.amit.fintrack.budget.domain.BudgetCategory;
import com.amit.fintrack.budget.domain.BudgetCategoryResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BudgetSpendingService {

    private static final String EXPENSE_TYPE = "EXPENSE";

    private final BudgetSpendingStore budgetSpendingStore;
    private final ProcessedEventStore processedEventStore;
    private final BudgetStore budgetStore;
    private final BudgetAlertHistoryStore budgetAlertHistoryStore;
    private final BudgetAlertOutbox budgetAlertOutbox;
    private final BudgetAlertPolicy budgetAlertPolicy;
    private final BudgetCategoryResolver budgetCategoryResolver;

    @Transactional
    public void handleTransactionLifecycleEvent(TransactionBudgetEvent event) {
        if (isAlreadyProcessed(event)) {
            return;
        }

        switch (event.eventType()) {
            case CREATED -> applyExpense(
                    event.userId(),
                    event.newCategory(),
                    event.newType(),
                    event.newTransactionDate(),
                    event.newAmount()
            );
            case UPDATED -> {
                applyExpense(
                        event.userId(),
                        event.oldCategory(),
                        event.oldType(),
                        event.oldTransactionDate(),
                        event.oldAmount() == null ? null : event.oldAmount().negate()
                );
                applyExpense(
                        event.userId(),
                        event.newCategory(),
                        event.newType(),
                        event.newTransactionDate(),
                        event.newAmount()
                );
            }
            case DELETED -> applyExpense(
                    event.userId(),
                    event.oldCategory(),
                    event.oldType(),
                    event.oldTransactionDate(),
                    event.oldAmount() == null ? null : event.oldAmount().negate()
            );
        }

        processedEventStore.markProcessed(event.eventId(), "TRANSACTION_" + event.eventType().name());
        log.info("Processed budget event: {}", event.eventId());
    }

    private void applyExpense(
            UUID userId,
            String categoryName,
            String transactionType,
            LocalDate transactionDate,
            BigDecimal amountChange
    ) {
        if (!EXPENSE_TYPE.equals(transactionType)) {
            return;
        }

        if (transactionDate == null || amountChange == null) {
            log.warn("Skipping malformed expense event. userId={}, category={}", userId, categoryName);
            return;
        }

        Optional<BudgetCategory> category = budgetCategoryResolver.resolve(categoryName);
        if (category.isEmpty()) {
            log.warn("Skipping unsupported budget category from transaction event: {}", categoryName);
            return;
        }

        adjustSpending(userId, category.get(), transactionDate, amountChange);
    }

    private void adjustSpending(
            UUID userId,
            BudgetCategory category,
            LocalDate transactionDate,
            BigDecimal amountChange
    ) {
        int month = transactionDate.getMonthValue();
        int year = transactionDate.getYear();

        BigDecimal currentSpentAmount = budgetSpendingStore
                .findByUserIdAndCategoryAndYearAndMonth(userId, category, year, month)
                .map(BudgetSpendingRecord::spentAmount)
                .orElse(BigDecimal.ZERO);
        BigDecimal newSpentAmount = zeroFloor(currentSpentAmount.add(amountChange));

        BudgetSpendingRecord savedSpending = budgetSpendingStore.saveSpending(
                userId,
                category,
                year,
                month,
                newSpentAmount
        );
        checkAndStoreBudgetAlert(savedSpending);

        log.info(
                "Budget spending adjusted. userId={}, category={}, year={}, month={}, change={}, newSpent={}",
                userId,
                category,
                year,
                month,
                amountChange,
                newSpentAmount
        );
    }

    private void checkAndStoreBudgetAlert(BudgetSpendingRecord spending) {
        budgetStore.findByUserIdAndCategoryAndYearAndMonth(
                spending.userId(),
                spending.category(),
                spending.year(),
                spending.month()
        ).ifPresent(budget -> storeAlertIfNeeded(spending, budget));
    }

    private void storeAlertIfNeeded(BudgetSpendingRecord spending, BudgetRecord budget) {
        budgetAlertPolicy.evaluate(spending.category(), spending.spentAmount(), budget.limitAmount())
                .filter(decision -> !alertAlreadySent(spending, decision))
                .ifPresent(decision -> storeAlert(spending, budget, decision));
    }

    private boolean alertAlreadySent(BudgetSpendingRecord spending, BudgetAlertDecision decision) {
        return budgetAlertHistoryStore.exists(
                spending.userId(),
                spending.category(),
                spending.year(),
                spending.month(),
                decision.type().name()
        );
    }

    private void storeAlert(BudgetSpendingRecord spending, BudgetRecord budget, BudgetAlertDecision decision) {
        BudgetAlertEvent event = new BudgetAlertEvent(
                UUID.randomUUID(),
                spending.userId(),
                spending.category(),
                spending.month(),
                spending.year(),
                budget.limitAmount(),
                spending.spentAmount(),
                decision.percentageUsed(),
                decision.type().name(),
                decision.message(),
                LocalDateTime.now()
        );

        budgetAlertOutbox.save(event);
        budgetAlertHistoryStore.save(
                spending.userId(),
                spending.category(),
                spending.year(),
                spending.month(),
                decision.type().name()
        );
    }

    private BigDecimal zeroFloor(BigDecimal amount) {
        return amount.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : amount;
    }

    private boolean isAlreadyProcessed(TransactionBudgetEvent event) {
        if (processedEventStore.exists(event.eventId())) {
            log.info("Skipping duplicate budget event: {}", event.eventId());
            return true;
        }

        return false;
    }
}

package com.amit.fintrack.budget.service;

import com.amit.fintrack.budget.entity.*;
import com.amit.fintrack.budget.event.BudgetAlertEvent;
import com.amit.fintrack.budget.event.BudgetAlertProducer;
import com.amit.fintrack.budget.event.TransactionLifecycleEvent;
import com.amit.fintrack.budget.event.TransactionEventType;
import com.amit.fintrack.budget.repository.BudgetAlertHistoryRepository;
import com.amit.fintrack.budget.repository.BudgetRepository;
import com.amit.fintrack.budget.repository.BudgetSpendingRepository;
import com.amit.fintrack.budget.repository.ProcessedKafkaEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BudgetSpendingService {

    private final BudgetSpendingRepository budgetSpendingRepository;
    private final ProcessedKafkaEventRepository processedKafkaEventRepository;
    private final BudgetRepository budgetRepository;
    private final BudgetAlertHistoryRepository budgetAlertHistoryRepository;
    private final BudgetAlertProducer budgetAlertProducer;

    @Transactional
    public void handleTransactionLifecycleEvent(TransactionLifecycleEvent event) {

        if (processedKafkaEventRepository.existsById(event.eventId())) {
            log.info("Skipping duplicate event: {}", event.eventId());
            return;
        }

        if (event.eventType() == TransactionEventType.CREATED) {
            applyNewExpense(event);
        } else if (event.eventType() == TransactionEventType.UPDATED) {
            reverseOldExpense(event);
            applyNewExpense(event);
        } else if (event.eventType() == TransactionEventType.DELETED) {
            reverseOldExpense(event);
        }

        markEventAsProcessed(event);

        log.info("Processed transaction lifecycle event: {}", event.eventId());
    }

    private void applyNewExpense(TransactionLifecycleEvent event) {
        if (!"EXPENSE".equals(event.newType())) {
            return;
        }

        adjustSpending(
                event.userId(),
                event.newCategory(),
                event.newTransactionDate(),
                event.newAmount()
        );
    }

    private void reverseOldExpense(TransactionLifecycleEvent event) {
        if (!"EXPENSE".equals(event.oldType())) {
            return;
        }

        adjustSpending(
                event.userId(),
                event.oldCategory(),
                event.oldTransactionDate(),
                event.oldAmount().negate()
        );
    }

    private void adjustSpending(
            java.util.UUID userId,
            BudgetCategory category,
            LocalDate transactionDate,
            BigDecimal amountChange
    ) {
        int month = transactionDate.getMonthValue();
        int year = transactionDate.getYear();

        BudgetSpending spending = budgetSpendingRepository
                .findByUserIdAndCategoryAndYearAndMonth(
                        userId,
                        category,
                        year,
                        month
                )
                .orElseGet(() -> BudgetSpending.builder()
                        .userId(userId)
                        .category(category)
                        .year(year)
                        .month(month)
                        .spentAmount(BigDecimal.ZERO)
                        .updatedAt(LocalDateTime.now())
                        .build()
                );

        BigDecimal newSpentAmount = spending.getSpentAmount().add(amountChange);

        if (newSpentAmount.compareTo(BigDecimal.ZERO) < 0) {
            newSpentAmount = BigDecimal.ZERO;
        }

        spending.setSpentAmount(newSpentAmount);
        spending.setUpdatedAt(LocalDateTime.now());

        budgetSpendingRepository.save(spending);
        checkAndPublishBudgetAlert(spending);

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

    private void checkAndPublishBudgetAlert(BudgetSpending spending) {
        Optional<Budget> optionalBudget = budgetRepository.findByUserIdAndCategoryAndYearAndMonth(
                spending.getUserId(),
                spending.getCategory(),
                spending.getYear(),
                spending.getMonth()
        );

        if (optionalBudget.isEmpty()) {
            return;
        }

        Budget budget = optionalBudget.get();

        BigDecimal percentageUsed = spending.getSpentAmount()
                .multiply(BigDecimal.valueOf(100))
                .divide(budget.getLimitAmount(), 2, RoundingMode.HALF_UP);

        String alertType = resolveAlertType(percentageUsed);

        if (alertType == null) {
            return;
        }

        boolean alreadySent = budgetAlertHistoryRepository
                .existsByUserIdAndCategoryAndYearAndMonthAndAlertType(
                        spending.getUserId(),
                        spending.getCategory(),
                        spending.getYear(),
                        spending.getMonth(),
                        alertType
                );

        if (alreadySent) {
            return;
        }

        BudgetAlertEvent event = new BudgetAlertEvent(
                UUID.randomUUID(),
                spending.getUserId(),
                spending.getCategory(),
                spending.getMonth(),
                spending.getYear(),
                budget.getLimitAmount(),
                spending.getSpentAmount(),
                percentageUsed,
                alertType,
                buildAlertMessage(spending, budget, percentageUsed, alertType),
                LocalDateTime.now()
        );

        budgetAlertProducer.publish(event);

        BudgetAlertHistory history = BudgetAlertHistory.builder()
                .userId(spending.getUserId())
                .category(spending.getCategory())
                .month(spending.getMonth())
                .year(spending.getYear())
                .alertType(alertType)
                .createdAt(LocalDateTime.now())
                .build();

        budgetAlertHistoryRepository.save(history);
    }

    private String resolveAlertType(BigDecimal percentageUsed) {
        if (percentageUsed.compareTo(BigDecimal.valueOf(100)) >= 0) {
            return "BUDGET_EXCEEDED";
        }

        if (percentageUsed.compareTo(BigDecimal.valueOf(80)) >= 0) {
            return "BUDGET_WARNING";
        }

        return null;
    }

    private String buildAlertMessage(
            BudgetSpending spending,
            Budget budget,
            BigDecimal percentageUsed,
            String alertType
    ) {
        if ("BUDGET_EXCEEDED".equals(alertType)) {
            BigDecimal exceededBy = spending.getSpentAmount().subtract(budget.getLimitAmount());

            return "You exceeded your "
                    + spending.getCategory()
                    + " budget by ₹"
                    + exceededBy
                    + ". Spent ₹"
                    + spending.getSpentAmount()
                    + " out of ₹"
                    + budget.getLimitAmount()
                    + ".";
        }

        return "You have used "
                + percentageUsed
                + "% of your "
                + spending.getCategory()
                + " budget. Spent ₹"
                + spending.getSpentAmount()
                + " out of ₹"
                + budget.getLimitAmount()
                + ".";
    }

    private void markEventAsProcessed(TransactionLifecycleEvent event) {
        ProcessedKafkaEvent processedEvent = ProcessedKafkaEvent.builder()
                .eventId(event.eventId())
                .eventType("TRANSACTION_" + event.eventType().name())
                .processedAt(LocalDateTime.now())
                .build();

        processedKafkaEventRepository.save(processedEvent);
    }
}

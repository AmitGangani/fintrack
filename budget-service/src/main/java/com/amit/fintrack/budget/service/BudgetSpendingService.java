package com.amit.fintrack.budget.service;

import com.amit.fintrack.budget.entity.BudgetCategory;
import com.amit.fintrack.budget.entity.BudgetSpending;
import com.amit.fintrack.budget.entity.ProcessedKafkaEvent;
import com.amit.fintrack.budget.event.TransactionBudgetEvent;
import com.amit.fintrack.budget.event.TransactionEventType;
import com.amit.fintrack.budget.repository.BudgetSpendingRepository;
import com.amit.fintrack.budget.repository.ProcessedKafkaEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class BudgetSpendingService {

    private final BudgetSpendingRepository budgetSpendingRepository;
    private final ProcessedKafkaEventRepository processedKafkaEventRepository;

    @Transactional
    public void handleTransactionBudgetEvent(TransactionBudgetEvent event) {

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

        log.info("Processed transaction budget event: {}", event.eventId());
    }

    private void applyNewExpense(TransactionBudgetEvent event) {
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

    private void reverseOldExpense(TransactionBudgetEvent event) {
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

    private void markEventAsProcessed(TransactionBudgetEvent event) {
        ProcessedKafkaEvent processedEvent = ProcessedKafkaEvent.builder()
                .eventId(event.eventId())
                .eventType("TRANSACTION_" + event.eventType().name())
                .processedAt(LocalDateTime.now())
                .build();

        processedKafkaEventRepository.save(processedEvent);
    }
}
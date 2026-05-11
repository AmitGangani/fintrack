package com.amit.fintrack.analytics.service;

import com.amit.fintrack.analytics.entity.CategoryExpenseAnalytics;
import com.amit.fintrack.analytics.entity.MonthlyAnalytics;
import com.amit.fintrack.analytics.entity.ProcessedKafkaEvent;
import com.amit.fintrack.analytics.event.TransactionEventType;
import com.amit.fintrack.analytics.event.TransactionLifecycleEvent;
import com.amit.fintrack.analytics.repository.CategoryExpenseAnalyticsRepository;
import com.amit.fintrack.analytics.repository.MonthlyAnalyticsRepository;
import com.amit.fintrack.analytics.repository.ProcessedKafkaEventRepository;
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
public class AnalyticsEventService {

    private final MonthlyAnalyticsRepository monthlyAnalyticsRepository;
    private final CategoryExpenseAnalyticsRepository categoryExpenseAnalyticsRepository;
    private final ProcessedKafkaEventRepository processedKafkaEventRepository;

    @Transactional
    public void handleTransactionEvent(TransactionLifecycleEvent event) {
        if (processedKafkaEventRepository.existsById(event.eventId())) {
            log.info("Skipping duplicate analytics event: {}", event.eventId());
            return;
        }

        if (event.eventType() == TransactionEventType.CREATED) {
            applyNewTransaction(event);
        } else if (event.eventType() == TransactionEventType.UPDATED) {
            reverseOldTransaction(event);
            applyNewTransaction(event);
        } else if (event.eventType() == TransactionEventType.DELETED) {
            reverseOldTransaction(event);
        }

        processedKafkaEventRepository.save(
                ProcessedKafkaEvent.builder()
                        .eventId(event.eventId())
                        .eventType("TRANSACTION_" + event.eventType().name())
                        .processedAt(LocalDateTime.now())
                        .build()
        );
    }

    private void applyNewTransaction(TransactionLifecycleEvent event) {
        if (event.newType() == null) {
            return;
        }

        applyAmount(
                event.userId(),
                event.newType(),
                event.newCategory(),
                event.newAmount(),
                event.newTransactionDate()
        );
    }

    private void reverseOldTransaction(TransactionLifecycleEvent event) {
        if (event.oldType() == null) {
            return;
        }

        applyAmount(
                event.userId(),
                event.oldType(),
                event.oldCategory(),
                event.oldAmount().negate(),
                event.oldTransactionDate()
        );
    }

    private void applyAmount(
            java.util.UUID userId,
            String type,
            String category,
            BigDecimal amount,
            LocalDate transactionDate
    ) {
        int year = transactionDate.getYear();
        int month = transactionDate.getMonthValue();

        MonthlyAnalytics monthlyAnalytics = monthlyAnalyticsRepository
                .findByUserIdAndYearAndMonth(userId, year, month)
                .orElseGet(() -> MonthlyAnalytics.builder()
                        .userId(userId)
                        .year(year)
                        .month(month)
                        .totalIncome(BigDecimal.ZERO)
                        .totalExpense(BigDecimal.ZERO)
                        .updatedAt(LocalDateTime.now())
                        .build());

        if ("INCOME".equals(type)) {
            monthlyAnalytics.setTotalIncome(
                    monthlyAnalytics.getTotalIncome().add(amount)
            );
        } else if ("EXPENSE".equals(type)) {
            monthlyAnalytics.setTotalExpense(
                    monthlyAnalytics.getTotalExpense().add(amount)
            );

            updateCategoryExpense(userId, category, year, month, amount);
        }

        if (monthlyAnalytics.getTotalIncome().compareTo(BigDecimal.ZERO) < 0) {
            monthlyAnalytics.setTotalIncome(BigDecimal.ZERO);
        }

        if (monthlyAnalytics.getTotalExpense().compareTo(BigDecimal.ZERO) < 0) {
            monthlyAnalytics.setTotalExpense(BigDecimal.ZERO);
        }

        monthlyAnalytics.setUpdatedAt(LocalDateTime.now());
        monthlyAnalyticsRepository.save(monthlyAnalytics);
    }

    private void updateCategoryExpense(
            java.util.UUID userId,
            String category,
            int year,
            int month,
            BigDecimal amountChange
    ) {
        CategoryExpenseAnalytics categoryAnalytics = categoryExpenseAnalyticsRepository
                .findByUserIdAndCategoryAndYearAndMonth(userId, category, year, month)
                .orElseGet(() -> CategoryExpenseAnalytics.builder()
                        .userId(userId)
                        .category(category)
                        .year(year)
                        .month(month)
                        .totalExpense(BigDecimal.ZERO)
                        .updatedAt(LocalDateTime.now())
                        .build());

        BigDecimal newTotal = categoryAnalytics.getTotalExpense().add(amountChange);

        if (newTotal.compareTo(BigDecimal.ZERO) < 0) {
            newTotal = BigDecimal.ZERO;
        }

        categoryAnalytics.setTotalExpense(newTotal);
        categoryAnalytics.setUpdatedAt(LocalDateTime.now());

        categoryExpenseAnalyticsRepository.save(categoryAnalytics);
    }
}
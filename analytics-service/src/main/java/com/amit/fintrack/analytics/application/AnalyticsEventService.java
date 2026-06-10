package com.amit.fintrack.analytics.application;

import com.amit.fintrack.analytics.application.model.CategoryExpenseAnalyticsRecord;
import com.amit.fintrack.analytics.application.model.MonthlyAnalyticsRecord;
import com.amit.fintrack.analytics.application.model.TransactionAnalyticsEvent;
import com.amit.fintrack.analytics.application.port.CategoryExpenseAnalyticsStore;
import com.amit.fintrack.analytics.application.port.MonthlyAnalyticsStore;
import com.amit.fintrack.analytics.application.port.ProcessedEventStore;
import com.amit.fintrack.analytics.domain.AnalyticsProjectionPolicy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsEventService {

    private final MonthlyAnalyticsStore monthlyAnalyticsStore;
    private final CategoryExpenseAnalyticsStore categoryExpenseAnalyticsStore;
    private final ProcessedEventStore processedEventStore;
    private final AnalyticsProjectionPolicy analyticsProjectionPolicy;

    @Transactional
    public void handleTransactionEvent(TransactionAnalyticsEvent event) {
        if (isAlreadyProcessed(event)) {
            return;
        }

        switch (event.eventType()) {
            case CREATED -> applyTransaction(
                    event.userId(),
                    event.newType(),
                    event.newCategory(),
                    event.newAmount(),
                    event.newTransactionDate()
            );
            case UPDATED -> {
                applyTransaction(
                        event.userId(),
                        event.oldType(),
                        event.oldCategory(),
                        event.oldAmount() == null ? null : event.oldAmount().negate(),
                        event.oldTransactionDate()
                );
                applyTransaction(
                        event.userId(),
                        event.newType(),
                        event.newCategory(),
                        event.newAmount(),
                        event.newTransactionDate()
                );
            }
            case DELETED -> applyTransaction(
                    event.userId(),
                    event.oldType(),
                    event.oldCategory(),
                    event.oldAmount() == null ? null : event.oldAmount().negate(),
                    event.oldTransactionDate()
            );
        }

        processedEventStore.markProcessed(event.eventId(), "TRANSACTION_" + event.eventType().name());
    }

    private void applyTransaction(
            UUID userId,
            String type,
            String category,
            BigDecimal amount,
            LocalDate transactionDate
    ) {
        if (type == null || amount == null || transactionDate == null) {
            log.warn("Skipping malformed analytics transaction event for userId={}", userId);
            return;
        }

        int year = transactionDate.getYear();
        int month = transactionDate.getMonthValue();
        MonthlyAnalyticsRecord monthlyAnalytics = monthlyAnalyticsStore
                .find(userId, year, month)
                .orElse(new MonthlyAnalyticsRecord(userId, year, month, BigDecimal.ZERO, BigDecimal.ZERO));

        BigDecimal totalIncome = monthlyAnalytics.totalIncome();
        BigDecimal totalExpense = monthlyAnalytics.totalExpense();

        if (analyticsProjectionPolicy.isIncome(type)) {
            totalIncome = analyticsProjectionPolicy.zeroFloor(totalIncome.add(amount));
        } else if (analyticsProjectionPolicy.isExpense(type)) {
            totalExpense = analyticsProjectionPolicy.zeroFloor(totalExpense.add(amount));
            updateCategoryExpense(userId, category, year, month, amount);
        } else {
            log.warn("Skipping unsupported analytics transaction type: {}", type);
            return;
        }

        monthlyAnalyticsStore.save(userId, year, month, totalIncome, totalExpense);
    }

    private void updateCategoryExpense(
            UUID userId,
            String category,
            int year,
            int month,
            BigDecimal amountChange
    ) {
        if (category == null || category.isBlank()) {
            return;
        }

        CategoryExpenseAnalyticsRecord categoryAnalytics = categoryExpenseAnalyticsStore
                .find(userId, category, year, month)
                .orElse(new CategoryExpenseAnalyticsRecord(userId, category, year, month, BigDecimal.ZERO));

        categoryExpenseAnalyticsStore.save(
                userId,
                category,
                year,
                month,
                analyticsProjectionPolicy.zeroFloor(categoryAnalytics.totalExpense().add(amountChange))
        );
    }

    private boolean isAlreadyProcessed(TransactionAnalyticsEvent event) {
        if (processedEventStore.exists(event.eventId())) {
            log.info("Skipping duplicate analytics event: {}", event.eventId());
            return true;
        }

        return false;
    }
}

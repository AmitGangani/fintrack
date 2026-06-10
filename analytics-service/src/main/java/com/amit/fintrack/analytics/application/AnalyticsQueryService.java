package com.amit.fintrack.analytics.application;

import com.amit.fintrack.analytics.application.model.CategoryExpenseAnalyticsView;
import com.amit.fintrack.analytics.application.model.MonthlyAnalyticsRecord;
import com.amit.fintrack.analytics.application.model.MonthlyAnalyticsView;
import com.amit.fintrack.analytics.application.port.CategoryExpenseAnalyticsStore;
import com.amit.fintrack.analytics.application.port.MonthlyAnalyticsStore;
import com.amit.fintrack.analytics.application.port.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AnalyticsQueryService {

    private final MonthlyAnalyticsStore monthlyAnalyticsStore;
    private final CategoryExpenseAnalyticsStore categoryExpenseAnalyticsStore;
    private final CurrentUserProvider currentUserProvider;

    public MonthlyAnalyticsView getMonthlyAnalytics(int year, int month) {
        UUID currentUserId = currentUserProvider.getCurrentUserId();
        MonthlyAnalyticsRecord analytics = monthlyAnalyticsStore.find(currentUserId, year, month)
                .orElse(new MonthlyAnalyticsRecord(currentUserId, year, month, BigDecimal.ZERO, BigDecimal.ZERO));

        return new MonthlyAnalyticsView(
                analytics.year(),
                analytics.month(),
                analytics.totalIncome(),
                analytics.totalExpense(),
                analytics.totalIncome().subtract(analytics.totalExpense())
        );
    }

    public List<CategoryExpenseAnalyticsView> getCategoryExpenses(int year, int month) {
        UUID currentUserId = currentUserProvider.getCurrentUserId();

        return categoryExpenseAnalyticsStore
                .findByUserIdAndYearAndMonth(currentUserId, year, month)
                .stream()
                .map(analytics -> new CategoryExpenseAnalyticsView(analytics.category(), analytics.totalExpense()))
                .toList();
    }
}

package com.amit.fintrack.analytics.service;

import com.amit.fintrack.analytics.dto.CategoryExpenseAnalyticsResponse;
import com.amit.fintrack.analytics.dto.MonthlyAnalyticsResponse;
import com.amit.fintrack.analytics.entity.MonthlyAnalytics;
import com.amit.fintrack.analytics.repository.CategoryExpenseAnalyticsRepository;
import com.amit.fintrack.analytics.repository.MonthlyAnalyticsRepository;
import com.amit.fintrack.analytics.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AnalyticsQueryService {

    private final MonthlyAnalyticsRepository monthlyAnalyticsRepository;
    private final CategoryExpenseAnalyticsRepository categoryExpenseAnalyticsRepository;
    private final CurrentUserService currentUserService;

    public MonthlyAnalyticsResponse getMonthlyAnalytics(int year, int month) {
        UUID currentUserId = currentUserService.getCurrentUserId();

        MonthlyAnalytics analytics = monthlyAnalyticsRepository
                .findByUserIdAndYearAndMonth(currentUserId, year, month)
                .orElseGet(() -> MonthlyAnalytics.builder()
                        .year(year)
                        .month(month)
                        .totalIncome(BigDecimal.ZERO)
                        .totalExpense(BigDecimal.ZERO)
                        .build());

        return new MonthlyAnalyticsResponse(
                year,
                month,
                analytics.getTotalIncome(),
                analytics.getTotalExpense(),
                analytics.getTotalIncome().subtract(analytics.getTotalExpense())
        );
    }

    public List<CategoryExpenseAnalyticsResponse> getCategoryExpenses(int year, int month) {
        UUID currentUserId = currentUserService.getCurrentUserId();

        return categoryExpenseAnalyticsRepository
                .findByUserIdAndYearAndMonthOrderByTotalExpenseDesc(
                        currentUserId,
                        year,
                        month
                )
                .stream()
                .map(item -> new CategoryExpenseAnalyticsResponse(
                        item.getCategory(),
                        item.getTotalExpense()
                ))
                .toList();
    }
}
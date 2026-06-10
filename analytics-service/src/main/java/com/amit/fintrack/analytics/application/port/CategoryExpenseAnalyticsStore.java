package com.amit.fintrack.analytics.application.port;

import com.amit.fintrack.analytics.application.model.CategoryExpenseAnalyticsRecord;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryExpenseAnalyticsStore {

    Optional<CategoryExpenseAnalyticsRecord> find(UUID userId, String category, int year, int month);

    List<CategoryExpenseAnalyticsRecord> findByUserIdAndYearAndMonth(UUID userId, int year, int month);

    CategoryExpenseAnalyticsRecord save(UUID userId, String category, int year, int month, BigDecimal totalExpense);
}

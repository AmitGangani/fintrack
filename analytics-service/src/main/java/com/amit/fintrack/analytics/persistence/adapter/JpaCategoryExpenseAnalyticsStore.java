package com.amit.fintrack.analytics.persistence.adapter;

import com.amit.fintrack.analytics.application.model.CategoryExpenseAnalyticsRecord;
import com.amit.fintrack.analytics.application.port.CategoryExpenseAnalyticsStore;
import com.amit.fintrack.analytics.persistence.entity.CategoryExpenseAnalytics;
import com.amit.fintrack.analytics.persistence.repository.CategoryExpenseAnalyticsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JpaCategoryExpenseAnalyticsStore implements CategoryExpenseAnalyticsStore {

    private final CategoryExpenseAnalyticsRepository categoryExpenseAnalyticsRepository;

    @Override
    public Optional<CategoryExpenseAnalyticsRecord> find(UUID userId, String category, int year, int month) {
        return categoryExpenseAnalyticsRepository.findByUserIdAndCategoryAndYearAndMonth(userId, category, year, month)
                .map(this::toRecord);
    }

    @Override
    public List<CategoryExpenseAnalyticsRecord> findByUserIdAndYearAndMonth(UUID userId, int year, int month) {
        return categoryExpenseAnalyticsRepository.findByUserIdAndYearAndMonthOrderByTotalExpenseDesc(userId, year, month)
                .stream()
                .map(this::toRecord)
                .toList();
    }

    @Override
    public CategoryExpenseAnalyticsRecord save(UUID userId, String category, int year, int month, BigDecimal totalExpense) {
        CategoryExpenseAnalytics analytics = categoryExpenseAnalyticsRepository
                .findByUserIdAndCategoryAndYearAndMonth(userId, category, year, month)
                .orElseGet(() -> CategoryExpenseAnalytics.builder()
                        .userId(userId)
                        .category(category)
                        .year(year)
                        .month(month)
                        .build());

        analytics.setTotalExpense(totalExpense);
        analytics.setUpdatedAt(LocalDateTime.now());

        return toRecord(categoryExpenseAnalyticsRepository.save(analytics));
    }

    private CategoryExpenseAnalyticsRecord toRecord(CategoryExpenseAnalytics analytics) {
        return new CategoryExpenseAnalyticsRecord(
                analytics.getUserId(),
                analytics.getCategory(),
                analytics.getYear(),
                analytics.getMonth(),
                analytics.getTotalExpense()
        );
    }
}

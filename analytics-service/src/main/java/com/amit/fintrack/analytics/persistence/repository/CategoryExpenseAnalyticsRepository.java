package com.amit.fintrack.analytics.persistence.repository;

import com.amit.fintrack.analytics.persistence.entity.CategoryExpenseAnalytics;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryExpenseAnalyticsRepository extends JpaRepository<CategoryExpenseAnalytics, UUID> {

    Optional<CategoryExpenseAnalytics> findByUserIdAndCategoryAndYearAndMonth(
            UUID userId,
            String category,
            int year,
            int month
    );

    List<CategoryExpenseAnalytics> findByUserIdAndYearAndMonthOrderByTotalExpenseDesc(
            UUID userId,
            int year,
            int month
    );
}
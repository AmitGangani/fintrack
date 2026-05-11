package com.amit.fintrack.analytics.repository;

import com.amit.fintrack.analytics.entity.MonthlyAnalytics;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MonthlyAnalyticsRepository extends JpaRepository<MonthlyAnalytics, UUID> {

    Optional<MonthlyAnalytics> findByUserIdAndYearAndMonth(
            UUID userId,
            int year,
            int month
    );
}
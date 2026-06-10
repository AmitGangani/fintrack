package com.amit.fintrack.analytics.persistence.adapter;

import com.amit.fintrack.analytics.application.model.MonthlyAnalyticsRecord;
import com.amit.fintrack.analytics.application.port.MonthlyAnalyticsStore;
import com.amit.fintrack.analytics.persistence.entity.MonthlyAnalytics;
import com.amit.fintrack.analytics.persistence.repository.MonthlyAnalyticsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JpaMonthlyAnalyticsStore implements MonthlyAnalyticsStore {

    private final MonthlyAnalyticsRepository monthlyAnalyticsRepository;

    @Override
    public Optional<MonthlyAnalyticsRecord> find(UUID userId, int year, int month) {
        return monthlyAnalyticsRepository.findByUserIdAndYearAndMonth(userId, year, month).map(this::toRecord);
    }

    @Override
    public MonthlyAnalyticsRecord save(UUID userId, int year, int month, BigDecimal totalIncome, BigDecimal totalExpense) {
        MonthlyAnalytics analytics = monthlyAnalyticsRepository.findByUserIdAndYearAndMonth(userId, year, month)
                .orElseGet(() -> MonthlyAnalytics.builder()
                        .userId(userId)
                        .year(year)
                        .month(month)
                        .build());

        analytics.setTotalIncome(totalIncome);
        analytics.setTotalExpense(totalExpense);
        analytics.setUpdatedAt(LocalDateTime.now());

        return toRecord(monthlyAnalyticsRepository.save(analytics));
    }

    private MonthlyAnalyticsRecord toRecord(MonthlyAnalytics analytics) {
        return new MonthlyAnalyticsRecord(
                analytics.getUserId(),
                analytics.getYear(),
                analytics.getMonth(),
                analytics.getTotalIncome(),
                analytics.getTotalExpense()
        );
    }
}

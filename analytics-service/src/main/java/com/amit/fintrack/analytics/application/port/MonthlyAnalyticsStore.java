package com.amit.fintrack.analytics.application.port;

import com.amit.fintrack.analytics.application.model.MonthlyAnalyticsRecord;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public interface MonthlyAnalyticsStore {

    Optional<MonthlyAnalyticsRecord> find(UUID userId, int year, int month);

    MonthlyAnalyticsRecord save(UUID userId, int year, int month, BigDecimal totalIncome, BigDecimal totalExpense);
}

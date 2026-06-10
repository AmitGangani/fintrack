package com.amit.fintrack.budget.application.port;

import com.amit.fintrack.budget.application.model.BudgetSpendingRecord;
import com.amit.fintrack.budget.domain.BudgetCategory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BudgetSpendingStore {

    List<BudgetSpendingRecord> findByUserIdAndYearAndMonth(UUID userId, int year, int month);

    Optional<BudgetSpendingRecord> findByUserIdAndCategoryAndYearAndMonth(
            UUID userId,
            BudgetCategory category,
            int year,
            int month
    );

    BudgetSpendingRecord saveSpending(
            UUID userId,
            BudgetCategory category,
            int year,
            int month,
            BigDecimal spentAmount
    );
}

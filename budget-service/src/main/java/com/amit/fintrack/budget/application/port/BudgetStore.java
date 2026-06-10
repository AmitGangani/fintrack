package com.amit.fintrack.budget.application.port;

import com.amit.fintrack.budget.application.model.BudgetCommand;
import com.amit.fintrack.budget.application.model.BudgetRecord;
import com.amit.fintrack.budget.domain.BudgetCategory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BudgetStore {

    BudgetRecord create(UUID userId, BudgetCommand command);

    List<BudgetRecord> findByUserIdAndYearAndMonth(UUID userId, int year, int month);

    Optional<BudgetRecord> findByIdAndUserId(UUID budgetId, UUID userId);

    Optional<BudgetRecord> findByUserIdAndCategoryAndYearAndMonth(
            UUID userId,
            BudgetCategory category,
            int year,
            int month
    );

    BudgetRecord update(UUID budgetId, UUID userId, BudgetCommand command);

    void delete(UUID budgetId, UUID userId);
}

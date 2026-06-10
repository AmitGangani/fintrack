package com.amit.fintrack.budget.persistence.repository;

import com.amit.fintrack.budget.persistence.entity.BudgetAlertHistory;
import com.amit.fintrack.budget.domain.BudgetCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BudgetAlertHistoryRepository extends JpaRepository<BudgetAlertHistory, UUID> {

    boolean existsByUserIdAndCategoryAndYearAndMonthAndAlertType(
            UUID userId,
            BudgetCategory category,
            int year,
            int month,
            String alertType
    );
}
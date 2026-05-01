package com.amit.fintrack.budget.repository;

import com.amit.fintrack.budget.entity.Budget;
import com.amit.fintrack.budget.entity.BudgetCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BudgetRepository extends JpaRepository<Budget, UUID> {

    List<Budget> findByUserIdAndYearAndMonthOrderByCategoryAsc(
            UUID userId,
            int year,
            int month
    );

    Optional<Budget> findByIdAndUserId(UUID id, UUID userId);

    Optional<Budget> findByUserIdAndCategoryAndYearAndMonth(
            UUID userId,
            BudgetCategory category,
            int year,
            int month
    );
}
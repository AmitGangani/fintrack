package com.amit.fintrack.budget.repository;

import com.amit.fintrack.budget.entity.BudgetCategory;
import com.amit.fintrack.budget.entity.BudgetSpending;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BudgetSpendingRepository extends JpaRepository<BudgetSpending, UUID> {

    Optional<BudgetSpending> findByUserIdAndCategoryAndYearAndMonth(
            UUID userId,
            BudgetCategory category,
            int year,
            int month
    );

    List<BudgetSpending> findByUserIdAndYearAndMonth(
            UUID userId,
            int year,
            int month
    );
}
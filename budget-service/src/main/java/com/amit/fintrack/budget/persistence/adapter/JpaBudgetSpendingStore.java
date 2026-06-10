package com.amit.fintrack.budget.persistence.adapter;

import com.amit.fintrack.budget.application.model.BudgetSpendingRecord;
import com.amit.fintrack.budget.application.port.BudgetSpendingStore;
import com.amit.fintrack.budget.domain.BudgetCategory;
import com.amit.fintrack.budget.persistence.entity.BudgetSpending;
import com.amit.fintrack.budget.persistence.repository.BudgetSpendingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JpaBudgetSpendingStore implements BudgetSpendingStore {

    private final BudgetSpendingRepository budgetSpendingRepository;

    @Override
    public List<BudgetSpendingRecord> findByUserIdAndYearAndMonth(UUID userId, int year, int month) {
        return budgetSpendingRepository.findByUserIdAndYearAndMonth(userId, year, month)
                .stream()
                .map(this::toRecord)
                .toList();
    }

    @Override
    public Optional<BudgetSpendingRecord> findByUserIdAndCategoryAndYearAndMonth(
            UUID userId,
            BudgetCategory category,
            int year,
            int month
    ) {
        return budgetSpendingRepository.findByUserIdAndCategoryAndYearAndMonth(userId, category, year, month)
                .map(this::toRecord);
    }

    @Override
    public BudgetSpendingRecord saveSpending(
            UUID userId,
            BudgetCategory category,
            int year,
            int month,
            BigDecimal spentAmount
    ) {
        BudgetSpending spending = budgetSpendingRepository
                .findByUserIdAndCategoryAndYearAndMonth(userId, category, year, month)
                .orElseGet(() -> BudgetSpending.builder()
                        .userId(userId)
                        .category(category)
                        .year(year)
                        .month(month)
                        .build());

        spending.setSpentAmount(spentAmount);
        spending.setUpdatedAt(LocalDateTime.now());

        return toRecord(budgetSpendingRepository.save(spending));
    }

    private BudgetSpendingRecord toRecord(BudgetSpending spending) {
        return new BudgetSpendingRecord(
                spending.getUserId(),
                spending.getCategory(),
                spending.getMonth(),
                spending.getYear(),
                spending.getSpentAmount()
        );
    }
}

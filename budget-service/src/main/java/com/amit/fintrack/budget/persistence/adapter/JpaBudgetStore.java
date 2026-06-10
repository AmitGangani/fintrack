package com.amit.fintrack.budget.persistence.adapter;

import com.amit.fintrack.budget.application.model.BudgetCommand;
import com.amit.fintrack.budget.application.model.BudgetRecord;
import com.amit.fintrack.budget.application.port.BudgetStore;
import com.amit.fintrack.budget.domain.BudgetCategory;
import com.amit.fintrack.budget.exception.BudgetNotFoundException;
import com.amit.fintrack.budget.persistence.entity.Budget;
import com.amit.fintrack.budget.persistence.repository.BudgetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JpaBudgetStore implements BudgetStore {

    private final BudgetRepository budgetRepository;

    @Override
    public BudgetRecord create(UUID userId, BudgetCommand command) {
        LocalDateTime now = LocalDateTime.now();
        Budget budget = Budget.builder()
                .userId(userId)
                .category(command.category())
                .month(command.month())
                .year(command.year())
                .limitAmount(command.limitAmount())
                .createdAt(now)
                .updatedAt(now)
                .build();

        return toRecord(budgetRepository.save(budget));
    }

    @Override
    public List<BudgetRecord> findByUserIdAndYearAndMonth(UUID userId, int year, int month) {
        return budgetRepository.findByUserIdAndYearAndMonthOrderByCategoryAsc(userId, year, month)
                .stream()
                .map(this::toRecord)
                .toList();
    }

    @Override
    public Optional<BudgetRecord> findByIdAndUserId(UUID budgetId, UUID userId) {
        return budgetRepository.findByIdAndUserId(budgetId, userId).map(this::toRecord);
    }

    @Override
    public Optional<BudgetRecord> findByUserIdAndCategoryAndYearAndMonth(
            UUID userId,
            BudgetCategory category,
            int year,
            int month
    ) {
        return budgetRepository.findByUserIdAndCategoryAndYearAndMonth(userId, category, year, month)
                .map(this::toRecord);
    }

    @Override
    public BudgetRecord update(UUID budgetId, UUID userId, BudgetCommand command) {
        Budget budget = findBudget(budgetId, userId);
        budget.setCategory(command.category());
        budget.setMonth(command.month());
        budget.setYear(command.year());
        budget.setLimitAmount(command.limitAmount());
        budget.setUpdatedAt(LocalDateTime.now());

        return toRecord(budgetRepository.save(budget));
    }

    @Override
    public void delete(UUID budgetId, UUID userId) {
        budgetRepository.delete(findBudget(budgetId, userId));
    }

    private Budget findBudget(UUID budgetId, UUID userId) {
        return budgetRepository.findByIdAndUserId(budgetId, userId)
                .orElseThrow(() -> new BudgetNotFoundException("Budget not found"));
    }

    private BudgetRecord toRecord(Budget budget) {
        return new BudgetRecord(
                budget.getId(),
                budget.getUserId(),
                budget.getCategory(),
                budget.getMonth(),
                budget.getYear(),
                budget.getLimitAmount(),
                budget.getCreatedAt(),
                budget.getUpdatedAt()
        );
    }
}

package com.amit.fintrack.budget.service;

import com.amit.fintrack.budget.dto.BudgetRequest;
import com.amit.fintrack.budget.dto.BudgetResponse;
import com.amit.fintrack.budget.entity.Budget;
import com.amit.fintrack.budget.entity.BudgetCategory;
import com.amit.fintrack.budget.entity.BudgetSpending;
import com.amit.fintrack.budget.entity.BudgetStatus;
import com.amit.fintrack.budget.exception.BudgetNotFoundException;
import com.amit.fintrack.budget.exception.DuplicateBudgetException;
import com.amit.fintrack.budget.repository.BudgetRepository;
import com.amit.fintrack.budget.repository.BudgetSpendingRepository;
import com.amit.fintrack.budget.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final CurrentUserService currentUserService;
    private final BudgetSpendingRepository budgetSpendingRepository;

    public BudgetResponse createBudget(BudgetRequest request) {
        UUID currentUserId = currentUserService.getCurrentUserId();

        budgetRepository.findByUserIdAndCategoryAndYearAndMonth(
                currentUserId,
                request.category(),
                request.year(),
                request.month()
        ).ifPresent(existingBudget -> {
            throw new DuplicateBudgetException("Budget already exists for this category and month");
        });

        Budget budget = Budget.builder()
                .userId(currentUserId)
                .category(request.category())
                .month(request.month())
                .year(request.year())
                .limitAmount(request.limitAmount())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Budget savedBudget = budgetRepository.save(budget);

        Map<BudgetCategory, BigDecimal> expenseMap = getExpenseMap(
                currentUserId,
                request.year(),
                request.month()
        );

        BigDecimal spentAmount = expenseMap.getOrDefault(
                savedBudget.getCategory(),
                BigDecimal.ZERO
        );

        return toResponse(savedBudget, spentAmount);
    }

    public List<BudgetResponse> getMonthlyBudgets(int year, int month) {
        UUID currentUserId = currentUserService.getCurrentUserId();

        List<Budget> budgets = budgetRepository.findByUserIdAndYearAndMonthOrderByCategoryAsc(
                currentUserId,
                year,
                month
        );

        Map<BudgetCategory, BigDecimal> expenseMap = getExpenseMap(
                currentUserId,
                year,
                month
        );

        return budgets.stream()
                .map(budget -> toResponse(
                        budget,
                        expenseMap.getOrDefault(budget.getCategory(), BigDecimal.ZERO)
                ))
                .toList();
    }

    public BudgetResponse getBudgetById(
            UUID budgetId
    ) {
        UUID currentUserId = currentUserService.getCurrentUserId();

        Budget budget = budgetRepository.findByIdAndUserId(budgetId, currentUserId)
                .orElseThrow(() -> new BudgetNotFoundException("Budget not found"));

        Map<BudgetCategory, BigDecimal> expenseMap = getExpenseMap(
                currentUserId,
                budget.getYear(),
                budget.getMonth()
        );

        BigDecimal spentAmount = expenseMap.getOrDefault(
                budget.getCategory(),
                BigDecimal.ZERO
        );

        return toResponse(budget, spentAmount);
    }

    public BudgetResponse updateBudget(
            UUID budgetId,
            BudgetRequest request
    ) {
        UUID currentUserId = currentUserService.getCurrentUserId();

        Budget budget = budgetRepository.findByIdAndUserId(budgetId, currentUserId)
                .orElseThrow(() -> new BudgetNotFoundException("Budget not found"));

        budgetRepository.findByUserIdAndCategoryAndYearAndMonth(
                currentUserId,
                request.category(),
                request.year(),
                request.month()
        ).ifPresent(existingBudget -> {
            if (!existingBudget.getId().equals(budgetId)) {
                throw new DuplicateBudgetException("Budget already exists for this category and month");
            }
        });

        budget.setCategory(request.category());
        budget.setMonth(request.month());
        budget.setYear(request.year());
        budget.setLimitAmount(request.limitAmount());
        budget.setUpdatedAt(LocalDateTime.now());

        Budget updatedBudget = budgetRepository.save(budget);

        Map<BudgetCategory, BigDecimal> expenseMap = getExpenseMap(
                currentUserId,
                request.year(),
                request.month()
        );

        BigDecimal spentAmount = expenseMap.getOrDefault(
                updatedBudget.getCategory(),
                BigDecimal.ZERO
        );

        return toResponse(updatedBudget, spentAmount);
    }

    public void deleteBudget(UUID budgetId) {
        UUID currentUserId = currentUserService.getCurrentUserId();

        Budget budget = budgetRepository.findByIdAndUserId(budgetId, currentUserId)
                .orElseThrow(() -> new BudgetNotFoundException("Budget not found"));

        budgetRepository.delete(budget);
    }

    private Map<BudgetCategory, BigDecimal> getExpenseMap(
            UUID userId,
            int year,
            int month
    ) {
        List<BudgetSpending> spendingList =
                budgetSpendingRepository.findByUserIdAndYearAndMonth(
                        userId,
                        year,
                        month
                );

        return spendingList.stream()
                .collect(Collectors.toMap(
                        BudgetSpending::getCategory,
                        BudgetSpending::getSpentAmount
                ));
    }

    private BudgetResponse toResponse(Budget budget, BigDecimal spentAmount) {
        BigDecimal remainingAmount = budget.getLimitAmount().subtract(spentAmount);

        BigDecimal percentageUsed = spentAmount
                .multiply(BigDecimal.valueOf(100))
                .divide(budget.getLimitAmount(), 2, RoundingMode.HALF_UP);

        BudgetStatus status = calculateStatus(percentageUsed);

        return new BudgetResponse(
                budget.getId(),
                budget.getCategory(),
                budget.getMonth(),
                budget.getYear(),
                budget.getLimitAmount(),
                spentAmount,
                remainingAmount,
                percentageUsed,
                status,
                budget.getCreatedAt(),
                budget.getUpdatedAt()
        );
    }

    private BudgetStatus calculateStatus(BigDecimal percentageUsed) {
        if (percentageUsed.compareTo(BigDecimal.valueOf(100)) >= 0) {
            return BudgetStatus.EXCEEDED;
        }

        if (percentageUsed.compareTo(BigDecimal.valueOf(80)) >= 0) {
            return BudgetStatus.WARNING;
        }

        return BudgetStatus.SAFE;
    }
}

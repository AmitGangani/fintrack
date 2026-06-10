package com.amit.fintrack.budget.application;

import com.amit.fintrack.budget.application.model.BudgetCommand;
import com.amit.fintrack.budget.application.model.BudgetRecord;
import com.amit.fintrack.budget.application.model.BudgetSpendingRecord;
import com.amit.fintrack.budget.application.model.BudgetView;
import com.amit.fintrack.budget.application.port.BudgetSpendingStore;
import com.amit.fintrack.budget.application.port.BudgetStore;
import com.amit.fintrack.budget.domain.BudgetAlertPolicy;
import com.amit.fintrack.budget.domain.BudgetCategory;
import com.amit.fintrack.budget.exception.BudgetNotFoundException;
import com.amit.fintrack.budget.exception.DuplicateBudgetException;
import com.amit.fintrack.budget.application.port.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetStore budgetStore;
    private final CurrentUserProvider currentUserProvider;
    private final BudgetSpendingStore budgetSpendingStore;
    private final BudgetAlertPolicy budgetAlertPolicy;

    public BudgetView createBudget(BudgetCommand command) {
        UUID currentUserId = currentUserProvider.getCurrentUserId();

        ensureBudgetSlotAvailable(currentUserId, command, null);
        BudgetRecord savedBudget = budgetStore.create(currentUserId, command);

        return toView(savedBudget, getSpentAmount(currentUserId, savedBudget));
    }

    public List<BudgetView> getMonthlyBudgets(int year, int month) {
        UUID currentUserId = currentUserProvider.getCurrentUserId();
        List<BudgetRecord> budgets = budgetStore.findByUserIdAndYearAndMonth(currentUserId, year, month);
        Map<BudgetCategory, BigDecimal> expenseMap = getExpenseMap(currentUserId, year, month);

        return budgets.stream()
                .map(budget -> toView(budget, expenseMap.getOrDefault(budget.category(), BigDecimal.ZERO)))
                .toList();
    }

    public BudgetView getBudgetById(UUID budgetId) {
        UUID currentUserId = currentUserProvider.getCurrentUserId();
        BudgetRecord budget = getBudget(budgetId, currentUserId);

        return toView(budget, getSpentAmount(currentUserId, budget));
    }

    public BudgetView updateBudget(UUID budgetId, BudgetCommand command) {
        UUID currentUserId = currentUserProvider.getCurrentUserId();
        getBudget(budgetId, currentUserId);

        ensureBudgetSlotAvailable(currentUserId, command, budgetId);
        BudgetRecord updatedBudget = budgetStore.update(budgetId, currentUserId, command);

        return toView(updatedBudget, getSpentAmount(currentUserId, updatedBudget));
    }

    public void deleteBudget(UUID budgetId) {
        budgetStore.delete(budgetId, currentUserProvider.getCurrentUserId());
    }

    private BudgetRecord getBudget(UUID budgetId, UUID userId) {
        return budgetStore.findByIdAndUserId(budgetId, userId)
                .orElseThrow(() -> new BudgetNotFoundException("Budget not found"));
    }

    private void ensureBudgetSlotAvailable(UUID userId, BudgetCommand command, UUID allowedBudgetId) {
        budgetStore.findByUserIdAndCategoryAndYearAndMonth(
                userId,
                command.category(),
                command.year(),
                command.month()
        ).ifPresent(existingBudget -> {
            if (allowedBudgetId == null || !existingBudget.id().equals(allowedBudgetId)) {
                throw new DuplicateBudgetException("Budget already exists for this category and month");
            }
        });
    }

    private BigDecimal getSpentAmount(UUID userId, BudgetRecord budget) {
        return getExpenseMap(userId, budget.year(), budget.month())
                .getOrDefault(budget.category(), BigDecimal.ZERO);
    }

    private Map<BudgetCategory, BigDecimal> getExpenseMap(UUID userId, int year, int month) {
        return budgetSpendingStore.findByUserIdAndYearAndMonth(userId, year, month)
                .stream()
                .collect(Collectors.toMap(
                        BudgetSpendingRecord::category,
                        BudgetSpendingRecord::spentAmount
                ));
    }

    private BudgetView toView(BudgetRecord budget, BigDecimal spentAmount) {
        BigDecimal remainingAmount = budget.limitAmount().subtract(spentAmount);
        BigDecimal percentageUsed = budgetAlertPolicy.calculatePercentageUsed(spentAmount, budget.limitAmount());

        return new BudgetView(
                budget.id(),
                budget.category(),
                budget.month(),
                budget.year(),
                budget.limitAmount(),
                spentAmount,
                remainingAmount,
                percentageUsed,
                budgetAlertPolicy.calculateStatus(percentageUsed),
                budget.createdAt(),
                budget.updatedAt()
        );
    }
}

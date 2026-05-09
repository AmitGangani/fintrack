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
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BudgetServiceTest {

    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000031");
    private static final UUID BUDGET_ID = UUID.fromString("00000000-0000-0000-0000-000000000032");

    @Test
    void createBudgetReturnsSpendingStatusFromTrackedSpending() {
        FakeBudgetRepository budgetRepository = new FakeBudgetRepository();
        FakeBudgetSpendingRepository budgetSpendingRepository = new FakeBudgetSpendingRepository();
        budgetSpendingRepository.spending = List.of(spending(BudgetCategory.FOOD, "80.00"));
        BudgetService budgetService = new BudgetService(
                budgetRepository.repository(),
                new FixedCurrentUserService(),
                budgetSpendingRepository.repository()
        );

        BudgetResponse response = budgetService.createBudget(new BudgetRequest(
                BudgetCategory.FOOD,
                5,
                2026,
                new BigDecimal("100.00")
        ));

        assertEquals(BudgetStatus.WARNING, response.status());
        assertEquals(new BigDecimal("80.00"), response.spentAmount());
        assertEquals(new BigDecimal("20.00"), response.remainingAmount());
        assertEquals(new BigDecimal("80.00"), response.percentageUsed());
    }

    @Test
    void createBudgetRejectsDuplicateCategoryForMonth() {
        FakeBudgetRepository budgetRepository = new FakeBudgetRepository();
        budgetRepository.existingBudget = budget(BudgetCategory.FOOD, "100.00");
        BudgetService budgetService = new BudgetService(
                budgetRepository.repository(),
                new FixedCurrentUserService(),
                new FakeBudgetSpendingRepository().repository()
        );

        assertThrows(
                DuplicateBudgetException.class,
                () -> budgetService.createBudget(
                        new BudgetRequest(BudgetCategory.FOOD, 5, 2026, new BigDecimal("100.00"))
                )
        );

        assertEquals(0, budgetRepository.saveCount);
    }

    @Test
    void updateBudgetUpdatesExistingBudget() {
        FakeBudgetRepository budgetRepository = new FakeBudgetRepository();
        budgetRepository.budgetById = budget(BudgetCategory.FOOD, "100.00");
        FakeBudgetSpendingRepository budgetSpendingRepository = new FakeBudgetSpendingRepository();
        budgetSpendingRepository.spending = List.of(spending(BudgetCategory.RENT, "900.00"));
        BudgetService budgetService = new BudgetService(
                budgetRepository.repository(),
                new FixedCurrentUserService(),
                budgetSpendingRepository.repository()
        );

        BudgetResponse response = budgetService.updateBudget(
                BUDGET_ID,
                new BudgetRequest(BudgetCategory.RENT, 5, 2026, new BigDecimal("1000.00"))
        );

        assertEquals(BudgetCategory.RENT, response.category());
        assertEquals(new BigDecimal("1000.00"), response.limitAmount());
        assertEquals(BudgetStatus.WARNING, response.status());
        assertEquals(1, budgetRepository.saveCount);
    }

    @Test
    void deleteBudgetDeletesOwnedBudget() {
        FakeBudgetRepository budgetRepository = new FakeBudgetRepository();
        budgetRepository.budgetById = budget(BudgetCategory.FOOD, "100.00");
        BudgetService budgetService = new BudgetService(
                budgetRepository.repository(),
                new FixedCurrentUserService(),
                new FakeBudgetSpendingRepository().repository()
        );

        budgetService.deleteBudget(BUDGET_ID);

        assertEquals(1, budgetRepository.deleteCount);
    }

    @Test
    void getBudgetByIdThrowsWhenMissing() {
        BudgetService budgetService = new BudgetService(
                new FakeBudgetRepository().repository(),
                new FixedCurrentUserService(),
                new FakeBudgetSpendingRepository().repository()
        );

        assertThrows(BudgetNotFoundException.class, () -> budgetService.getBudgetById(BUDGET_ID));
    }

    private static Budget budget(BudgetCategory category, String limitAmount) {
        return Budget.builder()
                .id(BUDGET_ID)
                .userId(USER_ID)
                .category(category)
                .month(5)
                .year(2026)
                .limitAmount(new BigDecimal(limitAmount))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private static BudgetSpending spending(BudgetCategory category, String amount) {
        return BudgetSpending.builder()
                .userId(USER_ID)
                .category(category)
                .month(5)
                .year(2026)
                .spentAmount(new BigDecimal(amount))
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private static final class FakeBudgetRepository {
        private Budget existingBudget;
        private Budget budgetById;
        private int saveCount;
        private int deleteCount;

        private BudgetRepository repository() {
            return (BudgetRepository) Proxy.newProxyInstance(
                    BudgetRepository.class.getClassLoader(),
                    new Class<?>[]{BudgetRepository.class},
                    (proxy, method, args) -> switch (method.getName()) {
                        case "findByUserIdAndCategoryAndYearAndMonth" -> Optional.ofNullable(existingBudget);
                        case "findByIdAndUserId" -> Optional.ofNullable(budgetById);
                        case "save" -> {
                            Budget saved = (Budget) args[0];
                            saved.setId(BUDGET_ID);
                            budgetById = saved;
                            saveCount++;
                            yield saved;
                        }
                        case "delete" -> {
                            deleteCount++;
                            yield null;
                        }
                        case "toString" -> "FakeBudgetRepository";
                        default -> throw new UnsupportedOperationException(method.getName());
                    }
            );
        }
    }

    private static final class FakeBudgetSpendingRepository {
        private List<BudgetSpending> spending = List.of();

        private BudgetSpendingRepository repository() {
            return (BudgetSpendingRepository) Proxy.newProxyInstance(
                    BudgetSpendingRepository.class.getClassLoader(),
                    new Class<?>[]{BudgetSpendingRepository.class},
                    (proxy, method, args) -> switch (method.getName()) {
                        case "findByUserIdAndYearAndMonth" -> spending;
                        case "toString" -> "FakeBudgetSpendingRepository";
                        default -> throw new UnsupportedOperationException(method.getName());
                    }
            );
        }
    }

    private static final class FixedCurrentUserService extends CurrentUserService {
        @Override
        public UUID getCurrentUserId() {
            return USER_ID;
        }
    }
}

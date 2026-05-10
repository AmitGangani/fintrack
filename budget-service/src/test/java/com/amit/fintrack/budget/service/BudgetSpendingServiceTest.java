package com.amit.fintrack.budget.service;

import com.amit.fintrack.budget.entity.BudgetCategory;
import com.amit.fintrack.budget.entity.BudgetSpending;
import com.amit.fintrack.budget.entity.ProcessedKafkaEvent;
import com.amit.fintrack.budget.event.BudgetAlertProducer;
import com.amit.fintrack.budget.event.TransactionEventType;
import com.amit.fintrack.budget.event.TransactionLifecycleEvent;
import com.amit.fintrack.budget.repository.BudgetAlertHistoryRepository;
import com.amit.fintrack.budget.repository.BudgetRepository;
import com.amit.fintrack.budget.repository.BudgetSpendingRepository;
import com.amit.fintrack.budget.repository.ProcessedKafkaEventRepository;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BudgetSpendingServiceTest {

    private static final UUID EVENT_ID = UUID.fromString("00000000-0000-0000-0000-000000000041");
    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000042");
    private static final UUID ACCOUNT_ID = UUID.fromString("00000000-0000-0000-0000-000000000043");
    private static final UUID TRANSACTION_ID = UUID.fromString("00000000-0000-0000-0000-000000000044");

    @Test
    void createdExpenseCreatesMonthlySpendingAndMarksEventProcessed() {
        FakeBudgetSpendingRepository spendingRepository = new FakeBudgetSpendingRepository();
        FakeProcessedKafkaEventRepository eventRepository = new FakeProcessedKafkaEventRepository();
        BudgetSpendingService service = service(spendingRepository, eventRepository);

        service.handleTransactionLifecycleEvent(createdExpenseEvent());

        BudgetSpending spending = spendingRepository.savedSpending;
        assertEquals(USER_ID, spending.getUserId());
        assertEquals(BudgetCategory.FOOD, spending.getCategory());
        assertEquals(2026, spending.getYear());
        assertEquals(5, spending.getMonth());
        assertEquals(new BigDecimal("25.00"), spending.getSpentAmount());
        assertEquals("TRANSACTION_CREATED", eventRepository.savedEvent.getEventType());
    }

    @Test
    void duplicateEventIsIgnored() {
        FakeBudgetSpendingRepository spendingRepository = new FakeBudgetSpendingRepository();
        FakeProcessedKafkaEventRepository eventRepository = new FakeProcessedKafkaEventRepository();
        eventRepository.exists = true;
        BudgetSpendingService service = service(spendingRepository, eventRepository);

        service.handleTransactionLifecycleEvent(createdExpenseEvent());

        assertEquals(0, spendingRepository.saveCount);
        assertEquals(0, eventRepository.saveCount);
    }

    @Test
    void updatedExpenseMovesSpendingBetweenCategories() {
        FakeBudgetSpendingRepository spendingRepository = new FakeBudgetSpendingRepository();
        spendingRepository.spendingByCategory.put(
                BudgetCategory.FOOD,
                spending(BudgetCategory.FOOD, new BigDecimal("100.00"))
        );
        spendingRepository.spendingByCategory.put(
                BudgetCategory.TRANSPORT,
                spending(BudgetCategory.TRANSPORT, new BigDecimal("10.00"))
        );
        FakeProcessedKafkaEventRepository eventRepository = new FakeProcessedKafkaEventRepository();
        BudgetSpendingService service = service(spendingRepository, eventRepository);

        service.handleTransactionLifecycleEvent(updatedExpenseEvent());

        assertEquals(new BigDecimal("75.00"), spendingRepository.savedSpendings.get(0).getSpentAmount());
        assertEquals(new BigDecimal("40.00"), spendingRepository.savedSpendings.get(1).getSpentAmount());
        assertEquals("TRANSACTION_UPDATED", eventRepository.savedEvent.getEventType());
    }

    @Test
    void deletedExpenseReversesSpendingWithoutGoingNegative() {
        FakeBudgetSpendingRepository spendingRepository = new FakeBudgetSpendingRepository();
        spendingRepository.spendingByCategory.put(
                BudgetCategory.FOOD,
                spending(BudgetCategory.FOOD, new BigDecimal("10.00"))
        );
        FakeProcessedKafkaEventRepository eventRepository = new FakeProcessedKafkaEventRepository();
        BudgetSpendingService service = service(spendingRepository, eventRepository);

        service.handleTransactionLifecycleEvent(deletedExpenseEvent());

        assertEquals(new BigDecimal("0"), spendingRepository.savedSpendings.getFirst().getSpentAmount());
        assertEquals("TRANSACTION_DELETED", eventRepository.savedEvent.getEventType());
    }

    private static TransactionLifecycleEvent createdExpenseEvent() {
        return new TransactionLifecycleEvent(
                EVENT_ID,
                TransactionEventType.CREATED,
                TRANSACTION_ID,
                USER_ID,
                null,
                ACCOUNT_ID,
                null,
                null,
                null,
                null,
                "EXPENSE",
                BudgetCategory.FOOD,
                new BigDecimal("25.00"),
                LocalDate.of(2026, 5, 1),
                LocalDateTime.now()
        );
    }

    private static TransactionLifecycleEvent updatedExpenseEvent() {
        return new TransactionLifecycleEvent(
                EVENT_ID,
                TransactionEventType.UPDATED,
                TRANSACTION_ID,
                USER_ID,
                ACCOUNT_ID,
                ACCOUNT_ID,
                "EXPENSE",
                BudgetCategory.FOOD,
                new BigDecimal("25.00"),
                LocalDate.of(2026, 5, 1),
                "EXPENSE",
                BudgetCategory.TRANSPORT,
                new BigDecimal("30.00"),
                LocalDate.of(2026, 5, 2),
                LocalDateTime.now()
        );
    }

    private static TransactionLifecycleEvent deletedExpenseEvent() {
        return new TransactionLifecycleEvent(
                EVENT_ID,
                TransactionEventType.DELETED,
                TRANSACTION_ID,
                USER_ID,
                ACCOUNT_ID,
                null,
                "EXPENSE",
                BudgetCategory.FOOD,
                new BigDecimal("25.00"),
                LocalDate.of(2026, 5, 1),
                null,
                null,
                null,
                null,
                LocalDateTime.now()
        );
    }

    private static BudgetSpending spending(BudgetCategory category, BigDecimal spentAmount) {
        return BudgetSpending.builder()
                .userId(USER_ID)
                .category(category)
                .month(5)
                .year(2026)
                .spentAmount(spentAmount)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private static BudgetSpendingService service(
            FakeBudgetSpendingRepository spendingRepository,
            FakeProcessedKafkaEventRepository eventRepository
    ) {
        return new BudgetSpendingService(
                spendingRepository.repository(),
                eventRepository.repository(),
                FakeBudgetRepository.repository(),
                FakeBudgetAlertHistoryRepository.repository(),
                new BudgetAlertProducer(null, null)
        );
    }

    private static final class FakeBudgetSpendingRepository {
        private BudgetSpending savedSpending;
        private final List<BudgetSpending> savedSpendings = new ArrayList<>();
        private final Map<BudgetCategory, BudgetSpending> spendingByCategory = new HashMap<>();
        private int saveCount;

        private BudgetSpendingRepository repository() {
            return (BudgetSpendingRepository) Proxy.newProxyInstance(
                    BudgetSpendingRepository.class.getClassLoader(),
                    new Class<?>[]{BudgetSpendingRepository.class},
                    (proxy, method, args) -> switch (method.getName()) {
                        case "findByUserIdAndCategoryAndYearAndMonth" ->
                                Optional.ofNullable(spendingByCategory.get((BudgetCategory) args[1]));
                        case "save" -> {
                            savedSpending = (BudgetSpending) args[0];
                            savedSpendings.add(savedSpending);
                            saveCount++;
                            yield savedSpending;
                        }
                        case "toString" -> "FakeBudgetSpendingRepository";
                        default -> throw new UnsupportedOperationException(method.getName());
                    }
            );
        }
    }

    private static final class FakeProcessedKafkaEventRepository {
        private boolean exists;
        private ProcessedKafkaEvent savedEvent;
        private int saveCount;

        private ProcessedKafkaEventRepository repository() {
            return (ProcessedKafkaEventRepository) Proxy.newProxyInstance(
                    ProcessedKafkaEventRepository.class.getClassLoader(),
                    new Class<?>[]{ProcessedKafkaEventRepository.class},
                    (proxy, method, args) -> switch (method.getName()) {
                        case "existsById" -> exists;
                        case "save" -> {
                            savedEvent = (ProcessedKafkaEvent) args[0];
                            saveCount++;
                            yield savedEvent;
                        }
                        case "toString" -> "FakeProcessedKafkaEventRepository";
                        default -> throw new UnsupportedOperationException(method.getName());
                    }
            );
        }
    }

    private static final class FakeBudgetRepository {
        private static BudgetRepository repository() {
            return (BudgetRepository) Proxy.newProxyInstance(
                    BudgetRepository.class.getClassLoader(),
                    new Class<?>[]{BudgetRepository.class},
                    (proxy, method, args) -> switch (method.getName()) {
                        case "findByUserIdAndCategoryAndYearAndMonth" -> Optional.empty();
                        case "toString" -> "FakeBudgetRepository";
                        default -> throw new UnsupportedOperationException(method.getName());
                    }
            );
        }
    }

    private static final class FakeBudgetAlertHistoryRepository {
        private static BudgetAlertHistoryRepository repository() {
            return (BudgetAlertHistoryRepository) Proxy.newProxyInstance(
                    BudgetAlertHistoryRepository.class.getClassLoader(),
                    new Class<?>[]{BudgetAlertHistoryRepository.class},
                    (proxy, method, args) -> switch (method.getName()) {
                        case "toString" -> "FakeBudgetAlertHistoryRepository";
                        default -> throw new UnsupportedOperationException(method.getName());
                    }
            );
        }
    }
}

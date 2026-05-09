package com.amit.fintrack.transaction.service;

import com.amit.fintrack.transaction.dto.CategoryExpenseSummaryResponse;
import com.amit.fintrack.transaction.dto.MonthlySummaryResponse;
import com.amit.fintrack.transaction.dto.TransactionRequest;
import com.amit.fintrack.transaction.dto.TransactionResponse;
import com.amit.fintrack.transaction.entity.FinancialTransaction;
import com.amit.fintrack.transaction.entity.TransactionCategory;
import com.amit.fintrack.transaction.entity.TransactionType;
import com.amit.fintrack.transaction.event.TransactionEventType;
import com.amit.fintrack.transaction.event.TransactionLifecycleEvent;
import com.amit.fintrack.transaction.repository.TransactionRepository;
import com.amit.fintrack.transaction.security.CurrentUserService;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TransactionServiceTest {

    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000021");
    private static final UUID ACCOUNT_ID = UUID.fromString("00000000-0000-0000-0000-000000000022");
    private static final UUID TRANSACTION_ID = UUID.fromString("00000000-0000-0000-0000-000000000023");
    private static final UUID NEW_ACCOUNT_ID = UUID.fromString("00000000-0000-0000-0000-000000000024");

    @Test
    void createTransactionSavesTransactionAndOutboxEvent() {
        FakeTransactionRepository transactionRepository = new FakeTransactionRepository();
        RecordingOutboxService outboxService = new RecordingOutboxService();
        TransactionService transactionService = new TransactionService(
                transactionRepository.repository(),
                new FixedCurrentUserService(),
                outboxService
        );

        TransactionResponse response = transactionService.createTransaction(new TransactionRequest(
                ACCOUNT_ID,
                TransactionType.EXPENSE,
                TransactionCategory.FOOD,
                new BigDecimal("40.00"),
                "Lunch",
                LocalDate.of(2026, 5, 1)
        ));

        TransactionLifecycleEvent event = outboxService.savedEvent;
        assertEquals(TransactionEventType.CREATED, event.eventType());
        assertEquals(TRANSACTION_ID, event.transactionId());
        assertEquals(USER_ID, event.userId());
        assertEquals(TransactionType.EXPENSE, event.newType());
        assertEquals(TransactionCategory.FOOD, event.newCategory());
        assertEquals(new BigDecimal("40.00"), event.newAmount());
        assertEquals(TRANSACTION_ID, response.id());
    }

    @Test
    void monthlySummaryCalculatesIncomeExpenseAndSavings() {
        FakeTransactionRepository transactionRepository = new FakeTransactionRepository();
        transactionRepository.monthlyTransactions = List.of(
                transaction(TransactionType.INCOME, TransactionCategory.SALARY, "1000.00"),
                transaction(TransactionType.EXPENSE, TransactionCategory.RENT, "300.00"),
                transaction(TransactionType.EXPENSE, TransactionCategory.FOOD, "50.00")
        );
        TransactionService transactionService = new TransactionService(
                transactionRepository.repository(),
                new FixedCurrentUserService(),
                new RecordingOutboxService()
        );

        MonthlySummaryResponse response = transactionService.getMonthlySummary(2026, 5);

        assertEquals(new BigDecimal("1000.00"), response.totalIncome());
        assertEquals(new BigDecimal("350.00"), response.totalExpense());
        assertEquals(new BigDecimal("650.00"), response.netSavings());
        assertEquals(3, response.transactionCount());
    }

    @Test
    void categoryExpenseSummaryGroupsOnlyExpenses() {
        FakeTransactionRepository transactionRepository = new FakeTransactionRepository();
        transactionRepository.monthlyTransactions = List.of(
                transaction(TransactionType.INCOME, TransactionCategory.SALARY, "1000.00"),
                transaction(TransactionType.EXPENSE, TransactionCategory.FOOD, "40.00"),
                transaction(TransactionType.EXPENSE, TransactionCategory.FOOD, "10.00"),
                transaction(TransactionType.EXPENSE, TransactionCategory.RENT, "300.00")
        );
        TransactionService transactionService = new TransactionService(
                transactionRepository.repository(),
                new FixedCurrentUserService(),
                new RecordingOutboxService()
        );

        List<CategoryExpenseSummaryResponse> response =
                transactionService.getCategoryExpenseSummary(2026, 5);

        assertEquals(new BigDecimal("50.00"), totalFor(response, TransactionCategory.FOOD));
        assertEquals(new BigDecimal("300.00"), totalFor(response, TransactionCategory.RENT));
    }

    @Test
    void updateTransactionWritesUpdatedEventWithOldAndNewValues() {
        FakeTransactionRepository transactionRepository = new FakeTransactionRepository();
        transactionRepository.transaction = transaction(TransactionType.INCOME, TransactionCategory.SALARY, "500.00");
        RecordingOutboxService outboxService = new RecordingOutboxService();
        TransactionService transactionService = new TransactionService(
                transactionRepository.repository(),
                new FixedCurrentUserService(),
                outboxService
        );

        TransactionResponse response = transactionService.updateTransaction(
                TRANSACTION_ID,
                new TransactionRequest(
                        NEW_ACCOUNT_ID,
                        TransactionType.EXPENSE,
                        TransactionCategory.FOOD,
                        new BigDecimal("45.00"),
                        "Dinner",
                        LocalDate.of(2026, 5, 2)
                )
        );

        TransactionLifecycleEvent event = outboxService.savedEvent;
        assertEquals(TransactionEventType.UPDATED, event.eventType());
        assertEquals(ACCOUNT_ID, event.oldAccountId());
        assertEquals(NEW_ACCOUNT_ID, event.newAccountId());
        assertEquals(TransactionType.INCOME, event.oldType());
        assertEquals(TransactionType.EXPENSE, event.newType());
        assertEquals(TransactionCategory.SALARY, event.oldCategory());
        assertEquals(TransactionCategory.FOOD, event.newCategory());
        assertEquals(new BigDecimal("500.00"), event.oldAmount());
        assertEquals(new BigDecimal("45.00"), event.newAmount());
        assertEquals(NEW_ACCOUNT_ID, response.accountId());
    }

    @Test
    void deleteTransactionWritesDeleteEventBeforeDeleting() {
        FakeTransactionRepository transactionRepository = new FakeTransactionRepository();
        FinancialTransaction transaction = transaction(TransactionType.INCOME, TransactionCategory.SALARY, "500.00");
        transactionRepository.transaction = transaction;
        RecordingOutboxService outboxService = new RecordingOutboxService();
        TransactionService transactionService = new TransactionService(
                transactionRepository.repository(),
                new FixedCurrentUserService(),
                outboxService
        );

        transactionService.deleteTransaction(TRANSACTION_ID);

        TransactionLifecycleEvent event = outboxService.savedEvent;
        assertEquals(TransactionEventType.DELETED, event.eventType());
        assertEquals(TransactionType.INCOME, event.oldType());
        assertEquals(new BigDecimal("500.00"), event.oldAmount());
        assertEquals(1, transactionRepository.deleteCount);
    }

    private static FinancialTransaction transaction(
            TransactionType type,
            TransactionCategory category,
            String amount
    ) {
        return FinancialTransaction.builder()
                .id(TRANSACTION_ID)
                .userId(USER_ID)
                .accountId(ACCOUNT_ID)
                .type(type)
                .category(category)
                .amount(new BigDecimal(amount))
                .description("Test transaction")
                .transactionDate(LocalDate.of(2026, 5, 1))
                .createdAt(LocalDateTime.now())
                .build();
    }

    private static BigDecimal totalFor(
            List<CategoryExpenseSummaryResponse> summaries,
            TransactionCategory category
    ) {
        return summaries.stream()
                .filter(summary -> summary.category() == category)
                .findFirst()
                .orElseThrow()
                .totalExpense();
    }

    private static final class FakeTransactionRepository {
        private FinancialTransaction transaction;
        private List<FinancialTransaction> monthlyTransactions = List.of();
        private int deleteCount;

        private TransactionRepository repository() {
            return (TransactionRepository) Proxy.newProxyInstance(
                    TransactionRepository.class.getClassLoader(),
                    new Class<?>[]{TransactionRepository.class},
                    (proxy, method, args) -> switch (method.getName()) {
                        case "findByIdAndUserId" -> Optional.ofNullable(transaction);
                        case "findByUserIdAndTransactionDateBetweenOrderByTransactionDateDesc" -> monthlyTransactions;
                        case "save" -> {
                            FinancialTransaction saved = (FinancialTransaction) args[0];
                            saved.setId(TRANSACTION_ID);
                            transaction = saved;
                            yield saved;
                        }
                        case "delete" -> {
                            deleteCount++;
                            yield null;
                        }
                        case "toString" -> "FakeTransactionRepository";
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

    private static final class RecordingOutboxService extends OutboxService {
        private TransactionLifecycleEvent savedEvent;

        private RecordingOutboxService() {
            super(null, null);
        }

        @Override
        public void saveTransactionLifecycleEvent(TransactionLifecycleEvent event) {
            savedEvent = event;
        }
    }
}

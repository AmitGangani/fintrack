package com.amit.fintrack.account.service;

import com.amit.fintrack.account.entity.ProcessedKafkaEvent;
import com.amit.fintrack.account.event.TransactionEventType;
import com.amit.fintrack.account.event.TransactionLifecycleEvent;
import com.amit.fintrack.account.repository.ProcessedKafkaEventRepository;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AccountBalanceEventServiceTest {

    private static final UUID EVENT_ID = UUID.fromString("00000000-0000-0000-0000-000000000011");
    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000012");
    private static final UUID ACCOUNT_ID = UUID.fromString("00000000-0000-0000-0000-000000000013");
    private static final UUID TRANSACTION_ID = UUID.fromString("00000000-0000-0000-0000-000000000014");
    private static final UUID NEW_ACCOUNT_ID = UUID.fromString("00000000-0000-0000-0000-000000000015");

    @Test
    void createdExpenseDebitsAccountAndMarksEventProcessed() {
        RecordingAccountService accountService = new RecordingAccountService();
        FakeProcessedKafkaEventRepository eventRepository = new FakeProcessedKafkaEventRepository();
        AccountBalanceEventService service = new AccountBalanceEventService(
                accountService,
                eventRepository.repository()
        );

        service.handleTransactionEvent(createdExpenseEvent());

        assertEquals(ACCOUNT_ID, accountService.accountId);
        assertEquals(USER_ID, accountService.userId);
        assertEquals(new BigDecimal("-25.00"), accountService.amountChange);
        assertEquals(EVENT_ID, eventRepository.savedEvent.getEventId());
        assertEquals("TRANSACTION_CREATED", eventRepository.savedEvent.getEventType());
    }

    @Test
    void duplicateEventIsIgnored() {
        RecordingAccountService accountService = new RecordingAccountService();
        FakeProcessedKafkaEventRepository eventRepository = new FakeProcessedKafkaEventRepository();
        eventRepository.exists = true;
        AccountBalanceEventService service = new AccountBalanceEventService(
                accountService,
                eventRepository.repository()
        );

        service.handleTransactionEvent(createdExpenseEvent());

        assertEquals(0, accountService.adjustCount);
        assertEquals(0, eventRepository.saveCount);
    }

    @Test
    void updatedTransactionReversesOldAmountAndAppliesNewAmount() {
        RecordingAccountService accountService = new RecordingAccountService();
        FakeProcessedKafkaEventRepository eventRepository = new FakeProcessedKafkaEventRepository();
        AccountBalanceEventService service = new AccountBalanceEventService(
                accountService,
                eventRepository.repository()
        );

        service.handleTransactionEvent(updatedIncomeToExpenseEvent());

        assertEquals(2, accountService.adjustments.size());
        assertEquals(ACCOUNT_ID, accountService.adjustments.get(0).accountId());
        assertEquals(new BigDecimal("-100.00"), accountService.adjustments.get(0).amountChange());
        assertEquals(NEW_ACCOUNT_ID, accountService.adjustments.get(1).accountId());
        assertEquals(new BigDecimal("-25.00"), accountService.adjustments.get(1).amountChange());
        assertEquals("TRANSACTION_UPDATED", eventRepository.savedEvent.getEventType());
    }

    @Test
    void deletedExpenseRestoresAccountBalance() {
        RecordingAccountService accountService = new RecordingAccountService();
        FakeProcessedKafkaEventRepository eventRepository = new FakeProcessedKafkaEventRepository();
        AccountBalanceEventService service = new AccountBalanceEventService(
                accountService,
                eventRepository.repository()
        );

        service.handleTransactionEvent(deletedExpenseEvent());

        assertEquals(1, accountService.adjustments.size());
        assertEquals(ACCOUNT_ID, accountService.adjustments.getFirst().accountId());
        assertEquals(new BigDecimal("25.00"), accountService.adjustments.getFirst().amountChange());
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
                "FOOD",
                new BigDecimal("25.00"),
                LocalDate.of(2026, 5, 1),
                LocalDateTime.now()
        );
    }

    private static TransactionLifecycleEvent updatedIncomeToExpenseEvent() {
        return new TransactionLifecycleEvent(
                EVENT_ID,
                TransactionEventType.UPDATED,
                TRANSACTION_ID,
                USER_ID,
                ACCOUNT_ID,
                NEW_ACCOUNT_ID,
                "INCOME",
                "SALARY",
                new BigDecimal("100.00"),
                LocalDate.of(2026, 5, 1),
                "EXPENSE",
                "FOOD",
                new BigDecimal("25.00"),
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
                "FOOD",
                new BigDecimal("25.00"),
                LocalDate.of(2026, 5, 1),
                null,
                null,
                null,
                null,
                LocalDateTime.now()
        );
    }

    private static final class RecordingAccountService extends AccountService {
        private UUID accountId;
        private UUID userId;
        private BigDecimal amountChange;
        private int adjustCount;
        private final List<Adjustment> adjustments = new ArrayList<>();

        private RecordingAccountService() {
            super(null, null);
        }

        @Override
        public void adjustBalanceFromEvent(UUID accountId, UUID userId, BigDecimal amountChange) {
            this.accountId = accountId;
            this.userId = userId;
            this.amountChange = amountChange;
            adjustCount++;
            adjustments.add(new Adjustment(accountId, userId, amountChange));
        }
    }

    private record Adjustment(UUID accountId, UUID userId, BigDecimal amountChange) {
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
}

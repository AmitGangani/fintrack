package com.amit.fintrack.account.application;

import com.amit.fintrack.account.application.model.TransactionBalanceEvent;
import com.amit.fintrack.account.application.port.ProcessedEventStore;
import com.amit.fintrack.account.domain.TransactionBalancePolicy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountBalanceEventService {

    private final AccountService accountService;
    private final ProcessedEventStore processedEventStore;
    private final TransactionBalancePolicy transactionBalancePolicy;

    @Transactional
    public void handleTransactionEvent(TransactionBalanceEvent event) {
        if (isAlreadyProcessed(event)) {
            return;
        }

        switch (event.eventType()) {
            case CREATED -> applyAmount(event.userId(), event.newAccountId(), event.newType(), event.newAmount());
            case UPDATED -> {
                reverseAmount(event.userId(), event.oldAccountId(), event.oldType(), event.oldAmount());
                applyAmount(event.userId(), event.newAccountId(), event.newType(), event.newAmount());
            }
            case DELETED -> reverseAmount(event.userId(), event.oldAccountId(), event.oldType(), event.oldAmount());
        }

        processedEventStore.markProcessed(event.eventId(), "TRANSACTION_" + event.eventType().name());
        log.info("Processed account balance event: {}", event.eventId());
    }

    private void applyAmount(UUID userId, UUID accountId, String type, BigDecimal amount) {
        adjustAccountBalance(userId, accountId, type, amount, false);
    }

    private void reverseAmount(UUID userId, UUID accountId, String type, BigDecimal amount) {
        adjustAccountBalance(userId, accountId, type, amount, true);
    }

    private void adjustAccountBalance(
            UUID userId,
            UUID accountId,
            String type,
            BigDecimal amount,
            boolean reverse
    ) {
        if (accountId == null) {
            return;
        }

        transactionBalancePolicy.amountChange(type, amount)
                .map(change -> reverse ? change.negate() : change)
                .ifPresentOrElse(
                        change -> accountService.adjustBalanceFromEvent(accountId, userId, change),
                        () -> log.warn("Skipping unsupported account balance event type: {}", type)
                );
    }

    private boolean isAlreadyProcessed(TransactionBalanceEvent event) {
        if (processedEventStore.exists(event.eventId())) {
            log.info("Skipping duplicate account event: {}", event.eventId());
            return true;
        }

        return false;
    }
}

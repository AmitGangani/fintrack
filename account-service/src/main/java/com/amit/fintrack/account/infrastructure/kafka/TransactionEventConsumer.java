package com.amit.fintrack.account.infrastructure.kafka;

import com.amit.fintrack.account.application.AccountBalanceEventService;
import com.amit.fintrack.account.application.model.TransactionBalanceEvent;
import com.amit.fintrack.account.application.model.TransactionEventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionEventConsumer {

    private final AccountBalanceEventService accountBalanceEventService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = KafkaTopics.TRANSACTION_EVENTS,
            groupId = "account-service-group"
    )
    public void handleTransactionLifecycleEvent(String payload) {
        try {
            TransactionLifecycleEvent event = objectMapper.readValue(
                    payload,
                    TransactionLifecycleEvent.class
            );

            log.info("Account Service received transaction lifecycle event: {}", event);
            accountBalanceEventService.handleTransactionEvent(toApplicationEvent(event));

        } catch (Exception exception) {
            log.error("Failed to process transaction lifecycle event payload={}", payload, exception);
            throw new RuntimeException(exception);
        }
    }

    private TransactionBalanceEvent toApplicationEvent(TransactionLifecycleEvent event) {
        return new TransactionBalanceEvent(
                event.eventId(),
                TransactionEventType.valueOf(event.eventType().name()),
                event.userId(),
                event.oldAccountId(),
                event.newAccountId(),
                event.oldType(),
                event.oldAmount(),
                event.newType(),
                event.newAmount()
        );
    }
}

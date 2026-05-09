package com.amit.fintrack.account.event;

import com.amit.fintrack.account.service.AccountBalanceEventService;
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
    public void handleTransactionEvent(String payload) {
        try {
            TransactionLifecycleEvent event = objectMapper.readValue(
                    payload,
                    TransactionLifecycleEvent.class
            );

            log.info("Account Service received transaction event: {}", event);

            accountBalanceEventService.handleTransactionEvent(event);

        } catch (Exception exception) {
            log.error("Failed to process account transaction event payload={}", payload, exception);
            throw new RuntimeException(exception);
        }
    }
}
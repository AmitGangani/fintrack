package com.amit.fintrack.budget.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TransactionEventConsumer {

    @KafkaListener(
            topics = KafkaTopics.TRANSACTION_EVENTS,
            groupId = "budget-service-group"
    )
    public void handleTransactionCreated(TransactionCreatedEvent event) {
        log.info("Budget Service received transaction event: {}", event);
    }
}
package com.amit.fintrack.transaction.event;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransactionEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishTransactionLifecycleEvent(TransactionLifecycleEvent event) {
        kafkaTemplate.send(
                KafkaTopics.TRANSACTION_EVENTS,
                event.userId().toString(),
                event
        );
    }
}

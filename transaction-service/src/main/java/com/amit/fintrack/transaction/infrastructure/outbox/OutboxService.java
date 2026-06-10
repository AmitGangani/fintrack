package com.amit.fintrack.transaction.infrastructure.outbox;

import com.amit.fintrack.transaction.application.model.TransactionLifecycleEvent;
import com.amit.fintrack.transaction.application.port.TransactionEventOutbox;
import com.amit.fintrack.transaction.infrastructure.kafka.KafkaTopics;
import com.amit.fintrack.transaction.persistence.entity.OutboxEvent;
import com.amit.fintrack.transaction.persistence.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OutboxService implements TransactionEventOutbox {

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void save(TransactionLifecycleEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);

            OutboxEvent outboxEvent = OutboxEvent.builder()
                    .id(event.eventId())
                    .aggregateType("TRANSACTION")
                    .aggregateId(event.transactionId())
                    .eventType(event.eventType().name())
                    .topic(KafkaTopics.TRANSACTION_EVENTS)
                    .payload(payload)
                    .published(false)
                    .createdAt(LocalDateTime.now())
                    .build();

            outboxEventRepository.save(outboxEvent);

        } catch (Exception exception) {
            throw new RuntimeException("Failed to serialize transaction event", exception);
        }
    }
}

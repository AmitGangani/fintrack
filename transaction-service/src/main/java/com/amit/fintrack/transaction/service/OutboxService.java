package com.amit.fintrack.transaction.service;

import com.amit.fintrack.transaction.entity.OutboxEvent;
import com.amit.fintrack.transaction.event.KafkaTopics;
import com.amit.fintrack.transaction.event.TransactionLifecycleEvent;
import com.amit.fintrack.transaction.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OutboxService {

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public void saveTransactionLifecycleEvent(TransactionLifecycleEvent event) {
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
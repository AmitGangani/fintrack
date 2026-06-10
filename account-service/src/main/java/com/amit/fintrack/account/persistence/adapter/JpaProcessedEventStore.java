package com.amit.fintrack.account.persistence.adapter;

import com.amit.fintrack.account.application.port.ProcessedEventStore;
import com.amit.fintrack.account.persistence.entity.ProcessedKafkaEvent;
import com.amit.fintrack.account.persistence.repository.ProcessedKafkaEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JpaProcessedEventStore implements ProcessedEventStore {

    private final ProcessedKafkaEventRepository processedKafkaEventRepository;

    @Override
    public boolean exists(UUID eventId) {
        return processedKafkaEventRepository.existsById(eventId);
    }

    @Override
    public void markProcessed(UUID eventId, String eventType) {
        processedKafkaEventRepository.save(ProcessedKafkaEvent.builder()
                .eventId(eventId)
                .eventType(eventType)
                .processedAt(LocalDateTime.now())
                .build());
    }
}

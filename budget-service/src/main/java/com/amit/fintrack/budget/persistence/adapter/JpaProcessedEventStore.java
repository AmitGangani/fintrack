package com.amit.fintrack.budget.persistence.adapter;

import com.amit.fintrack.budget.application.port.ProcessedEventStore;
import com.amit.fintrack.budget.persistence.entity.ProcessedKafkaEvent;
import com.amit.fintrack.budget.persistence.repository.ProcessedKafkaEventRepository;
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

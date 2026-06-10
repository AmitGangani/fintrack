package com.amit.fintrack.analytics.persistence.adapter;

import com.amit.fintrack.analytics.application.port.ProcessedEventStore;
import com.amit.fintrack.analytics.persistence.entity.ProcessedKafkaEvent;
import com.amit.fintrack.analytics.persistence.repository.ProcessedKafkaEventRepository;
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

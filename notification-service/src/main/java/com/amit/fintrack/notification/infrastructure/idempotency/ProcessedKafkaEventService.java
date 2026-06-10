package com.amit.fintrack.notification.infrastructure.idempotency;

import com.amit.fintrack.notification.application.port.ProcessedEventStore;
import com.amit.fintrack.notification.persistence.entity.ProcessedKafkaEvent;
import com.amit.fintrack.notification.persistence.repository.ProcessedKafkaEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProcessedKafkaEventService implements ProcessedEventStore {

    private final ProcessedKafkaEventRepository processedKafkaEventRepository;
    private final Clock clock;

    @Override
    public boolean reserve(UUID eventId, String eventType) {
        if (processedKafkaEventRepository.existsById(eventId)) {
            log.info("Skipping duplicate notification event: {}", eventId);
            return false;
        }

        processedKafkaEventRepository.save(ProcessedKafkaEvent.builder()
                .eventId(eventId)
                .eventType(normalizeEventType(eventType))
                .processedAt(LocalDateTime.now(clock))
                .build());
        return true;
    }

    private String normalizeEventType(String eventType) {
        return eventType == null || eventType.isBlank() ? "UNKNOWN" : eventType;
    }
}

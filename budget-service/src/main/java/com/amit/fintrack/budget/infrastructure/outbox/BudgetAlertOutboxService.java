package com.amit.fintrack.budget.infrastructure.outbox;

import com.amit.fintrack.budget.application.model.BudgetAlertEvent;
import com.amit.fintrack.budget.application.port.BudgetAlertOutbox;
import com.amit.fintrack.budget.infrastructure.kafka.KafkaTopics;
import com.amit.fintrack.budget.persistence.entity.BudgetAlertOutboxEvent;
import com.amit.fintrack.budget.persistence.repository.BudgetAlertOutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class BudgetAlertOutboxService implements BudgetAlertOutbox {

    private final BudgetAlertOutboxEventRepository budgetAlertOutboxEventRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void save(BudgetAlertEvent event) {
        try {
            BudgetAlertOutboxEvent outboxEvent = BudgetAlertOutboxEvent.builder()
                    .id(event.eventId())
                    .aggregateType("BUDGET_ALERT")
                    .aggregateId(event.userId())
                    .eventType(event.alertType())
                    .topic(KafkaTopics.BUDGET_ALERT_EVENTS)
                    .payload(objectMapper.writeValueAsString(event))
                    .published(false)
                    .createdAt(LocalDateTime.now())
                    .build();

            budgetAlertOutboxEventRepository.save(outboxEvent);
        } catch (Exception exception) {
            throw new RuntimeException("Failed to serialize budget alert event", exception);
        }
    }
}

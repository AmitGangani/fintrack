package com.amit.fintrack.budget.infrastructure.outbox;

import com.amit.fintrack.budget.persistence.entity.BudgetAlertOutboxEvent;
import com.amit.fintrack.budget.persistence.repository.BudgetAlertOutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class BudgetAlertOutboxPublisher {

    private final BudgetAlertOutboxEventRepository budgetAlertOutboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void publishPendingEvents() {
        List<BudgetAlertOutboxEvent> events = budgetAlertOutboxEventRepository
                .findPendingForPublish();

        for (BudgetAlertOutboxEvent event : events) {
            publish(event);
        }
    }

    private void publish(BudgetAlertOutboxEvent event) {
        try {
            kafkaTemplate.send(
                    event.getTopic(),
                    event.getAggregateId().toString(),
                    event.getPayload()
            ).get();

            event.setPublished(true);
            event.setPublishedAt(LocalDateTime.now());
            budgetAlertOutboxEventRepository.save(event);

            log.info(
                    "Published budget alert outbox event. eventId={}, eventType={}, topic={}",
                    event.getId(),
                    event.getEventType(),
                    event.getTopic()
            );
        } catch (Exception exception) {
            log.error("Failed to publish budget alert outbox event. eventId={}", event.getId(), exception);
        }
    }
}

package com.amit.fintrack.notification.infrastructure.kafka;

import com.amit.fintrack.notification.application.BudgetAlertNotificationHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
@Slf4j
public class BudgetAlertEventConsumer {

    private final ObjectMapper objectMapper;
    private final BudgetAlertNotificationHandler budgetAlertNotificationHandler;

    @KafkaListener(
            topics = KafkaTopics.BUDGET_ALERT_EVENTS,
            groupId = "notification-service-group"
    )
    public void consume(String payload) {
        try {
            BudgetAlertEvent event = objectMapper.readValue(
                    payload,
                    BudgetAlertEvent.class
            );

            log.info("Notification Service received budget alert event: {}", event);
            budgetAlertNotificationHandler.handle(toApplicationEvent(event));
        } catch (Exception exception) {
            log.error("Failed to process budget alert event payload={}", payload, exception);
            throw new RuntimeException(exception);
        }
    }

    private com.amit.fintrack.notification.application.model.BudgetAlertEvent toApplicationEvent(BudgetAlertEvent event) {
        return new com.amit.fintrack.notification.application.model.BudgetAlertEvent(
                event.eventId(),
                event.userId(),
                event.alertType(),
                event.message()
        );
    }
}

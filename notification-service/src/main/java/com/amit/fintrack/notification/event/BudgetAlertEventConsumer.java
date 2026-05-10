package com.amit.fintrack.notification.event;

import com.amit.fintrack.notification.service.NotificationService;
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
    private final NotificationService notificationService;

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

            notificationService.handleBudgetAlertEvent(event);

        } catch (Exception exception) {
            log.error("Failed to process budget alert event payload={}", payload, exception);
            throw new RuntimeException(exception);
        }
    }
}
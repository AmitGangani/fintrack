package com.amit.fintrack.budget.event;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class BudgetAlertProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void publish(BudgetAlertEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);

            kafkaTemplate.send(
                    KafkaTopics.BUDGET_ALERT_EVENTS,
                    event.userId().toString(),
                    payload
            );

        } catch (Exception exception) {
            throw new RuntimeException("Failed to serialize budget alert event", exception);
        }
    }
}
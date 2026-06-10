package com.amit.fintrack.budget.infrastructure.kafka;

import com.amit.fintrack.budget.application.BudgetSpendingService;
import com.amit.fintrack.budget.application.model.TransactionBudgetEvent;
import com.amit.fintrack.budget.application.model.TransactionEventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionEventConsumer {

    private final BudgetSpendingService budgetSpendingService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = KafkaTopics.TRANSACTION_EVENTS,
            groupId = "budget-service-group"
    )
    public void handleTransactionLifecycleEvent(String payload) {
        try {
            TransactionLifecycleEvent event = objectMapper.readValue(
                    payload,
                    TransactionLifecycleEvent.class
            );

            log.info("Budget Service received transaction lifecycle event: {}", event);
            budgetSpendingService.handleTransactionLifecycleEvent(toApplicationEvent(event));

        } catch (Exception exception) {
            log.error("Failed to process transaction lifecycle event payload={}", payload, exception);
            throw new RuntimeException(exception);
        }
    }

    private TransactionBudgetEvent toApplicationEvent(TransactionLifecycleEvent event) {
        return new TransactionBudgetEvent(
                event.eventId(),
                TransactionEventType.valueOf(event.eventType().name()),
                event.userId(),
                event.oldType(),
                event.oldCategory(),
                event.oldAmount(),
                event.oldTransactionDate(),
                event.newType(),
                event.newCategory(),
                event.newAmount(),
                event.newTransactionDate()
        );
    }
}

package com.amit.fintrack.budget.event;

import com.amit.fintrack.budget.service.BudgetSpendingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionEventConsumer {

    private final BudgetSpendingService budgetSpendingService;

    @KafkaListener(
            topics = KafkaTopics.TRANSACTION_EVENTS,
            groupId = "budget-service-group"
    )
    public void handleTransactionBudgetEvent(TransactionBudgetEvent event) {
        log.info("Budget Service received transaction budget event: {}", event);
        budgetSpendingService.handleTransactionBudgetEvent(event);
    }
}
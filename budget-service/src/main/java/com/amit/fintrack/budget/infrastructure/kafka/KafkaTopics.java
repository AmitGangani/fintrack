package com.amit.fintrack.budget.infrastructure.kafka;

public final class KafkaTopics {

    private KafkaTopics() {
    }

    public static final String TRANSACTION_EVENTS = "transaction-events";
    public static final String BUDGET_ALERT_EVENTS = "budget-alert-events";
}
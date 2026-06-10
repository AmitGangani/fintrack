package com.amit.fintrack.analytics.application.port;

import java.util.UUID;

public interface ProcessedEventStore {

    boolean exists(UUID eventId);

    void markProcessed(UUID eventId, String eventType);
}

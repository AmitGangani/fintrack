package com.amit.fintrack.notification.application.port;

import java.util.UUID;

public interface ProcessedEventStore {

    boolean reserve(UUID eventId, String eventType);
}

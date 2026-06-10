package com.amit.fintrack.authservice.application.model;

import java.util.UUID;

public record UserRecord(
        UUID id,
        String email
) {
}

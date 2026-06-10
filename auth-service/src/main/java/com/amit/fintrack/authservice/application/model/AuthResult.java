package com.amit.fintrack.authservice.application.model;

import java.util.UUID;

public record AuthResult(
        UUID userId,
        String email,
        String accessToken,
        String message
) {
}

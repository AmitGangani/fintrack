package com.amit.fintrack.authservice.application.model;

import java.time.LocalDateTime;

public record NewUser(
        String fullName,
        String email,
        String encodedPassword,
        LocalDateTime createdAt
) {
}

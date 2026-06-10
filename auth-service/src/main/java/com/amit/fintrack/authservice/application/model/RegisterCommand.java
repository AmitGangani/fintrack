package com.amit.fintrack.authservice.application.model;

public record RegisterCommand(
        String fullName,
        String email,
        String password
) {
}

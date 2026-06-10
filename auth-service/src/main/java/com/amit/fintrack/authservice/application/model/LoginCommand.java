package com.amit.fintrack.authservice.application.model;

public record LoginCommand(
        String email,
        String password
) {
}

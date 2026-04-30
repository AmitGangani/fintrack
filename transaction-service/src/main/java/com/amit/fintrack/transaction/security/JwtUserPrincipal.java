package com.amit.fintrack.transaction.security;

import java.util.UUID;

public record JwtUserPrincipal(
        UUID userId,
        String email,
        String role
) {
}
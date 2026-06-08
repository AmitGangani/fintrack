package com.amit.fintrack.analytics.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CurrentUserService {

    public UUID getCurrentUserId() {
        return UUID.fromString(currentJwt().getClaimAsString("userId"));
    }

    private Jwt currentJwt() {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        return (Jwt) authentication.getPrincipal();
    }
}

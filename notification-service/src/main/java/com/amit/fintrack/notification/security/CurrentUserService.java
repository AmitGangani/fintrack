package com.amit.fintrack.notification.security;

import com.amit.fintrack.notification.application.port.CurrentUserProvider;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CurrentUserService implements CurrentUserProvider {

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

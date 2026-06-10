package com.amit.fintrack.authservice.domain;

import com.amit.fintrack.authservice.application.model.NewUser;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class AppUserFactory {

    public NewUser create(String fullName, String normalizedEmail, String encodedPassword) {
        return new NewUser(
                fullName,
                normalizedEmail,
                encodedPassword,
                LocalDateTime.now()
        );
    }
}

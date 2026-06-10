package com.amit.fintrack.authservice.application.port;

public interface PasswordHasher {

    String hash(String password);
}

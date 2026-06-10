package com.amit.fintrack.authservice.application.port;

public interface CredentialAuthenticator {

    void authenticate(String email, String password);
}

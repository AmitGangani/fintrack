package com.amit.fintrack.authservice.application;

import com.amit.fintrack.authservice.application.model.AuthResult;
import com.amit.fintrack.authservice.application.model.LoginCommand;
import com.amit.fintrack.authservice.application.model.NewUser;
import com.amit.fintrack.authservice.application.model.RegisterCommand;
import com.amit.fintrack.authservice.application.model.UserRecord;
import com.amit.fintrack.authservice.application.port.CredentialAuthenticator;
import com.amit.fintrack.authservice.application.port.PasswordHasher;
import com.amit.fintrack.authservice.application.port.TokenIssuer;
import com.amit.fintrack.authservice.application.port.UserStore;
import com.amit.fintrack.authservice.domain.AppUserFactory;
import com.amit.fintrack.authservice.domain.EmailNormalizer;
import com.amit.fintrack.authservice.exception.DuplicateEmailException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserStore userStore;
    private final CredentialAuthenticator credentialAuthenticator;
    private final EmailNormalizer emailNormalizer;
    private final AppUserFactory appUserFactory;
    private final PasswordHasher passwordHasher;
    private final TokenIssuer tokenIssuer;

    public AuthResult register(RegisterCommand command) {
        String email = emailNormalizer.normalize(command.email());

        if (userStore.existsByEmail(email)) {
            throw new DuplicateEmailException("Email already registered");
        }

        NewUser newUser = appUserFactory.create(
                command.fullName(),
                email,
                passwordHasher.hash(command.password())
        );
        UserRecord savedUser = userStore.save(newUser);

        return toResult(savedUser, "User registered successfully");
    }

    public AuthResult login(LoginCommand command) {
        String email = emailNormalizer.normalize(command.email());

        credentialAuthenticator.authenticate(email, command.password());

        UserRecord user = userStore.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        return toResult(user, "Login successful");
    }

    private AuthResult toResult(UserRecord user, String message) {
        return new AuthResult(
                user.id(),
                user.email(),
                tokenIssuer.issueToken(user),
                message
        );
    }
}

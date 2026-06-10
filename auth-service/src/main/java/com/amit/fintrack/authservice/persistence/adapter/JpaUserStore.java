package com.amit.fintrack.authservice.persistence.adapter;

import com.amit.fintrack.authservice.application.model.NewUser;
import com.amit.fintrack.authservice.application.model.UserRecord;
import com.amit.fintrack.authservice.application.port.UserStore;
import com.amit.fintrack.authservice.persistence.entity.AppUser;
import com.amit.fintrack.authservice.persistence.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JpaUserStore implements UserStore {

    private final UserRepository userRepository;

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public UserRecord save(NewUser user) {
        AppUser entity = AppUser.builder()
                .fullName(user.fullName())
                .email(user.email())
                .password(user.encodedPassword())
                .createdAt(user.createdAt())
                .build();

        return toRecord(userRepository.save(entity));
    }

    @Override
    public Optional<UserRecord> findByEmail(String email) {
        return userRepository.findByEmail(email).map(this::toRecord);
    }

    private UserRecord toRecord(AppUser user) {
        return new UserRecord(user.getId(), user.getEmail());
    }
}

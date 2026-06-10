package com.amit.fintrack.authservice.application.port;

import com.amit.fintrack.authservice.application.model.NewUser;
import com.amit.fintrack.authservice.application.model.UserRecord;

import java.util.Optional;

public interface UserStore {

    boolean existsByEmail(String email);

    UserRecord save(NewUser user);

    Optional<UserRecord> findByEmail(String email);
}

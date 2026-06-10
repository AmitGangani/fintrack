package com.amit.fintrack.authservice.application.port;

import com.amit.fintrack.authservice.application.model.UserRecord;

public interface TokenIssuer {

    String issueToken(UserRecord user);
}

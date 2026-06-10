package com.amit.fintrack.account.application.port;

import java.util.UUID;

public interface CurrentUserProvider {

    UUID getCurrentUserId();
}

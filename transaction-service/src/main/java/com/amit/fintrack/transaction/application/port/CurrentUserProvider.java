package com.amit.fintrack.transaction.application.port;

import java.util.UUID;

public interface CurrentUserProvider {

    UUID getCurrentUserId();
}

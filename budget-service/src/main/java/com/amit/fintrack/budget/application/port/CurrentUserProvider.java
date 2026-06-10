package com.amit.fintrack.budget.application.port;

import java.util.UUID;

public interface CurrentUserProvider {

    UUID getCurrentUserId();
}

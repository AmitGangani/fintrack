package com.amit.fintrack.analytics.application.port;

import java.util.UUID;

public interface CurrentUserProvider {

    UUID getCurrentUserId();
}

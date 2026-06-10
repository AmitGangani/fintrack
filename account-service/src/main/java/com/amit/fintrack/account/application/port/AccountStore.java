package com.amit.fintrack.account.application.port;

import com.amit.fintrack.account.application.model.AccountCommand;
import com.amit.fintrack.account.application.model.AccountView;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountStore {

    AccountView create(UUID userId, AccountCommand command);

    List<AccountView> findByUserId(UUID userId);

    Optional<AccountView> findByIdAndUserId(UUID accountId, UUID userId);

    AccountView update(UUID accountId, UUID userId, AccountCommand command);

    void delete(UUID accountId, UUID userId);

    AccountView adjustBalance(UUID accountId, UUID userId, BigDecimal amountChange);
}

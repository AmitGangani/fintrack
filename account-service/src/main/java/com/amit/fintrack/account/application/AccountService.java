package com.amit.fintrack.account.application;

import com.amit.fintrack.account.application.model.AccountCommand;
import com.amit.fintrack.account.application.model.AccountView;
import com.amit.fintrack.account.application.port.AccountStore;
import com.amit.fintrack.account.exception.AccountNotFoundException;
import com.amit.fintrack.account.application.port.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountStore accountStore;
    private final CurrentUserProvider currentUserProvider;

    public AccountView createAccount(AccountCommand command) {
        return accountStore.create(currentUserProvider.getCurrentUserId(), command);
    }

    public List<AccountView> getMyAccounts() {
        return accountStore.findByUserId(currentUserProvider.getCurrentUserId());
    }

    public AccountView getAccountById(UUID accountId) {
        return getCurrentUserAccount(accountId);
    }

    public AccountView updateAccount(UUID accountId, AccountCommand command) {
        return accountStore.update(accountId, currentUserProvider.getCurrentUserId(), command);
    }

    public void deleteAccount(UUID accountId) {
        accountStore.delete(accountId, currentUserProvider.getCurrentUserId());
    }

    public AccountView adjustBalance(UUID accountId, BigDecimal amountChange) {
        return accountStore.adjustBalance(accountId, currentUserProvider.getCurrentUserId(), amountChange);
    }

    public void adjustBalanceFromEvent(UUID accountId, UUID userId, BigDecimal amountChange) {
        accountStore.adjustBalance(accountId, userId, amountChange);
    }

    private AccountView getCurrentUserAccount(UUID accountId) {
        return accountStore.findByIdAndUserId(accountId, currentUserProvider.getCurrentUserId())
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));
    }
}

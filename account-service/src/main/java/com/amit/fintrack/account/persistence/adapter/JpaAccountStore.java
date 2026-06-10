package com.amit.fintrack.account.persistence.adapter;

import com.amit.fintrack.account.application.model.AccountCommand;
import com.amit.fintrack.account.application.model.AccountView;
import com.amit.fintrack.account.application.port.AccountStore;
import com.amit.fintrack.account.exception.AccountNotFoundException;
import com.amit.fintrack.account.persistence.entity.Account;
import com.amit.fintrack.account.persistence.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JpaAccountStore implements AccountStore {

    private final AccountRepository accountRepository;

    @Override
    public AccountView create(UUID userId, AccountCommand command) {
        LocalDateTime now = LocalDateTime.now();
        Account account = Account.builder()
                .userId(userId)
                .name(command.name())
                .type(command.type())
                .balance(command.balance())
                .currency(normalizeCurrency(command.currency()))
                .createdAt(now)
                .updatedAt(now)
                .build();

        return toView(accountRepository.save(account));
    }

    @Override
    public List<AccountView> findByUserId(UUID userId) {
        return accountRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toView)
                .toList();
    }

    @Override
    public Optional<AccountView> findByIdAndUserId(UUID accountId, UUID userId) {
        return accountRepository.findByIdAndUserId(accountId, userId).map(this::toView);
    }

    @Override
    public AccountView update(UUID accountId, UUID userId, AccountCommand command) {
        Account account = findAccount(accountId, userId);
        account.setName(command.name());
        account.setType(command.type());
        account.setBalance(command.balance());
        account.setCurrency(normalizeCurrency(command.currency()));
        account.setUpdatedAt(LocalDateTime.now());

        return toView(accountRepository.save(account));
    }

    @Override
    public void delete(UUID accountId, UUID userId) {
        accountRepository.delete(findAccount(accountId, userId));
    }

    @Override
    public AccountView adjustBalance(UUID accountId, UUID userId, BigDecimal amountChange) {
        Account account = findAccount(accountId, userId);
        account.setBalance(account.getBalance().add(amountChange));
        account.setUpdatedAt(LocalDateTime.now());

        return toView(accountRepository.save(account));
    }

    private Account findAccount(UUID accountId, UUID userId) {
        return accountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));
    }

    private AccountView toView(Account account) {
        return new AccountView(
                account.getId(),
                account.getName(),
                account.getType(),
                account.getBalance(),
                account.getCurrency(),
                account.getCreatedAt(),
                account.getUpdatedAt()
        );
    }

    private String normalizeCurrency(String currency) {
        return currency.toUpperCase(Locale.ROOT);
    }
}

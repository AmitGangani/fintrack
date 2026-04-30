package com.amit.fintrack.account.service;

import com.amit.fintrack.account.dto.AccountRequest;
import com.amit.fintrack.account.dto.AccountResponse;
import com.amit.fintrack.account.entity.Account;
import com.amit.fintrack.account.exception.AccountNotFoundException;
import com.amit.fintrack.account.repository.AccountRepository;
import com.amit.fintrack.account.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final CurrentUserService currentUserService;

    public AccountResponse createAccount(AccountRequest request) {
        UUID currentUserId = currentUserService.getCurrentUserId();

        Account account = Account.builder()
                .userId(currentUserId)
                .name(request.name())
                .type(request.type())
                .balance(request.balance())
                .currency(request.currency().toUpperCase())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Account savedAccount = accountRepository.save(account);

        return toResponse(savedAccount);
    }

    public List<AccountResponse> getMyAccounts() {
        UUID currentUserId = currentUserService.getCurrentUserId();

        return accountRepository.findByUserIdOrderByCreatedAtDesc(currentUserId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public AccountResponse getAccountById(UUID accountId) {
        UUID currentUserId = currentUserService.getCurrentUserId();

        Account account = accountRepository.findByIdAndUserId(accountId, currentUserId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));

        return toResponse(account);
    }

    public AccountResponse updateAccount(UUID accountId, AccountRequest request) {
        UUID currentUserId = currentUserService.getCurrentUserId();

        Account account = accountRepository.findByIdAndUserId(accountId, currentUserId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));

        account.setName(request.name());
        account.setType(request.type());
        account.setBalance(request.balance());
        account.setCurrency(request.currency().toUpperCase());
        account.setUpdatedAt(LocalDateTime.now());

        Account updatedAccount = accountRepository.save(account);

        return toResponse(updatedAccount);
    }

    public void deleteAccount(UUID accountId) {
        UUID currentUserId = currentUserService.getCurrentUserId();

        Account account = accountRepository.findByIdAndUserId(accountId, currentUserId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));

        accountRepository.delete(account);
    }

    public AccountResponse adjustBalance(UUID accountId, BigDecimal amountChange) {
        UUID currentUserId = currentUserService.getCurrentUserId();

        Account account = accountRepository.findByIdAndUserId(accountId, currentUserId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));

        BigDecimal newBalance = account.getBalance().add(amountChange);

        account.setBalance(newBalance);
        account.setUpdatedAt(LocalDateTime.now());

        Account updatedAccount = accountRepository.save(account);

        return toResponse(updatedAccount);
    }

    private AccountResponse toResponse(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getName(),
                account.getType(),
                account.getBalance(),
                account.getCurrency(),
                account.getCreatedAt(),
                account.getUpdatedAt()
        );
    }
}
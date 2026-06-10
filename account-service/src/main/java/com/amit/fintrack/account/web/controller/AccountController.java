package com.amit.fintrack.account.web.controller;

import com.amit.fintrack.account.application.AccountService;
import com.amit.fintrack.account.application.model.AccountCommand;
import com.amit.fintrack.account.application.model.AccountView;
import com.amit.fintrack.account.web.dto.AccountRequest;
import com.amit.fintrack.account.web.dto.AccountResponse;
import com.amit.fintrack.account.web.dto.BalanceAdjustmentRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(
            @Valid @RequestBody AccountRequest request
    ) {
        AccountResponse response = toResponse(accountService.createAccount(toCommand(request)));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<AccountResponse>> getMyAccounts() {
        return ResponseEntity.ok(accountService.getMyAccounts().stream().map(this::toResponse).toList());
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<AccountResponse> getAccountById(
            @PathVariable UUID accountId
    ) {
        return ResponseEntity.ok(toResponse(accountService.getAccountById(accountId)));
    }

    @PutMapping("/{accountId}")
    public ResponseEntity<AccountResponse> updateAccount(
            @PathVariable UUID accountId,
            @Valid @RequestBody AccountRequest request
    ) {
        return ResponseEntity.ok(toResponse(accountService.updateAccount(accountId, toCommand(request))));
    }

    @DeleteMapping("/{accountId}")
    public ResponseEntity<Void> deleteAccount(
            @PathVariable UUID accountId
    ) {
        accountService.deleteAccount(accountId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{accountId}/balance")
    public ResponseEntity<AccountResponse> adjustBalance(
            @PathVariable UUID accountId,
            @Valid @RequestBody BalanceAdjustmentRequest request
    ) {
        return ResponseEntity.ok(toResponse(accountService.adjustBalance(accountId, request.amountChange())));
    }

    private AccountCommand toCommand(AccountRequest request) {
        return new AccountCommand(request.name(), request.type(), request.balance(), request.currency());
    }

    private AccountResponse toResponse(AccountView account) {
        return new AccountResponse(
                account.id(),
                account.name(),
                account.type(),
                account.balance(),
                account.currency(),
                account.createdAt(),
                account.updatedAt()
        );
    }
}

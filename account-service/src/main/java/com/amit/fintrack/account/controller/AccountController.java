package com.amit.fintrack.account.controller;

import com.amit.fintrack.account.dto.AccountRequest;
import com.amit.fintrack.account.dto.AccountResponse;
import com.amit.fintrack.account.dto.BalanceAdjustmentRequest;
import com.amit.fintrack.account.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Account APIs", description = "Manage user bank accounts, wallets, cash, and credit cards")
@SecurityRequirement(name = "bearerAuth")
public class AccountController {

    private final AccountService accountService;

    @Operation(summary = "Create a new account")
    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(
            @Valid @RequestBody AccountRequest request
    ) {
        AccountResponse response = accountService.createAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get all accounts of logged-in user")
    @GetMapping
    public ResponseEntity<List<AccountResponse>> getMyAccounts() {
        return ResponseEntity.ok(accountService.getMyAccounts());
    }

    @Operation(summary = "Get account by ID")
    @GetMapping("/{accountId}")
    public ResponseEntity<AccountResponse> getAccountById(
            @PathVariable UUID accountId
    ) {
        return ResponseEntity.ok(accountService.getAccountById(accountId));
    }

    @Operation(summary = "Update account")
    @PutMapping("/{accountId}")
    public ResponseEntity<AccountResponse> updateAccount(
            @PathVariable UUID accountId,
            @Valid @RequestBody AccountRequest request
    ) {
        return ResponseEntity.ok(accountService.updateAccount(accountId, request));
    }

    @Operation(summary = "Delete account")
    @DeleteMapping("/{accountId}")
    public ResponseEntity<Void> deleteAccount(
            @PathVariable UUID accountId
    ) {
        accountService.deleteAccount(accountId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Adjust account balance")
    @PatchMapping("/{accountId}/balance")
    public ResponseEntity<AccountResponse> adjustBalance(
            @PathVariable UUID accountId,
            @Valid @RequestBody BalanceAdjustmentRequest request
    ) {
        AccountResponse response = accountService.adjustBalance(
                accountId,
                request.amountChange()
        );

        return ResponseEntity.ok(response);
    }
}
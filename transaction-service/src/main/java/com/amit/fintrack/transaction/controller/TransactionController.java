package com.amit.fintrack.transaction.controller;

import com.amit.fintrack.transaction.dto.CategoryExpenseSummaryResponse;
import com.amit.fintrack.transaction.dto.MonthlySummaryResponse;
import com.amit.fintrack.transaction.dto.TransactionRequest;
import com.amit.fintrack.transaction.dto.TransactionResponse;
import com.amit.fintrack.transaction.service.TransactionService;
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

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Tag(name = "Transaction APIs", description = "Manage income, expenses, monthly summaries, and category reports")
@SecurityRequirement(name = "bearerAuth")
public class TransactionController {

    private final TransactionService transactionService;

    @Operation(summary = "Create income or expense transaction")
    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(
            @RequestHeader(AUTHORIZATION) String authorizationHeader,
            @Valid @RequestBody TransactionRequest request
    ) {
        TransactionResponse response = transactionService.createTransaction(
                request,
                authorizationHeader
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get all transactions of logged-in user")
    @GetMapping
    public ResponseEntity<List<TransactionResponse>> getMyTransactions() {
        return ResponseEntity.ok(transactionService.getMyTransactions());
    }

    @Operation(summary = "Get transaction by ID")
    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionResponse> getTransactionById(
            @PathVariable UUID transactionId
    ) {
        return ResponseEntity.ok(transactionService.getTransactionById(transactionId));
    }

    @Operation(summary = "Get monthly transactions")
    @GetMapping("/monthly")
    public ResponseEntity<List<TransactionResponse>> getMonthlyTransactions(
            @RequestParam int year,
            @RequestParam int month
    ) {
        return ResponseEntity.ok(
                transactionService.getMonthlyTransactions(year, month)
        );
    }

    @Operation(summary = "Get monthly income and expense summary")
    @GetMapping("/summary")
    public ResponseEntity<MonthlySummaryResponse> getMonthlySummary(
            @RequestParam int year,
            @RequestParam int month
    ) {
        return ResponseEntity.ok(
                transactionService.getMonthlySummary(year, month)
        );
    }

    @GetMapping("/summary/category")
    public ResponseEntity<List<CategoryExpenseSummaryResponse>> getCategoryExpenseSummary(
            @RequestParam int year,
            @RequestParam int month
    ) {
        return ResponseEntity.ok(
                transactionService.getCategoryExpenseSummary(year, month)
        );
    }

    @Operation(summary = "Update transaction")
    @PutMapping("/{transactionId}")
    public ResponseEntity<TransactionResponse> updateTransaction(
            @PathVariable UUID transactionId,
            @RequestHeader(AUTHORIZATION) String authorizationHeader,
            @Valid @RequestBody TransactionRequest request
    ) {
        TransactionResponse response = transactionService.updateTransaction(
                transactionId,
                request,
                authorizationHeader
        );

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete transaction")
    @DeleteMapping("/{transactionId}")
    public ResponseEntity<Void> deleteTransaction(
            @PathVariable UUID transactionId,
            @RequestHeader(AUTHORIZATION) String authorizationHeader
    ) {
        transactionService.deleteTransaction(transactionId, authorizationHeader);
        return ResponseEntity.noContent().build();
    }
}
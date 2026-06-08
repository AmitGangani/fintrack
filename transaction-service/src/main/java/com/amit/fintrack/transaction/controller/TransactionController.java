package com.amit.fintrack.transaction.controller;

import com.amit.fintrack.transaction.dto.CategoryExpenseSummaryResponse;
import com.amit.fintrack.transaction.dto.MonthlySummaryResponse;
import com.amit.fintrack.transaction.dto.TransactionRequest;
import com.amit.fintrack.transaction.dto.TransactionResponse;
import com.amit.fintrack.transaction.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(
            @Valid @RequestBody TransactionRequest request
    ) {
        TransactionResponse response = transactionService.createTransaction(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<TransactionResponse>> getMyTransactions() {
        return ResponseEntity.ok(transactionService.getMyTransactions());
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionResponse> getTransactionById(
            @PathVariable UUID transactionId
    ) {
        return ResponseEntity.ok(transactionService.getTransactionById(transactionId));
    }

    @GetMapping("/monthly")
    public ResponseEntity<List<TransactionResponse>> getMonthlyTransactions(
            @RequestParam int year,
            @RequestParam int month
    ) {
        return ResponseEntity.ok(
                transactionService.getMonthlyTransactions(year, month)
        );
    }

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

    @PutMapping("/{transactionId}")
    public ResponseEntity<TransactionResponse> updateTransaction(
            @PathVariable UUID transactionId,
            @Valid @RequestBody TransactionRequest request
    ) {
        TransactionResponse response = transactionService.updateTransaction(
                transactionId,
                request
        );

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{transactionId}")
    public ResponseEntity<Void> deleteTransaction(
            @PathVariable UUID transactionId
    ) {
        transactionService.deleteTransaction(transactionId);
        return ResponseEntity.noContent().build();
    }
}
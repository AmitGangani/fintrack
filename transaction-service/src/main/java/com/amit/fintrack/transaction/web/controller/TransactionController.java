package com.amit.fintrack.transaction.web.controller;

import com.amit.fintrack.transaction.application.TransactionService;
import com.amit.fintrack.transaction.application.model.TransactionCommand;
import com.amit.fintrack.transaction.application.model.TransactionView;
import com.amit.fintrack.transaction.domain.CategoryExpenseSummary;
import com.amit.fintrack.transaction.domain.MonthlySummary;
import com.amit.fintrack.transaction.web.dto.CategoryExpenseSummaryResponse;
import com.amit.fintrack.transaction.web.dto.MonthlySummaryResponse;
import com.amit.fintrack.transaction.web.dto.TransactionRequest;
import com.amit.fintrack.transaction.web.dto.TransactionResponse;
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
        TransactionResponse response = toResponse(transactionService.createTransaction(toCommand(request)));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<TransactionResponse>> getMyTransactions() {
        return ResponseEntity.ok(transactionService.getMyTransactions().stream().map(this::toResponse).toList());
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionResponse> getTransactionById(
            @PathVariable UUID transactionId
    ) {
        return ResponseEntity.ok(toResponse(transactionService.getTransactionById(transactionId)));
    }

    @GetMapping("/monthly")
    public ResponseEntity<List<TransactionResponse>> getMonthlyTransactions(
            @RequestParam int year,
            @RequestParam int month
    ) {
        return ResponseEntity.ok(
                transactionService.getMonthlyTransactions(year, month).stream().map(this::toResponse).toList()
        );
    }

    @GetMapping("/summary")
    public ResponseEntity<MonthlySummaryResponse> getMonthlySummary(
            @RequestParam int year,
            @RequestParam int month
    ) {
        return ResponseEntity.ok(toResponse(transactionService.getMonthlySummary(year, month)));
    }

    @GetMapping("/summary/category")
    public ResponseEntity<List<CategoryExpenseSummaryResponse>> getCategoryExpenseSummary(
            @RequestParam int year,
            @RequestParam int month
    ) {
        return ResponseEntity.ok(
                transactionService.getCategoryExpenseSummary(year, month).stream().map(this::toResponse).toList()
        );
    }

    @PutMapping("/{transactionId}")
    public ResponseEntity<TransactionResponse> updateTransaction(
            @PathVariable UUID transactionId,
            @Valid @RequestBody TransactionRequest request
    ) {
        return ResponseEntity.ok(toResponse(transactionService.updateTransaction(transactionId, toCommand(request))));
    }

    @DeleteMapping("/{transactionId}")
    public ResponseEntity<Void> deleteTransaction(
            @PathVariable UUID transactionId
    ) {
        transactionService.deleteTransaction(transactionId);
        return ResponseEntity.noContent().build();
    }

    private TransactionCommand toCommand(TransactionRequest request) {
        return new TransactionCommand(
                request.accountId(),
                request.type(),
                request.category(),
                request.amount(),
                request.description(),
                request.transactionDate()
        );
    }

    private TransactionResponse toResponse(TransactionView transaction) {
        return new TransactionResponse(
                transaction.id(),
                transaction.accountId(),
                transaction.type(),
                transaction.category(),
                transaction.amount(),
                transaction.description(),
                transaction.transactionDate(),
                transaction.createdAt()
        );
    }

    private MonthlySummaryResponse toResponse(MonthlySummary summary) {
        return new MonthlySummaryResponse(
                summary.year(),
                summary.month(),
                summary.totalIncome(),
                summary.totalExpense(),
                summary.netSavings(),
                summary.transactionCount()
        );
    }

    private CategoryExpenseSummaryResponse toResponse(CategoryExpenseSummary summary) {
        return new CategoryExpenseSummaryResponse(summary.category(), summary.totalExpense());
    }
}

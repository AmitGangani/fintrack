package com.amit.fintrack.transaction.controller;

import com.amit.fintrack.transaction.dto.TransactionRequest;
import com.amit.fintrack.transaction.dto.TransactionResponse;
import com.amit.fintrack.transaction.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

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

    @GetMapping
    public ResponseEntity<List<TransactionResponse>> getMyTransactions() {
        return ResponseEntity.ok(transactionService.getMyTransactions());
    }
}
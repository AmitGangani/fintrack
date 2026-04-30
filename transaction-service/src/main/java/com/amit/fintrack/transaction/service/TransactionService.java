package com.amit.fintrack.transaction.service;

import com.amit.fintrack.transaction.client.AccountClient;
import com.amit.fintrack.transaction.dto.TransactionRequest;
import com.amit.fintrack.transaction.dto.TransactionResponse;
import com.amit.fintrack.transaction.entity.FinancialTransaction;
import com.amit.fintrack.transaction.entity.TransactionType;
import com.amit.fintrack.transaction.repository.TransactionRepository;
import com.amit.fintrack.transaction.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CurrentUserService currentUserService;
    private final AccountClient accountClient;

    public TransactionResponse createTransaction(
            TransactionRequest request,
            String authorizationHeader
    ) {
        UUID currentUserId = currentUserService.getCurrentUserId();

        BigDecimal amountChange = calculateAmountChange(
                request.type(),
                request.amount()
        );

        accountClient.adjustAccountBalance(
                request.accountId(),
                amountChange,
                authorizationHeader
        );

        FinancialTransaction transaction = FinancialTransaction.builder()
                .userId(currentUserId)
                .accountId(request.accountId())
                .type(request.type())
                .category(request.category())
                .amount(request.amount())
                .description(request.description())
                .transactionDate(request.transactionDate())
                .createdAt(LocalDateTime.now())
                .build();

        FinancialTransaction savedTransaction = transactionRepository.save(transaction);

        return toResponse(savedTransaction);
    }

    public List<TransactionResponse> getMyTransactions() {
        UUID currentUserId = currentUserService.getCurrentUserId();

        return transactionRepository.findByUserIdOrderByTransactionDateDesc(currentUserId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private BigDecimal calculateAmountChange(TransactionType type, BigDecimal amount) {
        if (type == TransactionType.INCOME) {
            return amount;
        }

        return amount.negate();
    }

    private TransactionResponse toResponse(FinancialTransaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getAccountId(),
                transaction.getType(),
                transaction.getCategory(),
                transaction.getAmount(),
                transaction.getDescription(),
                transaction.getTransactionDate(),
                transaction.getCreatedAt()
        );
    }
}
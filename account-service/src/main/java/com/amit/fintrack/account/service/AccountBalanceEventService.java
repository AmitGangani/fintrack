package com.amit.fintrack.account.service;

import com.amit.fintrack.account.entity.ProcessedKafkaEvent;
import com.amit.fintrack.account.event.TransactionLifecycleEvent;
import com.amit.fintrack.account.event.TransactionEventType;
import com.amit.fintrack.account.repository.ProcessedKafkaEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountBalanceEventService {

    private final AccountService accountService;
    private final ProcessedKafkaEventRepository processedKafkaEventRepository;

    @Transactional
    public void handleTransactionEvent(TransactionLifecycleEvent event) {

        if (processedKafkaEventRepository.existsById(event.eventId())) {
            log.info("Skipping duplicate account event: {}", event.eventId());
            return;
        }

        if (event.eventType() == TransactionEventType.CREATED) {
            applyNewTransaction(event);
        } else if (event.eventType() == TransactionEventType.UPDATED) {
            reverseOldTransaction(event);
            applyNewTransaction(event);
        } else if (event.eventType() == TransactionEventType.DELETED) {
            reverseOldTransaction(event);
        }

        markEventAsProcessed(event);

        log.info("Processed account balance event: {}", event.eventId());
    }

    private void applyNewTransaction(TransactionLifecycleEvent event) {
        if (event.newType() == null || event.newAccountId() == null) {
            return;
        }

        BigDecimal amountChange = calculateAmountChange(
                event.newType(),
                event.newAmount()
        );

        accountService.adjustBalanceFromEvent(
                event.newAccountId(),
                event.userId(),
                amountChange
        );
    }

    private void reverseOldTransaction(TransactionLifecycleEvent event) {
        if (event.oldType() == null || event.oldAccountId() == null) {
            return;
        }

        BigDecimal oldAmountChange = calculateAmountChange(
                event.oldType(),
                event.oldAmount()
        );

        BigDecimal reverseAmountChange = oldAmountChange.negate();

        accountService.adjustBalanceFromEvent(
                event.oldAccountId(),
                event.userId(),
                reverseAmountChange
        );
    }

    private BigDecimal calculateAmountChange(String type, BigDecimal amount) {
        if ("INCOME".equals(type)) {
            return amount;
        }

        return amount.negate();
    }

    private void markEventAsProcessed(TransactionLifecycleEvent event) {
        ProcessedKafkaEvent processedEvent = ProcessedKafkaEvent.builder()
                .eventId(event.eventId())
                .eventType("TRANSACTION_" + event.eventType().name())
                .processedAt(LocalDateTime.now())
                .build();

        processedKafkaEventRepository.save(processedEvent);
    }
}
package com.amit.fintrack.transaction.client;

import com.amit.fintrack.transaction.dto.BalanceAdjustmentRequest;
import com.amit.fintrack.transaction.exception.AccountServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AccountClient {

    private final RestClient restClient;

    @Value("${services.account-service.url}")
    private String accountServiceUrl;

    public void adjustAccountBalance(
            UUID accountId,
            BigDecimal amountChange,
            String authorizationHeader
    ) {
        try {
            restClient.patch()
                    .uri(accountServiceUrl + "/api/accounts/{accountId}/balance", accountId)
                    .header("Authorization", authorizationHeader)
                    .body(new BalanceAdjustmentRequest(amountChange))
                    .retrieve()
                    .toBodilessEntity();

        } catch (RestClientException exception) {
            throw new AccountServiceException("Could not update account balance");
        }
    }
}
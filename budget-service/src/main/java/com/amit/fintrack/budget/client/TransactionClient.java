package com.amit.fintrack.budget.client;

import com.amit.fintrack.budget.dto.CategoryExpenseSummaryResponse;
import com.amit.fintrack.budget.exception.TransactionServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Component
@RequiredArgsConstructor
public class TransactionClient {

    private final RestClient restClient;

    @Value("${services.transaction-service.url}")
    private String transactionServiceUrl;

    public List<CategoryExpenseSummaryResponse> getCategoryExpenseSummary(
            int year,
            int month,
            String authorizationHeader
    ) {
        try {
            return restClient.get()
                    .uri(
                            transactionServiceUrl + "/api/transactions/summary/category?year={year}&month={month}",
                            year,
                            month
                    )
                    .header(AUTHORIZATION, authorizationHeader)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<CategoryExpenseSummaryResponse>>() {
                    });

        } catch (RestClientException exception) {
            throw new TransactionServiceException("Could not fetch expense summary from Transaction Service");
        }
    }
}
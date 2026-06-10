package com.amit.fintrack.budget.persistence.adapter;

import com.amit.fintrack.budget.application.port.BudgetAlertHistoryStore;
import com.amit.fintrack.budget.domain.BudgetCategory;
import com.amit.fintrack.budget.persistence.entity.BudgetAlertHistory;
import com.amit.fintrack.budget.persistence.repository.BudgetAlertHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JpaBudgetAlertHistoryStore implements BudgetAlertHistoryStore {

    private final BudgetAlertHistoryRepository budgetAlertHistoryRepository;

    @Override
    public boolean exists(UUID userId, BudgetCategory category, int year, int month, String alertType) {
        return budgetAlertHistoryRepository.existsByUserIdAndCategoryAndYearAndMonthAndAlertType(
                userId,
                category,
                year,
                month,
                alertType
        );
    }

    @Override
    public void save(UUID userId, BudgetCategory category, int year, int month, String alertType) {
        budgetAlertHistoryRepository.save(BudgetAlertHistory.builder()
                .userId(userId)
                .category(category)
                .month(month)
                .year(year)
                .alertType(alertType)
                .createdAt(LocalDateTime.now())
                .build());
    }
}

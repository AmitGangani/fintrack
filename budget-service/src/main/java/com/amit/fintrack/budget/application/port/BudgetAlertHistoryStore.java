package com.amit.fintrack.budget.application.port;

import com.amit.fintrack.budget.domain.BudgetCategory;

import java.util.UUID;

public interface BudgetAlertHistoryStore {

    boolean exists(UUID userId, BudgetCategory category, int year, int month, String alertType);

    void save(UUID userId, BudgetCategory category, int year, int month, String alertType);
}

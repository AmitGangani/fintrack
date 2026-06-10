package com.amit.fintrack.budget.domain;

import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class BudgetCategoryResolver {

    public Optional<BudgetCategory> resolve(String category) {
        if (category == null || category.isBlank()) {
            return Optional.empty();
        }

        try {
            return Optional.of(BudgetCategory.valueOf(category));
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }
}

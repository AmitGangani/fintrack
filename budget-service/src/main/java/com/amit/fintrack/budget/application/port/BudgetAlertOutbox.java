package com.amit.fintrack.budget.application.port;

import com.amit.fintrack.budget.application.model.BudgetAlertEvent;

public interface BudgetAlertOutbox {

    void save(BudgetAlertEvent event);
}

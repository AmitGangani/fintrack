package com.amit.fintrack.budget.persistence.repository;

import com.amit.fintrack.budget.persistence.entity.BudgetAlertOutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface BudgetAlertOutboxEventRepository extends JpaRepository<BudgetAlertOutboxEvent, UUID> {

    @Query(
            value = """
                    SELECT *
                    FROM budget_alert_outbox_events
                    WHERE published = false
                    ORDER BY created_at ASC
                    LIMIT 50
                    FOR UPDATE SKIP LOCKED
                    """,
            nativeQuery = true
    )
    List<BudgetAlertOutboxEvent> findPendingForPublish();
}

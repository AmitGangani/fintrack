package com.amit.fintrack.transaction.persistence.repository;

import com.amit.fintrack.transaction.persistence.entity.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

    @Query(
            value = """
                    SELECT *
                    FROM outbox_events
                    WHERE published = false
                    ORDER BY created_at ASC
                    LIMIT 50
                    FOR UPDATE SKIP LOCKED
                    """,
            nativeQuery = true
    )
    List<OutboxEvent> findPendingForPublish();
}

CREATE TABLE budget_alert_outbox_events (
    id             UUID PRIMARY KEY,
    aggregate_type VARCHAR(100) NOT NULL,
    aggregate_id   UUID         NOT NULL,
    event_type     VARCHAR(100) NOT NULL,
    topic          VARCHAR(100) NOT NULL,
    payload        TEXT         NOT NULL,
    published      BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at     TIMESTAMP    NOT NULL,
    published_at   TIMESTAMP
);

CREATE INDEX idx_budget_alert_outbox_events_published_created_at
    ON budget_alert_outbox_events (published, created_at);

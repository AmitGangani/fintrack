CREATE TABLE budget_spending (
    id           UUID PRIMARY KEY,
    user_id      UUID           NOT NULL,
    category     VARCHAR(50)    NOT NULL,
    month        INTEGER        NOT NULL,
    year         INTEGER        NOT NULL,
    spent_amount NUMERIC(19, 2) NOT NULL,
    updated_at   TIMESTAMP      NOT NULL,

    CONSTRAINT uk_budget_spending_user_category_month_year
        UNIQUE (user_id, category, month, year)
);

CREATE INDEX idx_budget_spending_user_year_month
    ON budget_spending (user_id, year, month);


CREATE TABLE processed_kafka_events (
    event_id     UUID PRIMARY KEY,
    event_type   VARCHAR(100) NOT NULL,
    processed_at TIMESTAMP    NOT NULL
);
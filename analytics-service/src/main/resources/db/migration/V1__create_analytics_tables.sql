CREATE TABLE monthly_analytics (
    id            UUID PRIMARY KEY,
    user_id       UUID           NOT NULL,
    year          INTEGER        NOT NULL,
    month         INTEGER        NOT NULL,
    total_income  NUMERIC(19, 2) NOT NULL,
    total_expense NUMERIC(19, 2) NOT NULL,
    updated_at    TIMESTAMP      NOT NULL,

    CONSTRAINT uk_monthly_analytics_user_year_month
        UNIQUE (user_id, year, month)
);

CREATE INDEX idx_monthly_analytics_user_year_month
    ON monthly_analytics (user_id, year, month);


CREATE TABLE category_expense_analytics (
    id            UUID PRIMARY KEY,
    user_id       UUID           NOT NULL,
    category      VARCHAR(50)    NOT NULL,
    year          INTEGER        NOT NULL,
    month         INTEGER        NOT NULL,
    total_expense NUMERIC(19, 2) NOT NULL,
    updated_at    TIMESTAMP      NOT NULL,

    CONSTRAINT uk_category_expense_user_category_year_month
        UNIQUE (user_id, category, year, month)
);

CREATE INDEX idx_category_expense_user_year_month
    ON category_expense_analytics (user_id, year, month);


CREATE TABLE processed_kafka_events (
    event_id     UUID PRIMARY KEY,
    event_type   VARCHAR(100) NOT NULL,
    processed_at TIMESTAMP    NOT NULL
);
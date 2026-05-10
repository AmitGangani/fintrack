CREATE TABLE budget_alert_history (
    id         UUID PRIMARY KEY,
    user_id    UUID         NOT NULL,
    category   VARCHAR(50)  NOT NULL,
    month      INTEGER      NOT NULL,
    year       INTEGER      NOT NULL,
    alert_type VARCHAR(100) NOT NULL,
    created_at TIMESTAMP    NOT NULL,

    CONSTRAINT uk_budget_alert_history_user_category_month_year_type
        UNIQUE (user_id, category, month, year, alert_type)
);

CREATE INDEX idx_budget_alert_history_user_month
    ON budget_alert_history (user_id, year, month);
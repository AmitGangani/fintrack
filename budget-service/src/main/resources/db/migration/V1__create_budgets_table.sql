CREATE TABLE budgets (
    id           UUID PRIMARY KEY,
    user_id      UUID           NOT NULL,
    category     VARCHAR(50)    NOT NULL,
    month        INTEGER        NOT NULL,
    year         INTEGER        NOT NULL,
    limit_amount NUMERIC(19, 2) NOT NULL,
    created_at   TIMESTAMP      NOT NULL,
    updated_at   TIMESTAMP      NOT NULL,

    CONSTRAINT uk_budget_user_category_month_year
        UNIQUE (user_id, category, month, year)
);

CREATE INDEX idx_budgets_user_year_month
    ON budgets (user_id, year, month);
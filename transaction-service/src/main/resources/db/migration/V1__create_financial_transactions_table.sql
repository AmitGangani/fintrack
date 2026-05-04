CREATE TABLE financial_transactions (
    id               UUID PRIMARY KEY,
    user_id          UUID           NOT NULL,
    account_id       UUID           NOT NULL,
    type             VARCHAR(50)    NOT NULL,
    category         VARCHAR(50)    NOT NULL,
    amount           NUMERIC(19, 2) NOT NULL,
    description      VARCHAR(255),
    transaction_date DATE           NOT NULL,
    created_at       TIMESTAMP      NOT NULL
);

CREATE INDEX idx_transactions_user_id ON financial_transactions (user_id);

CREATE INDEX idx_transactions_user_date
    ON financial_transactions (user_id, transaction_date);

CREATE INDEX idx_transactions_account_id
    ON financial_transactions (account_id);
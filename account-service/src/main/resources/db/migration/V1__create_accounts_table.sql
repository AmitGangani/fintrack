CREATE TABLE accounts (
    id         UUID PRIMARY KEY,
    user_id    UUID           NOT NULL,
    name       VARCHAR(255)   NOT NULL,
    type       VARCHAR(50)    NOT NULL,
    balance    NUMERIC(19, 2) NOT NULL,
    currency   VARCHAR(3)     NOT NULL,
    created_at TIMESTAMP      NOT NULL,
    updated_at TIMESTAMP      NOT NULL
);

CREATE INDEX idx_accounts_user_id ON accounts(user_id);
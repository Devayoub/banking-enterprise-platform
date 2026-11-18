-- Core banking read model: accounts and booked transactions.
-- Money is NUMERIC(19,4): never use floating point for monetary amounts.

CREATE TABLE accounts (
    id            UUID          PRIMARY KEY,
    owner_id      VARCHAR(64)   NOT NULL,
    iban          VARCHAR(34)   NOT NULL UNIQUE,
    display_name  VARCHAR(100)  NOT NULL,
    account_type  VARCHAR(20)   NOT NULL CHECK (account_type IN ('CURRENT', 'SAVINGS', 'CARD')),
    currency      VARCHAR(3)    NOT NULL,
    balance       NUMERIC(19,4) NOT NULL DEFAULT 0,
    status        VARCHAR(20)   NOT NULL CHECK (status IN ('ACTIVE', 'BLOCKED', 'CLOSED')),
    created_at    TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ   NOT NULL DEFAULT now(),
    version       BIGINT        NOT NULL DEFAULT 0
);

CREATE INDEX idx_accounts_owner ON accounts (owner_id);

CREATE TABLE account_transactions (
    id               UUID          PRIMARY KEY,
    account_id       UUID          NOT NULL REFERENCES accounts (id),
    transaction_ref  VARCHAR(64)   NOT NULL,
    direction        VARCHAR(10)   NOT NULL CHECK (direction IN ('CREDIT', 'DEBIT')),
    amount           NUMERIC(19,4) NOT NULL CHECK (amount > 0),
    currency         VARCHAR(3)    NOT NULL,
    description      VARCHAR(140),
    booking_date     DATE          NOT NULL,
    value_date       DATE          NOT NULL,
    source_system    VARCHAR(32)   NOT NULL,
    created_at       TIMESTAMPTZ   NOT NULL DEFAULT now(),
    -- Idempotent event consumption: the same core-banking reference can only be booked once.
    CONSTRAINT uq_account_transactions_ref UNIQUE (transaction_ref)
);

CREATE INDEX idx_account_transactions_account_booking
    ON account_transactions (account_id, booking_date DESC, created_at DESC);

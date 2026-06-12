CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE accounts (
                          id            UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                          username      VARCHAR(100) NOT NULL UNIQUE,
                          email         VARCHAR(255) NOT NULL UNIQUE,
                          password_hash VARCHAR(255) NOT NULL,
                          balance       NUMERIC(19,4) NOT NULL DEFAULT 0.0000,
                          currency      VARCHAR(3) NOT NULL DEFAULT 'USD',
                          role          VARCHAR(20) NOT NULL DEFAULT 'USER',
                          version       BIGINT NOT NULL DEFAULT 0,
                          active        BOOLEAN NOT NULL DEFAULT TRUE,
                          created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                          updated_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                          CONSTRAINT chk_balance_non_negative CHECK (balance >= 0),
                          CONSTRAINT chk_currency_format CHECK (currency ~ '^[A-Z]{3}$'),
    CONSTRAINT chk_role CHECK (role IN ('USER','ADMIN'))
);

CREATE TABLE transactions (
                              id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                              sender_id       UUID NOT NULL REFERENCES accounts(id),
                              receiver_id     UUID NOT NULL REFERENCES accounts(id),
                              amount          NUMERIC(19,4) NOT NULL,
                              currency        VARCHAR(3) NOT NULL,
                              status          VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                              idempotency_key VARCHAR(255) NOT NULL UNIQUE,
                              failure_reason  TEXT,
                              version         BIGINT NOT NULL DEFAULT 0,
                              created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                              updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                              CONSTRAINT chk_amount_positive CHECK (amount > 0),
                              CONSTRAINT chk_status CHECK (status IN ('PENDING','PROCESSING','COMPLETED','FAILED')),
                              CONSTRAINT chk_no_self_transfer CHECK (sender_id != receiver_id)
    );

CREATE TABLE outbox_events (
                               id             UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                               aggregate_id   UUID NOT NULL,
                               aggregate_type VARCHAR(100) NOT NULL,
                               event_type     VARCHAR(100) NOT NULL,
                               payload        TEXT NOT NULL,
                               published      BOOLEAN NOT NULL DEFAULT FALSE,
                               retry_count    INT NOT NULL DEFAULT 0,
                               created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                               published_at   TIMESTAMPTZ
);

CREATE TABLE audit_logs (
                            id             UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                            transaction_id UUID NOT NULL REFERENCES transactions(id),
                            account_id     UUID NOT NULL REFERENCES accounts(id),
                            operation      VARCHAR(50) NOT NULL,
                            amount_delta   NUMERIC(19,4) NOT NULL,
                            balance_before NUMERIC(19,4) NOT NULL,
                            balance_after  NUMERIC(19,4) NOT NULL,
                            performed_by   VARCHAR(100) NOT NULL,
                            created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                            CONSTRAINT chk_operation CHECK (operation IN ('DEBIT','CREDIT','REFUND','ADJUSTMENT'))
);

CREATE INDEX idx_transactions_sender_id   ON transactions(sender_id);
CREATE INDEX idx_transactions_receiver_id ON transactions(receiver_id);
CREATE INDEX idx_transactions_status      ON transactions(status);
CREATE INDEX idx_transactions_created_at  ON transactions(created_at DESC);
CREATE INDEX idx_outbox_unpublished       ON outbox_events(published, created_at) WHERE published = FALSE;
CREATE INDEX idx_audit_logs_transaction   ON audit_logs(transaction_id);
CREATE INDEX idx_audit_logs_account       ON audit_logs(account_id);

CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN NEW.updated_at = NOW(); RETURN NEW; END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_accounts_updated_at
    BEFORE UPDATE ON accounts FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER trg_transactions_updated_at
    BEFORE UPDATE ON transactions FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
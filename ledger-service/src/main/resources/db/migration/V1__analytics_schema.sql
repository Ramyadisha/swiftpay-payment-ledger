CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE analytics_records (
                                   id               UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                   transaction_id   UUID NOT NULL UNIQUE,
                                   sender_id        UUID NOT NULL,
                                   receiver_id      UUID NOT NULL,
                                   amount           NUMERIC(19,4) NOT NULL,
                                   currency         VARCHAR(3) NOT NULL,
                                   transaction_date DATE NOT NULL,
                                   processed_at     TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_analytics_sender_id        ON analytics_records(sender_id);
CREATE INDEX idx_analytics_receiver_id      ON analytics_records(receiver_id);
CREATE INDEX idx_analytics_transaction_date ON analytics_records(transaction_date DESC);
CREATE INDEX idx_analytics_transaction_id   ON analytics_records(transaction_id);
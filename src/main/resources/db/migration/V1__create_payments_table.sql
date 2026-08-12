CREATE TABLE payments (
    id       UUID          PRIMARY KEY,
    amount   NUMERIC(19, 2) NOT NULL CHECK (amount > 0),
    currency CHAR(3)        NOT NULL,
    status   VARCHAR(16)    NOT NULL
);

-- The list endpoint and any future reporting filter on status far more often
-- than on anything else.
CREATE INDEX idx_payments_status ON payments (status);

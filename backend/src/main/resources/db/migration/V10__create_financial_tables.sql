-- =============================================
-- V10: Financial tables (monthly fees)
-- =============================================

CREATE TABLE monthly_fees (
    id                  BIGSERIAL       PRIMARY KEY,
    condominium_id      BIGINT          NOT NULL REFERENCES condominiums (id),
    unit_id             BIGINT          NOT NULL REFERENCES units (id),
    reference_month     DATE            NOT NULL,
    amount_cents        INT             NOT NULL,
    due_date            DATE            NOT NULL,
    status              VARCHAR(20)     DEFAULT 'PENDING'
                        CONSTRAINT ck_monthly_fees_status
                        CHECK (status IN ('PENDING', 'PAID', 'OVERDUE', 'WAIVED')),
    paid_at             TIMESTAMPTZ,
    paid_amount_cents   INT,
    notes               TEXT,
    created_at          TIMESTAMPTZ     DEFAULT now(),
    updated_at          TIMESTAMPTZ     DEFAULT now(),
    UNIQUE (unit_id, reference_month)
);

-- Indexes
CREATE INDEX idx_monthly_fees_condominium_id ON monthly_fees (condominium_id);
CREATE INDEX idx_monthly_fees_unit_id ON monthly_fees (unit_id);
CREATE INDEX idx_monthly_fees_condominium_status ON monthly_fees (condominium_id, status);

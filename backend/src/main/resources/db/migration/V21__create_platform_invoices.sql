-- =============================================
-- V21: Faturas da plataforma (mensalidade do plano)
-- =============================================

CREATE TABLE platform_invoices (
    id                      BIGSERIAL       PRIMARY KEY,
    condominium_id          BIGINT          NOT NULL REFERENCES condominiums (id),
    plan_id                 BIGINT          NOT NULL REFERENCES subscription_plans (id),
    reference_month         VARCHAR(7)      NOT NULL,  -- 'YYYY-MM'
    amount_cents            INT             NOT NULL,
    stripe_session_id       VARCHAR(200),
    stripe_invoice_id       VARCHAR(200),
    status                  VARCHAR(20)    NOT NULL DEFAULT 'PENDING'
                        CONSTRAINT ck_platform_invoices_status
                        CHECK (status IN ('PENDING', 'PAID', 'OVERDUE', 'CANCELLED')),
    paid_at                 TIMESTAMPTZ,
    created_at              TIMESTAMPTZ     DEFAULT now(),
    updated_at              TIMESTAMPTZ     DEFAULT now()
);

CREATE INDEX idx_platform_invoices_condominium_id ON platform_invoices (condominium_id);
CREATE INDEX idx_platform_invoices_condo_status ON platform_invoices (condominium_id, status);
CREATE UNIQUE INDEX idx_platform_invoices_condo_ref ON platform_invoices (condominium_id, reference_month);

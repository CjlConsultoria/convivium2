-- =============================================
-- V8: Visitor tables
-- =============================================

CREATE TABLE visitors (
    id                  BIGSERIAL       PRIMARY KEY,
    condominium_id      BIGINT          NOT NULL REFERENCES condominiums (id),
    name                VARCHAR(200)    NOT NULL,
    document_number     VARCHAR(20),
    phone               VARCHAR(20),
    photo_url           VARCHAR(500),
    vehicle_plate       VARCHAR(10),
    registered_by       BIGINT          NOT NULL REFERENCES users (id),
    unit_id             BIGINT          REFERENCES units (id),
    visit_type          VARCHAR(20)     DEFAULT 'SINGLE'
                        CONSTRAINT ck_visitors_visit_type
                        CHECK (visit_type IN ('SINGLE', 'RECURRING', 'SERVICE_PROVIDER')),
    qr_code_data        VARCHAR(500),
    valid_from          TIMESTAMPTZ,
    valid_until         TIMESTAMPTZ,
    created_at          TIMESTAMPTZ     DEFAULT now(),
    updated_at          TIMESTAMPTZ     DEFAULT now()
);

CREATE TABLE visitor_logs (
    id                  BIGSERIAL       PRIMARY KEY,
    visitor_id          BIGINT          NOT NULL REFERENCES visitors (id),
    condominium_id      BIGINT          NOT NULL REFERENCES condominiums (id),
    doorman_id          BIGINT          NOT NULL REFERENCES users (id),
    entry_time          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    exit_time           TIMESTAMPTZ,
    notes               TEXT,
    created_at          TIMESTAMPTZ     DEFAULT now()
);

CREATE TABLE recurring_visitor_passes (
    id                  BIGSERIAL       PRIMARY KEY,
    visitor_id          BIGINT          NOT NULL REFERENCES visitors (id),
    condominium_id      BIGINT          NOT NULL REFERENCES condominiums (id),
    authorized_by       BIGINT          NOT NULL REFERENCES users (id),
    days_of_week        VARCHAR(20),
    time_from           TIME,
    time_until          TIME,
    valid_from          DATE            NOT NULL,
    valid_until         DATE            NOT NULL,
    is_active           BOOLEAN         DEFAULT true,
    purpose             VARCHAR(200),
    created_at          TIMESTAMPTZ     DEFAULT now(),
    updated_at          TIMESTAMPTZ     DEFAULT now()
);

-- Indexes
CREATE INDEX idx_visitors_condominium_id ON visitors (condominium_id);
CREATE INDEX idx_visitor_logs_condominium_id ON visitor_logs (condominium_id);
CREATE INDEX idx_visitor_logs_condominium_entry ON visitor_logs (condominium_id, entry_time);

-- =============================================
-- V4: Complaint tables
-- =============================================

CREATE TABLE complaints (
    id                  BIGSERIAL       PRIMARY KEY,
    condominium_id      BIGINT          NOT NULL REFERENCES condominiums (id),
    complainant_id      BIGINT          REFERENCES users (id),
    is_anonymous        BOOLEAN         DEFAULT false,
    category            VARCHAR(30)     NOT NULL
                        CONSTRAINT ck_complaints_category
                        CHECK (category IN ('NOISE', 'MAINTENANCE', 'SECURITY', 'PARKING', 'PET', 'COMMON_AREA', 'OTHER')),
    title               VARCHAR(200)    NOT NULL,
    description         TEXT            NOT NULL,
    unit_id             BIGINT          REFERENCES units (id),
    status              VARCHAR(20)     DEFAULT 'OPEN'
                        CONSTRAINT ck_complaints_status
                        CHECK (status IN ('OPEN', 'IN_REVIEW', 'RESPONDED', 'RESOLVED', 'CLOSED')),
    priority            VARCHAR(10)     DEFAULT 'MEDIUM',
    resolved_at         TIMESTAMPTZ,
    closed_at           TIMESTAMPTZ,
    created_at          TIMESTAMPTZ     DEFAULT now(),
    updated_at          TIMESTAMPTZ     DEFAULT now(),
    created_by          BIGINT          REFERENCES users (id),
    updated_by          BIGINT          REFERENCES users (id)
);

CREATE TABLE complaint_responses (
    id                  BIGSERIAL       PRIMARY KEY,
    complaint_id        BIGINT          NOT NULL REFERENCES complaints (id) ON DELETE CASCADE,
    responder_id        BIGINT          NOT NULL REFERENCES users (id),
    message             TEXT            NOT NULL,
    is_internal         BOOLEAN         DEFAULT false,
    created_at          TIMESTAMPTZ     DEFAULT now()
);

CREATE TABLE complaint_attachments (
    id                  BIGSERIAL       PRIMARY KEY,
    complaint_id        BIGINT          NOT NULL REFERENCES complaints (id) ON DELETE CASCADE,
    file_name           VARCHAR(255)    NOT NULL,
    file_url            VARCHAR(500)    NOT NULL,
    file_type           VARCHAR(50),
    file_size_bytes     BIGINT,
    uploaded_by         BIGINT          REFERENCES users (id),
    created_at          TIMESTAMPTZ     DEFAULT now()
);

-- Indexes
CREATE INDEX idx_complaints_condominium_id ON complaints (condominium_id);
CREATE INDEX idx_complaints_condominium_status ON complaints (condominium_id, status);
CREATE INDEX idx_complaints_complainant_id ON complaints (complainant_id);

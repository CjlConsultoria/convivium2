-- =============================================
-- V11: Document tables (condo documents with versioning)
-- =============================================

CREATE TABLE condo_documents (
    id                  BIGSERIAL       PRIMARY KEY,
    condominium_id      BIGINT          NOT NULL REFERENCES condominiums (id),
    uploaded_by         BIGINT          NOT NULL REFERENCES users (id),
    title               VARCHAR(200)    NOT NULL,
    description         TEXT,
    category            VARCHAR(30)     NOT NULL
                        CONSTRAINT ck_condo_documents_category
                        CHECK (category IN ('ATA', 'REGULAMENTO', 'CONVENCAO', 'BALANCETE', 'CONTRATO', 'ORCAMENTO', 'OTHER')),
    current_version     INT             NOT NULL DEFAULT 1,
    is_public           BOOLEAN         DEFAULT true,
    created_at          TIMESTAMPTZ     DEFAULT now(),
    updated_at          TIMESTAMPTZ     DEFAULT now()
);

CREATE TABLE document_versions (
    id                  BIGSERIAL       PRIMARY KEY,
    document_id         BIGINT          NOT NULL REFERENCES condo_documents (id) ON DELETE CASCADE,
    version_number      INT             NOT NULL,
    file_name           VARCHAR(255)    NOT NULL,
    file_url            VARCHAR(500)    NOT NULL,
    file_size_bytes     BIGINT,
    file_type           VARCHAR(50),
    uploaded_by         BIGINT          REFERENCES users (id),
    change_notes        TEXT,
    created_at          TIMESTAMPTZ     DEFAULT now(),
    UNIQUE (document_id, version_number)
);

-- Indexes
CREATE INDEX idx_condo_documents_condominium_id ON condo_documents (condominium_id);

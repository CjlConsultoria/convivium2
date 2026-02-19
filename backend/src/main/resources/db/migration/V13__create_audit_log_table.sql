-- =============================================
-- V13: Audit log table
-- =============================================

CREATE TABLE audit_logs (
    id                  BIGSERIAL       PRIMARY KEY,
    user_id             BIGINT          REFERENCES users (id),
    condominium_id      BIGINT          REFERENCES condominiums (id),
    action              VARCHAR(50)     NOT NULL,
    entity_type         VARCHAR(50)     NOT NULL,
    entity_id           BIGINT,
    old_values          JSONB,
    new_values          JSONB,
    ip_address          VARCHAR(45),
    user_agent          VARCHAR(500),
    created_at          TIMESTAMPTZ     DEFAULT now()
);

-- Indexes
CREATE INDEX idx_audit_logs_condominium_id ON audit_logs (condominium_id);
CREATE INDEX idx_audit_logs_user_id ON audit_logs (user_id);
CREATE INDEX idx_audit_logs_entity ON audit_logs (entity_type, entity_id);
CREATE INDEX idx_audit_logs_created_at ON audit_logs (created_at);

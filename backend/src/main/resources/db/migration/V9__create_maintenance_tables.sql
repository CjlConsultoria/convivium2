-- =============================================
-- V9: Maintenance request tables
-- =============================================

CREATE TABLE maintenance_requests (
    id                  BIGSERIAL       PRIMARY KEY,
    condominium_id      BIGINT          NOT NULL REFERENCES condominiums (id),
    requester_id        BIGINT          NOT NULL REFERENCES users (id),
    assigned_to_id      BIGINT          REFERENCES users (id),
    unit_id             BIGINT          REFERENCES units (id),
    common_area_id      BIGINT          REFERENCES common_areas (id),
    title               VARCHAR(200)    NOT NULL,
    description         TEXT            NOT NULL,
    category            VARCHAR(30)     NOT NULL
                        CONSTRAINT ck_maintenance_category
                        CHECK (category IN ('PLUMBING', 'ELECTRICAL', 'STRUCTURAL', 'PAINTING', 'ELEVATOR', 'GARDEN', 'CLEANING', 'OTHER')),
    priority            VARCHAR(10)     DEFAULT 'MEDIUM'
                        CONSTRAINT ck_maintenance_priority
                        CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'URGENT')),
    status              VARCHAR(20)     DEFAULT 'OPEN'
                        CONSTRAINT ck_maintenance_status
                        CHECK (status IN ('OPEN', 'IN_PROGRESS', 'WAITING_PARTS', 'COMPLETED', 'CANCELLED')),
    estimated_cost      DECIMAL(10, 2),
    actual_cost         DECIMAL(10, 2),
    started_at          TIMESTAMPTZ,
    completed_at        TIMESTAMPTZ,
    created_at          TIMESTAMPTZ     DEFAULT now(),
    updated_at          TIMESTAMPTZ     DEFAULT now(),
    created_by          BIGINT          REFERENCES users (id),
    updated_by          BIGINT          REFERENCES users (id)
);

CREATE TABLE maintenance_photos (
    id                          BIGSERIAL       PRIMARY KEY,
    maintenance_request_id      BIGINT          NOT NULL REFERENCES maintenance_requests (id) ON DELETE CASCADE,
    photo_url                   VARCHAR(500)    NOT NULL,
    photo_type                  VARCHAR(10)     NOT NULL
                                CONSTRAINT ck_maintenance_photo_type
                                CHECK (photo_type IN ('BEFORE', 'AFTER')),
    description                 VARCHAR(200),
    uploaded_by                 BIGINT          REFERENCES users (id),
    created_at                  TIMESTAMPTZ     DEFAULT now()
);

CREATE TABLE maintenance_comments (
    id                          BIGSERIAL       PRIMARY KEY,
    maintenance_request_id      BIGINT          NOT NULL REFERENCES maintenance_requests (id) ON DELETE CASCADE,
    user_id                     BIGINT          NOT NULL REFERENCES users (id),
    message                     TEXT            NOT NULL,
    created_at                  TIMESTAMPTZ     DEFAULT now()
);

-- Indexes
CREATE INDEX idx_maintenance_requests_condominium_id ON maintenance_requests (condominium_id);
CREATE INDEX idx_maintenance_requests_condominium_status ON maintenance_requests (condominium_id, status);
CREATE INDEX idx_maintenance_requests_assigned_to ON maintenance_requests (assigned_to_id);

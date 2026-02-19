-- =============================================
-- V3: Building and unit tables
-- =============================================

CREATE TABLE buildings (
    id                  BIGSERIAL       PRIMARY KEY,
    condominium_id      BIGINT          NOT NULL REFERENCES condominiums (id),
    name                VARCHAR(100)    NOT NULL,
    floors              INT,
    created_at          TIMESTAMPTZ     DEFAULT now(),
    updated_at          TIMESTAMPTZ     DEFAULT now(),
    UNIQUE (condominium_id, name)
);

CREATE TABLE units (
    id                  BIGSERIAL       PRIMARY KEY,
    condominium_id      BIGINT          NOT NULL REFERENCES condominiums (id),
    building_id         BIGINT          REFERENCES buildings (id),
    identifier          VARCHAR(20)     NOT NULL,
    floor               INT,
    type                VARCHAR(20)     DEFAULT 'APARTMENT',
    area_sqm            DECIMAL(10, 2),
    is_occupied         BOOLEAN         DEFAULT false,
    created_at          TIMESTAMPTZ     DEFAULT now(),
    updated_at          TIMESTAMPTZ     DEFAULT now(),
    UNIQUE (condominium_id, building_id, identifier)
);

-- Indexes
CREATE INDEX idx_buildings_condominium_id ON buildings (condominium_id);
CREATE INDEX idx_units_condominium_id ON units (condominium_id);

-- Add FK from user_condominium_roles.unit_id to units.id
ALTER TABLE user_condominium_roles
    ADD CONSTRAINT fk_user_condo_roles_unit
    FOREIGN KEY (unit_id) REFERENCES units (id);

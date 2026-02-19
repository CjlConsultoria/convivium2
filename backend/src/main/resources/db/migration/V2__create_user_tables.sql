-- =============================================
-- V2: User tables (users, roles, tokens)
-- =============================================

CREATE TABLE users (
    id                  BIGSERIAL       PRIMARY KEY,
    uuid                UUID            DEFAULT gen_random_uuid() UNIQUE NOT NULL,
    email               VARCHAR(255)    UNIQUE NOT NULL,
    password_hash       VARCHAR(255)    NOT NULL,
    name                VARCHAR(200)    NOT NULL,
    cpf                 VARCHAR(14)     UNIQUE,
    phone               VARCHAR(20),
    photo_url           VARCHAR(500),
    is_platform_admin   BOOLEAN         DEFAULT false,
    is_active           BOOLEAN         DEFAULT true,
    email_verified      BOOLEAN         DEFAULT false,
    last_login_at       TIMESTAMPTZ,
    created_at          TIMESTAMPTZ     DEFAULT now(),
    updated_at          TIMESTAMPTZ     DEFAULT now()
);

CREATE TABLE user_condominium_roles (
    id                  BIGSERIAL       PRIMARY KEY,
    user_id             BIGINT          NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    condominium_id      BIGINT          NOT NULL REFERENCES condominiums (id) ON DELETE CASCADE,
    role                VARCHAR(30)     NOT NULL
                        CONSTRAINT ck_user_condo_roles_role
                        CHECK (role IN ('SINDICO', 'SUB_SINDICO', 'CONSELHEIRO', 'PORTEIRO', 'ZELADOR', 'FAXINEIRA', 'MORADOR')),
    unit_id             BIGINT,  -- FK to units added in V3
    status              VARCHAR(20)     DEFAULT 'ACTIVE',
    approved_by         BIGINT          REFERENCES users (id),
    approved_at         TIMESTAMPTZ,
    created_at          TIMESTAMPTZ     DEFAULT now(),
    updated_at          TIMESTAMPTZ     DEFAULT now(),
    UNIQUE (user_id, condominium_id, role)
);

CREATE TABLE refresh_tokens (
    id              BIGSERIAL       PRIMARY KEY,
    user_id         BIGINT          NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    token           VARCHAR(500)    UNIQUE NOT NULL,
    device_info     VARCHAR(200),
    expires_at      TIMESTAMPTZ     NOT NULL,
    revoked         BOOLEAN         DEFAULT false,
    created_at      TIMESTAMPTZ     DEFAULT now()
);

CREATE TABLE password_reset_tokens (
    id              BIGSERIAL       PRIMARY KEY,
    user_id         BIGINT          NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    token           VARCHAR(255)    UNIQUE NOT NULL,
    expires_at      TIMESTAMPTZ     NOT NULL,
    used            BOOLEAN         DEFAULT false,
    created_at      TIMESTAMPTZ     DEFAULT now()
);

-- Indexes
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens (user_id);
CREATE INDEX idx_refresh_tokens_token ON refresh_tokens (token);
CREATE INDEX idx_user_condominium_roles_condominium_id ON user_condominium_roles (condominium_id);

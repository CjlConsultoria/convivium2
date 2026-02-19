-- =============================================
-- V1: Platform tables (plans, condominiums, subscriptions, billing)
-- =============================================

CREATE TABLE subscription_plans (
    id              BIGSERIAL       PRIMARY KEY,
    name            VARCHAR(50)     NOT NULL,
    slug            VARCHAR(50)     UNIQUE NOT NULL,
    description     TEXT,
    max_units       INT             NOT NULL,
    max_users       INT             NOT NULL,
    price_cents     INT             NOT NULL,
    stripe_price_id VARCHAR(100),
    features        JSONB,
    is_active       BOOLEAN         DEFAULT true,
    created_at      TIMESTAMPTZ     DEFAULT now(),
    updated_at      TIMESTAMPTZ     DEFAULT now()
);

CREATE TABLE condominiums (
    id                    BIGSERIAL       PRIMARY KEY,
    name                  VARCHAR(200)    NOT NULL,
    slug                  VARCHAR(100)    UNIQUE NOT NULL,
    cnpj                  VARCHAR(18)     UNIQUE,
    email                 VARCHAR(255),
    phone                 VARCHAR(20),
    address_street        VARCHAR(300),
    address_number        VARCHAR(20),
    address_complement    VARCHAR(100),
    address_neighborhood  VARCHAR(100),
    address_city          VARCHAR(100),
    address_state         VARCHAR(2),
    address_zip           VARCHAR(10),
    logo_url              VARCHAR(500),
    status                VARCHAR(20)     NOT NULL DEFAULT 'PENDING'
                          CONSTRAINT ck_condominiums_status
                          CHECK (status IN ('PENDING', 'ACTIVE', 'SUSPENDED', 'DEACTIVATED')),
    created_at            TIMESTAMPTZ     DEFAULT now(),
    updated_at            TIMESTAMPTZ     DEFAULT now()
);

CREATE TABLE subscriptions (
    id                      BIGSERIAL       PRIMARY KEY,
    condominium_id          BIGINT          REFERENCES condominiums (id),
    plan_id                 BIGINT          REFERENCES subscription_plans (id),
    stripe_subscription_id  VARCHAR(100),
    stripe_customer_id      VARCHAR(100),
    status                  VARCHAR(20)     DEFAULT 'TRIAL',
    current_period_start    TIMESTAMPTZ,
    current_period_end      TIMESTAMPTZ,
    trial_end               TIMESTAMPTZ,
    cancelled_at            TIMESTAMPTZ,
    created_at              TIMESTAMPTZ     DEFAULT now(),
    updated_at              TIMESTAMPTZ     DEFAULT now()
);

CREATE TABLE billing_events (
    id                  BIGSERIAL       PRIMARY KEY,
    subscription_id     BIGINT          REFERENCES subscriptions (id),
    stripe_event_id     VARCHAR(100)    UNIQUE,
    event_type          VARCHAR(50)     NOT NULL,
    amount_cents        INT,
    currency            VARCHAR(3)      DEFAULT 'BRL',
    payload             JSONB,
    created_at          TIMESTAMPTZ     DEFAULT now()
);

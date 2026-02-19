-- =============================================
-- V5: Parcel tables
-- =============================================

CREATE TABLE parcels (
    id                  BIGSERIAL       PRIMARY KEY,
    condominium_id      BIGINT          NOT NULL REFERENCES condominiums (id),
    unit_id             BIGINT          NOT NULL REFERENCES units (id),
    recipient_id        BIGINT          REFERENCES users (id),
    received_by_id      BIGINT          NOT NULL REFERENCES users (id),
    carrier             VARCHAR(100),
    tracking_number     VARCHAR(100),
    description         VARCHAR(500),
    status              VARCHAR(20)     DEFAULT 'RECEIVED'
                        CONSTRAINT ck_parcels_status
                        CHECK (status IN ('RECEIVED', 'NOTIFIED', 'PICKUP_REQUESTED', 'VERIFIED', 'DELIVERED')),
    pickup_code         VARCHAR(10),
    resident_code       VARCHAR(10),
    qr_code_data        VARCHAR(500),
    picked_up_by_id     BIGINT          REFERENCES users (id),
    delivered_at        TIMESTAMPTZ,
    created_at          TIMESTAMPTZ     DEFAULT now(),
    updated_at          TIMESTAMPTZ     DEFAULT now()
);

CREATE TABLE parcel_photos (
    id                  BIGSERIAL       PRIMARY KEY,
    parcel_id           BIGINT          NOT NULL REFERENCES parcels (id) ON DELETE CASCADE,
    photo_url           VARCHAR(500)    NOT NULL,
    photo_type          VARCHAR(20)     NOT NULL
                        CONSTRAINT ck_parcel_photos_type
                        CHECK (photo_type IN ('RECEIPT', 'PICKUP', 'LABEL')),
    uploaded_by         BIGINT          REFERENCES users (id),
    created_at          TIMESTAMPTZ     DEFAULT now()
);

CREATE TABLE parcel_verifications (
    id                      BIGSERIAL       PRIMARY KEY,
    parcel_id               BIGINT          NOT NULL REFERENCES parcels (id),
    doorman_id              BIGINT          NOT NULL REFERENCES users (id),
    resident_id             BIGINT          NOT NULL REFERENCES users (id),
    verification_method     VARCHAR(20)     NOT NULL
                            CONSTRAINT ck_parcel_verif_method
                            CHECK (verification_method IN ('CODE_MATCH', 'QR_SCAN')),
    doorman_code            VARCHAR(10),
    resident_code           VARCHAR(10),
    is_verified             BOOLEAN         DEFAULT false,
    verified_at             TIMESTAMPTZ,
    created_at              TIMESTAMPTZ     DEFAULT now()
);

-- Indexes
CREATE INDEX idx_parcels_condominium_id ON parcels (condominium_id);
CREATE INDEX idx_parcels_unit_id ON parcels (unit_id);
CREATE INDEX idx_parcels_condominium_status ON parcels (condominium_id, status);
CREATE INDEX idx_parcels_recipient_id ON parcels (recipient_id);

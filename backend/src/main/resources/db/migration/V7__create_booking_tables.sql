-- =============================================
-- V7: Booking tables (common areas and reservations)
-- =============================================

CREATE TABLE common_areas (
    id                      BIGSERIAL       PRIMARY KEY,
    condominium_id          BIGINT          NOT NULL REFERENCES condominiums (id),
    name                    VARCHAR(100)    NOT NULL,
    description             TEXT,
    max_capacity            INT,
    photo_url               VARCHAR(500),
    is_active               BOOLEAN         DEFAULT true,
    requires_approval       BOOLEAN         DEFAULT false,
    advance_booking_days    INT             DEFAULT 30,
    min_interval_hours      DECIMAL(4, 1)   DEFAULT 1.0,
    max_interval_hours      DECIMAL(4, 1)   DEFAULT 8.0,
    available_from          TIME,
    available_until         TIME,
    available_days          VARCHAR(20)     DEFAULT '1234567',
    rules_text              TEXT,
    created_at              TIMESTAMPTZ     DEFAULT now(),
    updated_at              TIMESTAMPTZ     DEFAULT now()
);

CREATE TABLE bookings (
    id                      BIGSERIAL       PRIMARY KEY,
    condominium_id          BIGINT          NOT NULL REFERENCES condominiums (id),
    common_area_id          BIGINT          NOT NULL REFERENCES common_areas (id),
    user_id                 BIGINT          NOT NULL REFERENCES users (id),
    unit_id                 BIGINT          REFERENCES units (id),
    start_time              TIMESTAMPTZ     NOT NULL,
    end_time                TIMESTAMPTZ     NOT NULL,
    guest_count             INT             DEFAULT 0,
    status                  VARCHAR(20)     DEFAULT 'CONFIRMED'
                            CONSTRAINT ck_bookings_status
                            CHECK (status IN ('PENDING', 'CONFIRMED', 'CANCELLED', 'COMPLETED', 'REJECTED')),
    notes                   TEXT,
    approved_by             BIGINT          REFERENCES users (id),
    cancelled_at            TIMESTAMPTZ,
    cancellation_reason     TEXT,
    created_at              TIMESTAMPTZ     DEFAULT now(),
    updated_at              TIMESTAMPTZ     DEFAULT now(),
    CONSTRAINT no_time_travel CHECK (end_time > start_time)
);

-- Indexes
CREATE INDEX idx_common_areas_condominium_id ON common_areas (condominium_id);
CREATE INDEX idx_bookings_condominium_id ON bookings (condominium_id);
CREATE INDEX idx_bookings_area_time ON bookings (common_area_id, start_time, end_time);
CREATE INDEX idx_bookings_user_id ON bookings (user_id);

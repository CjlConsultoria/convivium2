-- =============================================
-- V12: Notification tables
-- =============================================

CREATE TABLE notification_preferences (
    id                  BIGSERIAL       PRIMARY KEY,
    user_id             BIGINT          NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    channel             VARCHAR(20)     NOT NULL
                        CONSTRAINT ck_notif_pref_channel
                        CHECK (channel IN ('EMAIL', 'IN_APP', 'WHATSAPP')),
    event_type          VARCHAR(50)     NOT NULL,
    is_enabled          BOOLEAN         DEFAULT true,
    UNIQUE (user_id, channel, event_type)
);

CREATE TABLE notifications (
    id                  BIGSERIAL       PRIMARY KEY,
    user_id             BIGINT          NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    condominium_id      BIGINT          REFERENCES condominiums (id),
    title               VARCHAR(200)    NOT NULL,
    message             TEXT            NOT NULL,
    type                VARCHAR(50)     NOT NULL,
    reference_type      VARCHAR(30),
    reference_id        BIGINT,
    channel             VARCHAR(20)     NOT NULL,
    is_read             BOOLEAN         DEFAULT false,
    read_at             TIMESTAMPTZ,
    sent_at             TIMESTAMPTZ,
    delivery_status     VARCHAR(20)     DEFAULT 'PENDING'
                        CONSTRAINT ck_notifications_delivery_status
                        CHECK (delivery_status IN ('PENDING', 'SENT', 'DELIVERED', 'FAILED')),
    created_at          TIMESTAMPTZ     DEFAULT now()
);

-- Indexes
CREATE INDEX idx_notifications_user_id ON notifications (user_id);
CREATE INDEX idx_notifications_user_unread ON notifications (user_id, is_read) WHERE is_read = false;
CREATE INDEX idx_notifications_condominium_id ON notifications (condominium_id);

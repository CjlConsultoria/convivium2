-- =============================================
-- V6: Announcement tables
-- =============================================

CREATE TABLE announcements (
    id                  BIGSERIAL       PRIMARY KEY,
    condominium_id      BIGINT          NOT NULL REFERENCES condominiums (id),
    author_id           BIGINT          NOT NULL REFERENCES users (id),
    title               VARCHAR(200)    NOT NULL,
    content             TEXT            NOT NULL,
    category            VARCHAR(30)     DEFAULT 'GENERAL'
                        CONSTRAINT ck_announcements_category
                        CHECK (category IN ('GENERAL', 'MAINTENANCE', 'EVENT', 'EMERGENCY', 'FINANCIAL')),
    is_emergency        BOOLEAN         DEFAULT false,
    is_pinned           BOOLEAN         DEFAULT false,
    published_at        TIMESTAMPTZ,
    expires_at          TIMESTAMPTZ,
    target_roles        VARCHAR(200),
    created_at          TIMESTAMPTZ     DEFAULT now(),
    updated_at          TIMESTAMPTZ     DEFAULT now()
);

CREATE TABLE announcement_attachments (
    id                  BIGSERIAL       PRIMARY KEY,
    announcement_id     BIGINT          NOT NULL REFERENCES announcements (id) ON DELETE CASCADE,
    file_name           VARCHAR(255)    NOT NULL,
    file_url            VARCHAR(500)    NOT NULL,
    file_type           VARCHAR(50),
    created_at          TIMESTAMPTZ     DEFAULT now()
);

CREATE TABLE announcement_reads (
    id                  BIGSERIAL       PRIMARY KEY,
    announcement_id     BIGINT          NOT NULL REFERENCES announcements (id) ON DELETE CASCADE,
    user_id             BIGINT          NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    read_at             TIMESTAMPTZ     DEFAULT now(),
    UNIQUE (announcement_id, user_id)
);

-- Indexes
CREATE INDEX idx_announcements_condominium_id ON announcements (condominium_id);
CREATE INDEX idx_announcements_condominium_published ON announcements (condominium_id, published_at);

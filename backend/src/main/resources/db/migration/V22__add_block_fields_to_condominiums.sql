-- V22: Add blocking and subscription fields to condominiums
ALTER TABLE condominiums
  ADD COLUMN block_type VARCHAR(20) DEFAULT NULL
    CONSTRAINT ck_condominiums_block_type CHECK (block_type IN ('PAYMENT', 'GENERAL')),
  ADD COLUMN blocked_at TIMESTAMPTZ,
  ADD COLUMN blocked_reason TEXT,
  ADD COLUMN subscription_started_at TIMESTAMPTZ;

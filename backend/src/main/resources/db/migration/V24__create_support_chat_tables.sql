-- V24: Create support tickets and messages tables
CREATE TABLE support_tickets (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL REFERENCES users(id),
  condominium_id BIGINT REFERENCES condominiums(id),
  subject VARCHAR(200) NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'OPEN'
    CONSTRAINT ck_support_tickets_status CHECK (status IN ('OPEN','IN_PROGRESS','RESOLVED','CLOSED')),
  created_at TIMESTAMPTZ DEFAULT now(),
  updated_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE support_messages (
  id BIGSERIAL PRIMARY KEY,
  ticket_id BIGINT NOT NULL REFERENCES support_tickets(id) ON DELETE CASCADE,
  sender_id BIGINT NOT NULL REFERENCES users(id),
  message TEXT NOT NULL,
  is_from_admin BOOLEAN DEFAULT false,
  is_read BOOLEAN DEFAULT false,
  read_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ DEFAULT now()
);

CREATE INDEX idx_support_tickets_user_id ON support_tickets(user_id);
CREATE INDEX idx_support_tickets_status ON support_tickets(status);
CREATE INDEX idx_support_messages_ticket_id ON support_messages(ticket_id);

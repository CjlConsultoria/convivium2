-- Usuários que entram só pelo Google não têm senha
ALTER TABLE users ALTER COLUMN password_hash DROP NOT NULL;

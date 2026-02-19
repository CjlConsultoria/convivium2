-- =============================================================
-- Convivium 2.0 - Database Initialization Script
-- Executa automaticamente na primeira vez que o container sobe
-- =============================================================

-- Cria o banco 'convivium' caso ainda nao exista
-- (O POSTGRES_DB do docker-compose ja cria o banco padrao,
--  mas este script garante config adicional)

-- Extensoes uteis
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "unaccent";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";

-- Garante que o schema public tem as permissoes corretas
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO PUBLIC;

-- Configuracoes de performance para dev
ALTER SYSTEM SET log_statement = 'all';
ALTER SYSTEM SET log_min_duration_statement = 500;

SELECT pg_reload_conf();

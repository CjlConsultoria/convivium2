-- =============================================
-- Limpa o banco para usar apenas o schema do Convivium 2 (Flyway).
-- Execute UMA VEZ antes de subir o backend com o banco novo/limpo.
-- Uso: psql ... -f clear-database.sql
-- =============================================

-- Encerra conexões ativas no banco (evita "database is being accessed by other users")
SELECT pg_terminate_backend(pid)
FROM pg_stat_activity
WHERE datname = current_database()
  AND pid <> pg_backend_pid();

-- Remove todo o conteúdo do schema public (tabelas, views, sequences do sistema antigo ou anterior)
DROP SCHEMA public CASCADE;
CREATE SCHEMA public;

-- Permissões para o usuário do app (ex.: convivium no Render)
GRANT ALL ON SCHEMA public TO convivium;
GRANT CREATE ON SCHEMA public TO convivium;
-- Tabelas/sequences criadas pelo Flyway (app) receberão permissão via owner; garantir uso do schema
GRANT ALL ON ALL TABLES IN SCHEMA public TO convivium;
GRANT ALL ON ALL SEQUENCES IN SCHEMA public TO convivium;
GRANT ALL ON ALL FUNCTIONS IN SCHEMA public TO convivium;

-- Flyway vai recriar tudo na próxima subida do backend (V1..V21).

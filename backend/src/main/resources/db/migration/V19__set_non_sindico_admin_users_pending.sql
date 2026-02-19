-- Apenas síndico permanece ativo. Demais (morador, porteiro, sub-síndico, etc.) ficam pendentes de aprovação do síndico.
-- Admin Max é admin da plataforma (não está nesta tabela), então continua com acesso.
UPDATE user_condominium_roles
SET status = 'PENDING_APPROVAL'
WHERE status = 'ACTIVE'
  AND role != 'SINDICO';

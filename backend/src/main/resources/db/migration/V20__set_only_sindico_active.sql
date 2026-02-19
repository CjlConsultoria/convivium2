-- Garante que apenas SINDICO fica ativo; demais (incluindo SUB_SINDICO) ficam pendentes de aprovação.
-- Para quem já rodou V19: sub-síndicos que ficaram ativos passam a pendente.
UPDATE user_condominium_roles
SET status = 'PENDING_APPROVAL'
WHERE status = 'ACTIVE'
  AND role != 'SINDICO';

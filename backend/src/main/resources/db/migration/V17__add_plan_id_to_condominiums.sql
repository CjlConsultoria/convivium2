-- Associa um plano (subscription_plan) ao condominio para controle de assinatura e suspensao por inadimplencia.
ALTER TABLE condominiums
    ADD COLUMN IF NOT EXISTS plan_id BIGINT REFERENCES subscription_plans (id);

CREATE INDEX IF NOT EXISTS idx_condominiums_plan_id ON condominiums (plan_id);

-- =============================================
-- V14: Seed initial data (subscription plans + admin user)
-- =============================================

-- Subscription Plans
INSERT INTO subscription_plans (name, slug, description, max_units, max_users, price_cents, features, is_active)
VALUES
    (
        'Basic',
        'basic',
        'Ideal para condomínios pequenos. Inclui gestão de unidades, moradores, encomendas e comunicados.',
        50,
        100,
        9990,
        '{"modules": ["units", "residents", "parcels", "announcements", "complaints"], "support": "email", "storage_gb": 5}',
        true
    ),
    (
        'Pro',
        'pro',
        'Para condomínios de médio porte. Todos os recursos do Basic mais reservas, manutenção e relatórios avançados.',
        200,
        500,
        19990,
        '{"modules": ["units", "residents", "parcels", "announcements", "complaints", "bookings", "maintenance", "financial", "documents", "visitors"], "support": "email_phone", "storage_gb": 25}',
        true
    ),
    (
        'Enterprise',
        'enterprise',
        'Solução completa para grandes condomínios e administradoras. Sem limites, com suporte prioritário.',
        9999,
        9999,
        39990,
        '{"modules": ["units", "residents", "parcels", "announcements", "complaints", "bookings", "maintenance", "financial", "documents", "visitors", "audit", "api_access"], "support": "priority", "storage_gb": 100, "custom_branding": true}',
        true
    );

-- Platform Admin User
INSERT INTO users (email, password_hash, name, is_platform_admin, is_active, email_verified)
VALUES
    (
        'admin@convivium.com',
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
        'Admin Max',
        true,
        true,
        true
    );

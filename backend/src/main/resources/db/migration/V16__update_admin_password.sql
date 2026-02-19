-- V16: Update admin password to Admin@123
-- Credenciais: admin@convivium.com / Admin@123

UPDATE users
SET password_hash = '$2a$10$uem8nR8sHjzXQqs.w5EYIuH7By2JDnOgGAcChr5po9yt5WsLHgali'
WHERE email = 'admin@convivium.com';

# Banco PostgreSQL no Render

## 1. Vincular o banco ao serviço do backend

No Render, no seu **Web Service** do backend:

- Em **Environment** → **Add Environment Variable**
- Se você **conectou** o PostgreSQL ao serviço (Dashboard do banco → "Connect" → escolheu o serviço), o Render já adiciona automaticamente a variável **DATABASE_URL** (formato `postgresql://convivium:senha@host/convivium`).
- Defina também:
  - `SPRING_PROFILES_ACTIVE` = `production`
  - `JWT_SECRET` = uma chave longa e segura (ex.: gerada com `openssl rand -base64 64`)

O backend converte `DATABASE_URL` para JDBC e usa esse banco.

## 2. Limpar o banco (sistema antigo) e usar só o Convivium 2

Para **apagar dados e tabelas antigas** e deixar apenas o schema do Convivium 2 (Flyway):

1. Use o **PSQL Command** que o Render mostra na tela do PostgreSQL (ou a **External Database URL**).
2. Execute o script **uma vez**:

```bash
# Exemplo (substitua pela sua senha e host do Render):
PGPASSWORD=SUA_SENHA psql -h dpg-d1q1bbs9c44c7394lsr0-a.oregon-postgres.render.com -U convivium convivium -f backend/scripts/clear-database.sql
```

Ou, conectado ao banco:

```sql
\i backend/scripts/clear-database.sql
```

Isso faz:

- `DROP SCHEMA public CASCADE` (remove todas as tabelas/dados do sistema antigo)
- `CREATE SCHEMA public` e permissões para o usuário `convivium`

3. Na **próxima subida** do backend, o Flyway roda e cria todo o schema atual (migrações V1 a V21).

## 3. Variáveis de ambiente no Render (resumo)

### Opção A – Variáveis explícitas (recomendado)

Use **SPRING_PROFILES_ACTIVE** = `production` ou `prod` (os dois funcionam). Defina também:

| Variável | Exemplo / Descrição |
|----------|---------------------|
| `SPRING_PROFILES_ACTIVE` | `prod` ou `production` |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://HOST_INTERNO:5432/NOME_DO_BANCO` (use o host **interno** do Render, sem `.oregon-postgres.render.com`) |
| `SPRING_DATASOURCE_USERNAME` | usuário do banco (ex.: `convivium_7qyz_user`) |
| `SPRING_DATASOURCE_PASSWORD` | senha do banco |
| `JWT_SECRET` | **Obrigatório.** Chave com **pelo menos 32 caracteres** (ex.: `openssl rand -base64 48`) |
| `PORT` | definido pelo Render; não precisa definir |
| `APP_FRONTEND_URL` ou `CORS_ALLOWED_ORIGINS` | **Obrigatório para o front acessar a API.** URL do front sem barra no final (ex.: `https://convivium2.onrender.com`). Se faltar, o login dá CORS/403. |

A API sobe em **porta** = `$PORT` e **context-path** = `/convivium` (ex.: `https://seu-backend.onrender.com/convivium`).

**CORS:** Após definir `APP_FRONTEND_URL`, faça um novo deploy do backend. Nos logs de startup deve aparecer `CORS allowed origins: [https://convivium2.onrender.com]` (ou a URL que você definiu). Se aparecer `http://localhost:5173`, a variável não está definida no Render.

### Opção B – DATABASE_URL (Render auto)

Se o PostgreSQL estiver vinculado ao serviço, o Render injeta `DATABASE_URL`. O backend converte para JDBC. Defina também `SPRING_PROFILES_ACTIVE`, `JWT_SECRET` e CORS.

## 4. Internal vs External URL

- **Internal Database URL**: use para o backend rodando no Render (mesma rede).
- **External Database URL**: use para rodar o `psql` do seu computador (por exemplo, para executar `clear-database.sql`).

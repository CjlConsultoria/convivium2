# Configuração do Stripe (pagamentos)

O módulo Financeiro permite que o síndico pague a mensalidade do plano pela página do Stripe. Quando o Stripe **não** está configurado, o botão "Pagar" exibe a mensagem: *"Pagamentos ainda não estão disponíveis..."*.

## Uso local (já configurado)

Foi criado o arquivo **`backend/src/main/resources/application-local.yml`** com a chave secreta do Stripe. Esse arquivo está no `.gitignore` e não é commitado.

Para o backend carregar essa configuração, rode com o perfil **`local`**:

- **IntelliJ:** Run → Edit Configurations → na sua configuração do backend, em *VM options* coloque:  
  `-Dspring.profiles.active=local`
- **Terminal:**  
  `mvn spring-boot:run -Dspring-boot.run.profiles=local`  
  ou, com jar:  
  `java -Dspring.profiles.active=local -jar backend/target/convivium-backend-*.jar`

Depois reinicie o backend e teste "Pagar na página do Stripe" no Financeiro.

## Habilitar para produção

1. **Chave secreta**  
   No [Dashboard Stripe](https://dashboard.stripe.com/apikeys) (Desenvolvedores → Chaves da API), copie a **Chave secreta** (ex.: `sk_test_...` para teste).

2. **Configurar no backend**  
   No `application.yml` ou via variáveis de ambiente:

   ```yaml
   app:
     stripe:
       enabled: true
       secret-key: "sk_test_xxxxxxxxxxxxxxxx"   # sua chave secreta
       webhook-secret: ""   # para produção, preencha com whsec_...
       success-url: "http://localhost:5173/c/{condoId}/financial?payment=success"
       cancel-url: "http://localhost:5173/c/{condoId}/financial?payment=cancel"
   ```

   Ou em produção (ex.: Render):

   - `APP_STRIPE_ENABLED=true`
   - `APP_STRIPE_SECRET_KEY=sk_live_...`
   - `APP_STRIPE_WEBHOOK_SECRET=whsec_...`
   - Ajuste `success-url` e `cancel-url` para a URL real do front (ex.: `https://seu-app.com/c/{condoId}/financial`).

3. **Webhook (para atualizar faturas ao pagar)**  
   No Dashboard Stripe: Desenvolvedores → Webhooks → Adicionar endpoint:

   - **URL:** `https://seu-dominio.com/api/v1/webhooks/stripe`
   - **Eventos:** `checkout.session.completed`  
   Copie o **Signing secret** (`whsec_...`) e defina em `app.stripe.webhook-secret`.

   Em desenvolvimento local pode usar o [Stripe CLI](https://stripe.com/docs/stripe-cli) para encaminhar eventos:

   ```bash
   stripe listen --forward-to localhost:8080/api/v1/webhooks/stripe
   ```

   Use o `whsec_...` que o CLI mostrar.

Com `enabled: true` e `secret-key` preenchidos, o botão "Pagar na página do Stripe" passa a redirecionar para o Checkout. Sem isso, a API retorna `STRIPE_NOT_CONFIGURED` e o front exibe a mensagem amigável.

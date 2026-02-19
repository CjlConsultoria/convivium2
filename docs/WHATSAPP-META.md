# Integração WhatsApp via Meta (Cloud API)

O Convivium usa a **API oficial do WhatsApp da Meta** (Cloud API) para notificar moradores sobre encomendas e, no futuro, enviar comunicados. A Meta cobra por conversa (janela de 24h), sem mensalidade fixa de agregador.

---

## 1. O que está integrado

- **Encomendas:** quando o porteiro cadastra uma encomenda para um apartamento (com destinatário e telefone), o morador recebe no WhatsApp:
  1. Uma mensagem **template** com o **código para retirada** (o que o morador fala e o porteiro digita na validação).
  2. Se o porteiro enviar **foto** da encomenda, a foto é enviada em seguida no mesmo chat.

- **Comunicados:** preparado para uso futuro (enviar para lista de moradores).

---

## 2. Configuração no backend

Em `application.yml` ou variáveis de ambiente:

| Propriedade | Descrição |
|-------------|-----------|
| `app.whatsapp.enabled` | `true` para ativar o envio |
| `app.whatsapp.phone-number-id` | ID do número WhatsApp Business (Meta) |
| `app.whatsapp.access-token` | Token de acesso permanente (Meta) |
| `app.whatsapp.base-url` | URL pública do backend (ex: `https://api.seudominio.com`) — usada para o link da **foto** da encomenda; a Meta precisa acessar essa URL |
| `app.whatsapp.template.encomenda` | Nome do template aprovado (ex: `encomenda_na_portaria`) |

Exemplo em produção (Render):

- `APP_WHATSAPP_ENABLED=true`
- `APP_WHATSAPP_PHONE_NUMBER_ID=123456789`
- `APP_WHATSAPP_ACCESS_TOKEN=EAAG...`
- `APP_WHATSAPP_BASE_URL=https://convivium-backend.onrender.com`

---

## 3. Criar o template no Meta Business Manager

Para iniciar conversa com o usuário, a Meta exige um **template aprovado**.

1. Acesse [Meta Business Manager](https://business.facebook.com) → Conta WhatsApp → **Gerenciamento de modelos de mensagem** (Message Templates).
2. Crie um novo template:
   - **Nome:** `encomenda_na_portaria` (ou o nome que configurou em `app.whatsapp.template.encomenda`)
   - **Idioma:** Português (Brasil)
   - **Categoria:** Utilitário
   - **Corpo:** texto com dois parâmetros:

   ```
   Olá {{1}}, você tem uma encomenda na portaria.
   Código para retirada: {{2}}
   Apresente este código ao porteiro na hora de retirar.
   ```

   - {{1}} = nome do morador  
   - {{2}} = código do morador (6 dígitos)

3. Envie para aprovação. Após aprovado, o envio no Convivium passará a usar esse template.

---

## 4. "Nenhuma empresa disponível" ao criar o app

Na etapa **Empresa** (portfólio empresarial) do assistente de criação do app, a Meta exige que o app esteja ligado a uma **conta empresarial**. Se aparecer "Nenhuma empresa disponível":

1. **Criar uma conta empresarial (se ainda não tiver):**
   - Acesse [Meta Business Suite](https://business.facebook.com) (como na sua "Consultoria CJL").
   - Menu **Configurações** (ícone de engrenagem) → **Contas empresariais** (ou "Configurações da empresa").
   - Crie uma nova conta empresarial ou use a que já existe (ex.: a da Consultoria CJL).

2. **Garantir que a conta empresarial está ativa e você tem acesso:**
   - Em [business.facebook.com](https://business.facebook.com) → **Configurações** → verifique o **status** da conta (ativa, sem restrições).
   - Confirme que o seu perfil pessoal do Facebook tem **nível de acesso** de administrador (ou pelo menos "Funcionário") nessa conta empresarial.

3. **Voltar ao assistente do app:**
   - Em [developers.facebook.com](https://developers.facebook.com) → **Meus apps** → seu app → assistente de criação.
   - Na etapa **Empresa**, o portfólio (ex.: Consultoria CJL) deve aparecer na lista. Selecione-o e clique em **Avançar**.
   - Se ainda não aparecer, faça logout/login no Facebook e tente de novo, ou use outro navegador.

4. **Verificação da empresa (opcional para testes):**  
   Para publicar o app e usar em produção, a Meta pode pedir **verificação da empresa**. Para desenvolvimento e testes com seu próprio número, muitas vezes é possível seguir sem verificação. Você pode conectar um portfólio "não verificado" e concluir a verificação depois.

Depois de conectar o portfólio e avançar até o fim do assistente, adicione o produto **WhatsApp** ao app e siga a secção 5.

---

## 5. Obter Phone Number ID e Access Token

1. **No Meta for Developers:** [developers.facebook.com](https://developers.facebook.com/) → seu app → adicionar produto **WhatsApp** (se ainda não tiver).
2. Em **WhatsApp** → **API Setup**: copie o **Phone number ID** e gere um **Access token** (permanente) com permissões `whatsapp_business_messaging` e `whatsapp_business_management`.
3. Use esses valores em `app.whatsapp.phone-number-id` e `app.whatsapp.access-token`.

**Onde fica a conta WhatsApp no Business Manager:**  
Em [business.facebook.com](https://business.facebook.com) → **Configurações** (engrenagem) → no menu à esquerda, em **Contas** → **Contas do WhatsApp**. É aí que se vincula ou cria a conta WhatsApp Business do portfólio (ex.: Consultoria CJL). O app que você criou no Developers usa essa conta para enviar as mensagens; o **Phone number ID** e o **Access token** vêm do app em Developers (API Setup), não dessa tela de Contas do WhatsApp.

---

## 6. Telefone do morador

O número do destinatário vem do cadastro do **usuário** (campo **telefone**). O número deve estar em formato que inclua DDI (ex: 55 para Brasil) e DDD. Exemplo: `5511999999999`. O sistema remove caracteres não numéricos e normaliza.

---

## 7. Fluxo na prática

1. Porteiro registra a encomenda (unidade, destinatário, opcionalmente foto).
2. O sistema gera o código do morador e envia o **template** para o telefone do destinatário com nome e código.
3. Se o porteiro anexar uma foto, o sistema envia a **imagem** em seguida (mesmo chat, janela de 24h).
4. O morador vai à portaria, diz o **código**; o porteiro digita na tela de validação e marca como entregue.

Comunicados (envio em massa) podem usar um template próprio e a mesma API; a lógica de “quem recebe” (por unidade/role) pode ser implementada no módulo de comunicados.

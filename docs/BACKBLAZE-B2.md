# Integração Backblaze B2 (storage S3-compatible)

O Convivium usa storage compatível com S3. O **Backblaze B2** é uma opção mais barata que a AWS S3 e já está integrada: basta configurar o endpoint e as chaves.

## 1. Criar conta e bucket no B2

1. Acesse [Backblaze B2](https://www.backblaze.com/b2/sign-up.html) e crie uma conta.
2. No painel, ative o **B2 Cloud Storage** se ainda não estiver ativo.
3. **Criar bucket:**
   - **Buckets** → **Create a bucket**
   - Nome: por exemplo `convivium`
   - **Files in Bucket are**: escolha **Public** se as fotos/anexos forem acessíveis por URL direta (recomendado para fotos de encomendas e anexos de reclamações)
   - **Encryption**: opcional
   - **Object Lock**: desativado
   - Anote a **região** do bucket (ex.: `us-west-004`). O endpoint será `https://s3.<região>.backblazeb2.com`.

4. **Criar Application Key** (não use a master key):
   - **App Keys** → **Add a New Application Key**
   - Nome: ex. `convivium-backend`
   - **Allow access to Bucket(s)**: apenas o bucket criado (ex. `convivium`)
   - **Allow List, Write, Delete**: marque conforme necessário (List, Write, Delete para o backend fazer upload e listar)
   - **Read** e **Write** permitidos
   - Crie e **copie** o **keyID** e o **applicationKey** (a chave secreta só aparece uma vez).

## 2. Variáveis de ambiente (Render ou local)

Defina no serviço do backend (ex.: Render → Environment):

| Variável | Valor | Obrigatório |
|----------|--------|--------------|
| `APP_STORAGE_TYPE` | `s3` | Sim |
| `APP_STORAGE_ENDPOINT` | `https://s3.<região>.backblazeb2.com` | Sim |
| `APP_STORAGE_REGION` | Mesma do bucket (ex.: `us-west-004`) | Sim |
| `APP_STORAGE_BUCKET_NAME` | Nome do bucket (ex.: `convivium`) | Sim |
| `APP_STORAGE_ACCESS_KEY` | keyID da Application Key | Sim |
| `APP_STORAGE_SECRET_KEY` | applicationKey da Application Key | Sim |

### Exemplo (bucket `convivium` na região US East 005)

Se o endpoint do seu bucket for `s3.us-east-005.backblazeb2.com` (como no painel do B2):

```env
APP_STORAGE_TYPE=s3
APP_STORAGE_ENDPOINT=https://s3.us-east-005.backblazeb2.com
APP_STORAGE_REGION=us-east-005
APP_STORAGE_BUCKET_NAME=convivium
APP_STORAGE_ACCESS_KEY=<keyID da sua Application Key>
APP_STORAGE_SECRET_KEY=<applicationKey da sua Application Key>
```

### Outro exemplo (US West)

```env
APP_STORAGE_TYPE=s3
APP_STORAGE_ENDPOINT=https://s3.us-west-004.backblazeb2.com
APP_STORAGE_REGION=us-west-004
APP_STORAGE_BUCKET_NAME=convivium
APP_STORAGE_ACCESS_KEY=003abc123...
APP_STORAGE_SECRET_KEY=K003xyz...
```

## 3. Regiões B2

A região do bucket aparece na tela do bucket (campo **Endpoint**: `s3.<região>.backblazeb2.com`). Exemplos:

- **US East 005**: `https://s3.us-east-005.backblazeb2.com`
- **US West**: `https://s3.us-west-004.backblazeb2.com` ou `https://s3.us-west-002.backblazeb2.com`
- **EU Central**: `https://s3.eu-central-003.backblazeb2.com`

Use sempre **HTTPS** e a mesma região em `APP_STORAGE_REGION` e no endpoint.

## 4. Comportamento no Convivium

- Com `APP_STORAGE_TYPE=s3` e endpoint do B2 configurado, o backend usa **path-style** (compatível com B2).
- Os uploads (fotos de encomendas, anexos de reclamações etc.) vão para o bucket B2.
- A URL retornada para o front é no formato:  
  `https://s3.<região>.backblazeb2.com/<bucket>/<caminho>`  
  Se o bucket for **Public**, essa URL abre direto no navegador.

## 5. Custos (referência)

- Armazenamento: ~US$ 6/TB/mês.
- Egress: grátis até 3× o armazenamento/mês; depois ~US$ 0,01/GB.
- Para poucos GB, o custo costuma ficar em poucos dólares ou menos de US$ 1/mês.

## 6. Resumo rápido

1. Criar bucket B2 (público se quiser URLs diretas).
2. Criar Application Key com acesso ao bucket.
3. Definir as 6 variáveis acima no backend (Render ou `.env`).
4. Reiniciar o backend; novos uploads passam a ir para o B2.

# Storage: local e S3 no Render

## Local (desenvolvimento)

- **app.storage.type**: `local`
- Fotos são salvas na pasta **app.storage.local.path** (ex.: `./uploads`).
- As imagens são servidas em **GET /uploads/** (pasta do sistema).

## Online / Render (produção)

- **app.storage.type**: `s3`
- Defina no Render as variáveis de ambiente:

| Variável | Obrigatório | Descrição |
|----------|-------------|-----------|
| `APP_STORAGE_TYPE` | Sim | `s3` |
| `APP_STORAGE_BUCKET_NAME` | Sim | Nome do bucket |
| `APP_STORAGE_ACCESS_KEY` | Sim | Access key (S3/R2/B2) |
| `APP_STORAGE_SECRET_KEY` | Sim | Secret key |
| `APP_STORAGE_REGION` | Não | Região (default: `us-east-1`) |
| `APP_STORAGE_ENDPOINT` | Não | Deixe vazio para **AWS S3**. Para S3-compatible (R2, B2, MinIO) use a URL do serviço. |

### Exemplos

**AWS S3**

- Deixe `APP_STORAGE_ENDPOINT` vazio.
- Defina região, bucket, access key e secret key do IAM.

**Cloudflare R2** (custo-benefício)

- `APP_STORAGE_ENDPOINT`: `https://<account_id>.r2.cloudflarestorage.com`
- `APP_STORAGE_REGION`: `auto`
- Crie as chaves em R2 → Manage R2 API Tokens.

**Backblaze B2**

- Endpoint: `https://s3.<region>.backblazeb2.com`
- Crie Application Key no bucket e use como access-key/secret-key.

Depois de configurar, as fotos das encomendas passam a ser gravadas no storage configurado e as URLs retornadas apontam para esse storage.

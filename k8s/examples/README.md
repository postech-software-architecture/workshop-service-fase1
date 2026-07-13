# `k8s/examples` — templates de referência

Arquivos-modelo com **placeholders**, mantidos fora do caminho de `apply` para não aplicar valores
falsos por engano.

| Arquivo | Uso |
|---|---|
| `secret.example.yaml` | Template do Secret `workshop-secret`. Referência das chaves esperadas (`DB_USER`, `DB_PASSWORD`, `JWT_SECRET`, `WEBHOOK_ORCAMENTO_TOKEN`, `MAIL_USERNAME`, `MAIL_PASSWORD`). |

Os overlays **geram** o Secret via Kustomize (`secretGenerator`) ou ele é criado no deploy
(pipeline CD / Terraform). Este template serve para saber quais chaves preencher — **nunca**
commite um Secret com valores reais. Ver [`../README.md`](../README.md).

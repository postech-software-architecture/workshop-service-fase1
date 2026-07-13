# Overlay `dev` — local (kind/minikube)

Ambiente de desenvolvimento local. **Autocontido**: sobe a aplicação **e** o Postgres dentro do
cluster de uma vez — diferente do overlay `aws`, aqui **não** há RDS.

| Arquivo | Papel |
|---|---|
| `kustomization.yaml` | compõe a `base` + `postgres.yaml`; gera `workshop-config` e `workshop-secret` a partir dos `.env` |
| `config.env` | ConfigMap não sensível — `DB_HOST=postgres` (Service in-cluster) |
| `secret.env` | Secret com valores **DUMMY** (só local; nunca usar em nuvem) |
| `postgres.yaml` | Postgres in-cluster (Service `postgres`), criado com o mesmo `DB_USER`/`DB_PASSWORD` do `secret.env` |

Como `secret.env` só tem credenciais falsas de desenvolvimento, ele **é versionado** de propósito
(app e banco consistentes por construção). Em nuvem, os segredos vêm do CD/Terraform.

## Subir

```bash
kubectl apply -k k8s/overlays/dev
kubectl -n workshop rollout status deployment/workshop-service --timeout=180s
kubectl -n workshop port-forward svc/workshop-service 8080:8080
curl -fsS http://localhost:8080/actuator/health/liveness   # {"status":"UP"}
```

> HPA exige `metrics-server` no cluster — no `kind`/`minikube` instale à parte se for testar escala.

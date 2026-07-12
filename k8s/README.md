# Manifestos Kubernetes — `workshop-service`

Implantação do `workshop-service` em Kubernetes: Namespace, ConfigMap, Secret, Postgres
in-cluster (opcional), Deployment, Service, HPA e Ingress (opcional).

## Contrato

| Item | Valor |
|---|---|
| Namespace | `workshop` |
| Imagem | `ghcr.io/postech-software-architecture/workshop-service:<tag>` (Dev 2; use `sha-<sha>` em produção) |
| Porta | `8080` |
| Profile | `SPRING_PROFILES_ACTIVE=docker` |
| Liveness / Readiness | `GET /actuator/health/liveness` · `GET /actuator/health/readiness` |

> **Pré-requisito (Dev 2):** as probes só respondem `UP` porque o `SecurityConfig` libera
> `/actuator/health/**` (`permitAll`). Sem esse fix o pod não fica `Ready`.

## Arquivos (a ordem vem do prefixo numérico)

| Arquivo | Recurso |
|---|---|
| `00-namespace.yaml` | Namespace `workshop` |
| `01-configmap.yaml` | ConfigMap `workshop-config` (não sensível) |
| `02-secret.example.yaml` | **Template** do Secret `workshop-secret` — copiar para `02-secret.yaml` e preencher |
| `10-postgres.yaml` | Postgres in-cluster (Opção A) — **não aplicar** se o banco vier do Dev 4 |
| `20-deployment.yaml` | Deployment da aplicação (2 réplicas, probes, requests/limits) |
| `21-service.yaml` | Service ClusterIP :8080 |
| `30-hpa.yaml` | HPA (CPU 70% / memória 80%, 2–10 réplicas) |
| `50-ingress.yaml` | Ingress (opcional) |

## Config vs. Secret

- **ConfigMap** (não sensível): `SPRING_PROFILES_ACTIVE`, `DB_HOST`, `DB_PORT`, `DB_NAME`,
  `MAIL_HOST`, `MAIL_PORT`, `MAIL_FROM`, `NOTIFICACAO_CANAL`.
- **Secret** (sensível): `DB_USER`, `DB_PASSWORD`, `JWT_SECRET`, `WEBHOOK_ORCAMENTO_TOKEN`,
  `MAIL_USERNAME`, `MAIL_PASSWORD` (chaves de contrato com o Dev 1).

O Secret real (`02-secret.yaml`) está no `.gitignore` — **nunca commitar valores reais**.

## Banco — duas opções (mutuamente exclusivas)

- **Opção A (in-cluster):** aplicar `10-postgres.yaml`. `DB_HOST=postgres` (nome do Service).
  Usa os mesmos `DB_NAME`/`DB_USER`/`DB_PASSWORD` do ConfigMap/Secret.
- **Opção B (banco do Dev 4):** **não** aplicar `10-postgres.yaml`. Apontar `DB_HOST` no
  ConfigMap para o endpoint entregue pelo Terraform (ex.: `postgresql.workshop.svc.cluster.local`
  na Trilha A, ou o endpoint do RDS na Trilha B) e ajustar `DB_NAME`/credenciais conforme os
  outputs. Opcional: um `Service` `ExternalName` para manter o nome lógico `postgres`.

## Aplicar

```bash
# Secret real a partir do template (ou via CI, abaixo)
cp k8s/02-secret.example.yaml k8s/02-secret.yaml   # preencher os CHANGE_ME

kubectl apply -f k8s/00-namespace.yaml
kubectl apply -f k8s/01-configmap.yaml
kubectl apply -f k8s/02-secret.yaml
kubectl apply -f k8s/10-postgres.yaml               # PULAR na Opção B
kubectl apply -f k8s/20-deployment.yaml -f k8s/21-service.yaml
kubectl apply -f k8s/30-hpa.yaml
# kubectl apply -f k8s/50-ingress.yaml              # opcional

# ou tudo de uma vez (o prefixo 00 garante o Namespace primeiro):
kubectl apply -f k8s/
```

### Secret via CI (recomendado — nada sensível no repo)

```bash
kubectl -n workshop create secret generic workshop-secret \
  --from-literal=DB_USER="$DB_USER" \
  --from-literal=DB_PASSWORD="$DB_PASSWORD" \
  --from-literal=JWT_SECRET="$JWT_SECRET" \
  --from-literal=WEBHOOK_ORCAMENTO_TOKEN="$WEBHOOK_ORCAMENTO_TOKEN" \
  --from-literal=MAIL_USERNAME="$MAIL_USERNAME" \
  --from-literal=MAIL_PASSWORD="$MAIL_PASSWORD" \
  --dry-run=client -o yaml | kubectl apply -f -
```

## Validar

```bash
# Estático (offline):
kubeconform -strict -summary -skip Secret k8s/*.yaml   # ou: kubectl apply --dry-run=client -f k8s/

# Real (kind local, imagem do Dev 2):
kind create cluster --name workshop
kind load docker-image workshop-service:local --name workshop
kubectl apply -f k8s/00-namespace.yaml -f k8s/01-configmap.yaml
cp k8s/02-secret.example.yaml k8s/02-secret.yaml && kubectl apply -f k8s/02-secret.yaml
kubectl apply -f k8s/10-postgres.yaml
# apontar o Deployment para workshop-service:local para o teste local
kubectl apply -f k8s/20-deployment.yaml -f k8s/21-service.yaml -f k8s/30-hpa.yaml
kubectl -n workshop rollout status deploy/workshop-service --timeout=180s
kubectl -n workshop port-forward svc/workshop-service 8080:8080 &
curl -fsS http://localhost:8080/actuator/health/liveness    # {"status":"UP"}

# HPA (metrics-server necessário):
kubectl -n workshop get hpa workshop-service   # TARGETS não pode estar <unknown>
```

## Imagem privada no GHCR (opcional)

```bash
kubectl -n workshop create secret docker-registry ghcr-pull \
  --docker-server=ghcr.io --docker-username=<user> --docker-password=<PAT read:packages> \
  --dry-run=client -o yaml | kubectl apply -f -
```
E descomentar `imagePullSecrets` no `20-deployment.yaml`.

## Flyway

A aplicação migra o schema **no boot** (`spring.flyway.enabled`, `baseline-on-migrate`), então
não há Job de migração dedicado por padrão. Com `replicas > 1`, o Flyway usa lock de banco para
serializar — o `startupProbe` generoso do Deployment cobre o pod que espera o lock.

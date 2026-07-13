# Manifestos Kubernetes — `workshop-service`

Implantação do `workshop-service` em Kubernetes, organizada com **Kustomize**: uma `base` comum
e dois `overlays` de ambiente (`dev` e `aws`).

## Estrutura

```text
k8s/
├── base/                    # recursos comuns a todos os ambientes
│   ├── namespace.yaml       # Namespace workshop
│   ├── deployment.yaml      # Deployment (2 réplicas, probes, requests/limits, securityContext)
│   ├── service.yaml         # Service ClusterIP :8080
│   ├── hpa.yaml             # HPA CPU 70% + memória 850Mi/pod (2→10 réplicas)
│   └── kustomization.yaml
├── overlays/
│   ├── dev/                 # local (kind/minikube): Postgres IN-CLUSTER
│   │   ├── kustomization.yaml
│   │   ├── config.env       # ConfigMap (DB_HOST=postgres)
│   │   ├── secret.env       # Secret com valores DUMMY (só local)
│   │   └── postgres.yaml
│   └── aws/                 # EKS + RDS: banco é o RDS (não in-cluster)
│       ├── kustomization.yaml
│       ├── config.env       # ConfigMap (DB_HOST = endpoint do RDS, injetado no deploy)
│       ├── service-lb.yaml  # Service type=LoadBalancer → ELB público
│       └── README.md
├── examples/
│   └── secret.example.yaml  # TEMPLATE do Secret (referência; overlays geram o seu)
└── 50-ingress.yaml          # Ingress avulso (opcional, fora dos overlays)
```

## Contrato

| Item | Valor |
|---|---|
| Namespace | `workshop` |
| Imagem | `ghcr.io/postech-software-architecture/workshop-service:<tag>` (use `sha-<sha>` em produção) |
| Porta | `8080` |
| Profile | `SPRING_PROFILES_ACTIVE=docker` |
| Liveness / Readiness | `GET /actuator/health/liveness` · `GET /actuator/health/readiness` |

## Config vs. Secret

Cada overlay **gera** o `workshop-config` (ConfigMap) e o `workshop-secret` (Secret) a partir de
arquivos `.env` (`configMapGenerator` / `secretGenerator`, sem hash de nome).

- **ConfigMap** (não sensível): `SPRING_PROFILES_ACTIVE`, `DB_HOST`, `DB_PORT`, `DB_NAME`,
  `MAIL_HOST`, `MAIL_PORT`, `MAIL_FROM`, `NOTIFICACAO_CANAL`.
- **Secret** (sensível): `DB_USER`, `DB_PASSWORD`, `JWT_SECRET`, `WEBHOOK_ORCAMENTO_TOKEN`,
  `MAIL_USERNAME`, `MAIL_PASSWORD`.

> No overlay **aws** o `secretGenerator` **não** é usado: as credenciais de nuvem **não** vão ao
> git. O `workshop-secret` é criado no deploy (pipeline CD ou `kubectl create secret`) a partir
> dos outputs do Terraform / GitHub Secrets. Ver [`overlays/aws/README.md`](overlays/aws/README.md).

## Aplicar

```bash
# DEV local (kind/minikube) — app + Postgres in-cluster de uma vez:
kubectl apply -k k8s/overlays/dev
kubectl -n workshop rollout status deployment/workshop-service --timeout=180s
kubectl -n workshop port-forward svc/workshop-service 8080:8080 &
curl -fsS http://localhost:8080/actuator/health/liveness      # {"status":"UP"}

# AWS (EKS + RDS) — normalmente via pipeline CD; manualmente ver overlays/aws/README.md:
kubectl apply -k k8s/overlays/aws
kubectl -n workshop rollout status deployment/workshop-service
```

Pré-visualizar o YAML renderizado sem aplicar:

```bash
kubectl kustomize k8s/overlays/dev     # ou overlays/aws
```

## Escalabilidade (HPA)

O HPA escala de **2→10 réplicas** pelo **maior** entre dois sinais:

- **CPU** — `Utilization` 70% (relativa ao *request* de CPU). Métrica primária: reflete a demanda.
- **Memória** — `AverageValue` **absoluto** de `850Mi`/pod (rede de segurança). Absoluto de
  propósito: a JVM reserva `-XX:MaxRAMPercentage=75%` do limit (~768Mi de 1Gi), então a
  utilização relativa ao *request* (512Mi) ficaria cronicamente >100% e travaria o HPA no máximo.

Requer o `metrics-server` no cluster (instalado pelo Terraform no EKS; no `kind` local, instalar à parte).

```bash
kubectl -n workshop get hpa workshop-service   # TARGETS (cpu + memory) não pode estar <unknown>
```

## Imagem privada no GHCR (opcional)

Se o pacote no GHCR for privado, os nós do EKS não conseguem puxar a imagem (`ImagePullBackOff`).
Torne o pacote público **ou** crie um `imagePullSecret`:

```bash
kubectl -n workshop create secret docker-registry ghcr-pull \
  --docker-server=ghcr.io --docker-username=<user> --docker-password=<PAT read:packages> \
  --dry-run=client -o yaml | kubectl apply -f -
```

E descomente `imagePullSecrets` em `base/deployment.yaml`.

## Flyway

A aplicação migra o schema **no boot** (`spring.flyway.enabled`, `baseline-on-migrate`), sem Job
dedicado. Com `replicas > 1`, o Flyway serializa via lock de banco — o `startupProbe` generoso do
Deployment cobre o pod que aguarda o lock.

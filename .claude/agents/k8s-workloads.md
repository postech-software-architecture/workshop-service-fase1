# Agente: Kubernetes Workloads (manifestos da aplicação)

## Responsabilidade
Recuperar, converter e evoluir os manifestos Kubernetes da aplicação em `k8s/**`: merge da
branch `feat/dev3-kubernetes` em `main`, conversão para Kustomize (`base/` + overlays por
ambiente), troca do Service para NLB interno, HPA, requests/limits e variáveis de ambiente do
OpenTelemetry.

Atua nas fases **W2** (recuperar + converter) e **W4-B** (exposição privada + observabilidade).

## Fronteira

A regra que sustenta o paralelismo de 4 agentes: **exatamente um agente escreve num dado
caminho**. Se você precisar de uma mudança fora da lista Owns, peça ao agente dono — não edite.

### Owns
- `k8s/**` inteiro: `base/`, `overlays/homolog/`, `overlays/prod/`, `k8s/README.md`,
  `k8s/examples/`
- Namespace, ConfigMap, template de Secret, Deployment, Service, HPA, PDB, ServiceAccount,
  NetworkPolicy — tudo que é manifesto
- As **variáveis de ambiente do OTel no manifesto** (`OTEL_SERVICE_NAME`,
  `OTEL_EXPORTER_OTLP_ENDPOINT`, `OTEL_RESOURCE_ATTRIBUTES`)
- O merge da branch `feat/dev3-kubernetes` para `main` (parte de manifestos)

### Não toca
- `src/**` — nenhum arquivo Java. O agente `otel-app` é dono do código de instrumentação e do
  `logback-spring.xml`; **você é dono só do env no manifesto**
- `Dockerfile` — se o UID do usuário `spring` precisar mudar, é `cicd-pipelines` (dono único);
  você **adapta o manifesto ao UID real da imagem**
- Qualquer `.tf` — VPC/EKS/LB Controller é `terraform-cluster`; RDS é `terraform-database`;
  Lambda/API Gateway/VPC Link é `serverless-lambda`
- `.github/workflows/**` — corpo do YAML é `cicd-pipelines`
- Dashboards Grafana e OTel Collector no cluster — `observability-platform` é dono

## Contexto do projeto

### Estado real verificado (o repo ganha do doc de planejamento)

Os docs de planejamento afirmam que "Kustomize já existe" e que "`k8s/base` já existe". **Não é
verdade.** Verificado com `git ls-tree`:

- **`k8s/` não existe em `main` nem na branch atual** (`feat/dev4-terraform-iac`). Existe
  somente na branch **não mergeada** `feat/dev3-kubernetes`, em **YAML puro — não Kustomize**,
  aplicado por ordem de prefixo numérico com `kubectl apply -f k8s/`.
- Recuperar + converter é **pré-requisito da fase**, não um ajuste. É a primeira tarefa da W2.

Manifestos reais que existem em `feat/dev3-kubernetes:k8s/`:

| Arquivo | Recurso | Destino na conversão |
|---|---|---|
| `00-namespace.yaml` | Namespace `workshop` | `base/namespace.yaml` |
| `01-configmap.yaml` | ConfigMap `workshop-config` | `base/configmap.yaml` + patches por overlay |
| `10-postgres.yaml` | Service headless + StatefulSet Postgres in-cluster | **REMOVER** — o RDS gerenciado substitui |
| `20-deployment.yaml` | Deployment, 2 réplicas, RollingUpdate `maxUnavailable: 0` | `base/deployment.yaml` |
| `21-service.yaml` | Service **ClusterIP** :8080 | `base/service.yaml`, virando **NLB interno** no overlay AWS |
| `30-hpa.yaml` | HPA CPU 70%, 2–10 réplicas | `base/hpa.yaml` |
| `50-ingress.yaml` | Ingress host `workshop.local` | **REMOVER** — órfão, sem controller |
| `README.md` | Documentação dos manifestos | `k8s/README.md`, reescrito |
| `examples/secret.example.yaml` | Template do Secret `workshop-secret` | `k8s/examples/` (mantém) |

Contrato já estabelecido pelos manifestos existentes — preserve:
- Namespace `workshop`; porta `8080`; `SPRING_PROFILES_ACTIVE=docker`
- Imagem `ghcr.io/postech-software-architecture/workshop-service:<tag>` (tag imutável
  `sha-<sha>` em produção, nunca `latest`)
- Probes: `/actuator/health/readiness` (startup + readiness) e `/actuator/health/liveness`
- `startupProbe` generoso (`failureThreshold: 30`, `periodSeconds: 5`, ~150s) porque o boot é
  lento: Spring + Flyway migrando, e com `replicas > 1` um pod espera o lock do Flyway
- Chaves de ConfigMap: `SPRING_PROFILES_ACTIVE`, `DB_HOST`, `DB_PORT`, `DB_NAME`, `MAIL_HOST`,
  `MAIL_PORT`, `MAIL_FROM`, `NOTIFICACAO_CANAL`
- Chaves de Secret: `DB_USER`, `DB_PASSWORD`, `JWT_SECRET`, `WEBHOOK_ORCAMENTO_TOKEN`,
  `MAIL_USERNAME`, `MAIL_PASSWORD`
- HPA **só por CPU** — a JVM reserva `MaxRAMPercentage` do limit e segura, então utilização de
  memória fica cronicamente alta e não reflete carga. Manter o comentário que explica isso.

### `/actuator/health/**` já está liberado
`SecurityConfig.java:61-64` já tem `permitAll()` para `"/actuator/health/**", "/actuator/info"`.
**Não precisa mudar nada em `src/`** — as probes funcionam. Se um refactor de `otel-app`
remover esse matcher, o pod nunca fica `Ready`: sinalize, não corrija você mesmo (`src/` não é
seu). O matcher `"/api/v1/ordens-servico/*/status"` também é público e precisa de auditoria
pelo `openapi`/`otel-app` — não é seu escopo.

### `runAsUser` precisa ser o UID numérico real da imagem

`runAsNonRoot: true` sozinho **quebra o pod**: o kubelet exige UID numérico para verificar que
não é root, e o Dockerfile declara `USER spring:spring` **por nome**.

Divergência real entre branches, verificada:

```dockerfile
# Dockerfile em main / feat/dev4-terraform-iac (linhas 27-28):
&& addgroup -S spring \
&& adduser -S -G spring -h /app -s /sbin/nologin spring
# sem -u/-g → o Alpine atribui UID/GID automaticamente (tipicamente 100/101)

# Dockerfile em feat/dev3-kubernetes (linhas 27-28):
&& addgroup -g 1000 -S spring \
&& adduser -u 1000 -S -G spring -h /app -s /sbin/nologin spring
# UID fixado em 1000 → e por isso 20-deployment.yaml usa runAsUser: 1000
```

O `runAsUser: 1000` da branch dev3 **só é válido se o Dockerfile com `-u 1000` vier no mesmo
merge**. Se a imagem publicada vier do Dockerfile de `main` (sem `-u`), o UID é **100** e
`runAsUser: 1000` faz o pod falhar. Determine o UID real da imagem que a pipeline publica:

```bash
docker run --rm --entrypoint id ghcr.io/postech-software-architecture/workshop-service:<tag>
# uid=100(spring) gid=101(spring)   → usar runAsUser: 100
```

**Validar em cluster real (kind ou EKS), não só `--dry-run`.** `kubectl apply --dry-run=client`
não detecta esse erro — ele só aparece quando o kubelet tenta iniciar o container.

## Padrão da estrutura Kustomize (entrega da W2)

```text
k8s/
├── base/
│   ├── kustomization.yaml
│   ├── namespace.yaml
│   ├── configmap.yaml
│   ├── deployment.yaml
│   ├── service.yaml          # ClusterIP no base
│   ├── hpa.yaml
│   ├── pdb.yaml
│   └── serviceaccount.yaml
├── overlays/
│   ├── homolog/
│   │   ├── kustomization.yaml
│   │   └── patch-*.yaml
│   └── prod/
│       ├── kustomization.yaml
│       ├── patch-service-nlb.yaml   # ClusterIP → NLB interno (W4-B)
│       └── patch-resources.yaml
├── examples/
│   └── secret.example.yaml
└── README.md
```

```yaml
# base/kustomization.yaml
apiVersion: kustomize.config.k8s.io/v1beta1
kind: Kustomization
namespace: workshop
commonLabels:
  app.kubernetes.io/name: workshop-service
  app.kubernetes.io/part-of: workshop-service
resources:
  - namespace.yaml
  - serviceaccount.yaml
  - configmap.yaml
  - deployment.yaml
  - service.yaml
  - hpa.yaml
  - pdb.yaml
```

```yaml
# overlays/prod/kustomization.yaml
apiVersion: kustomize.config.k8s.io/v1beta1
kind: Kustomization
namespace: workshop
resources:
  - ../../base
patches:
  - path: patch-service-nlb.yaml
  - path: patch-resources.yaml
images:
  - name: ghcr.io/postech-software-architecture/workshop-service
    newTag: sha-PLACEHOLDER   # o CD substitui pela tag imutável; nunca "latest"
configMapGenerator:
  - name: workshop-config
    behavior: merge
    literals:
      - DB_HOST=<endpoint do RDS — output do repo de banco>
      - DB_NAME=<output do repo de banco>
```

Substituição declarativa por Kustomize/`configMapGenerator` ou secret gerado na pipeline —
**nunca `sed` em `config.env` versionado** como mecanismo permanente.

## W4-B — Exposição privada: ClusterIP → NLB interno

Hoje `21-service.yaml` é `type: ClusterIP` e **nenhum LB é provisionado**. `50-ingress.yaml`
existe **órfão**, apontando para `workshop.local`, sem nenhum Ingress Controller no cluster.
**A exposição para o VPC Link precisa ser criada de zero** — não é ajuste de algo existente.

```yaml
# overlays/prod/patch-service-nlb.yaml
apiVersion: v1
kind: Service
metadata:
  name: workshop-service
  annotations:
    # NLB interno — unico destino aceito pelo VPC Link do API Gateway.
    # "internal" e o que fecha o bypass: sem DNS publico, o Gateway e a unica borda.
    service.beta.kubernetes.io/aws-load-balancer-type: "external"
    service.beta.kubernetes.io/aws-load-balancer-nlb-target-type: "ip"
    service.beta.kubernetes.io/aws-load-balancer-scheme: "internal"
    service.beta.kubernetes.io/aws-load-balancer-healthcheck-path: "/actuator/health/readiness"
    service.beta.kubernetes.io/aws-load-balancer-healthcheck-port: "8080"
spec:
  type: LoadBalancer
```

- Depende do **AWS Load Balancer Controller**, que `terraform-cluster` instala na **W2** — uma
  onda antes. Se o controller não estiver disponível no Academy, o fallback registrado em ADR é
  o `Service type=LoadBalancer` nativo com as annotations de NLB interno.
- **Remover `50-ingress.yaml`** na W4-B. Ele nunca funcionou e um segundo caminho de entrada
  contradiz "API Gateway é a única borda".
- O DNS do NLB é consumido por `serverless-lambda` para criar o VPC Link. Publique-o como
  saída da pipeline de deploy, não copiado à mão.

## W4-B — Env do OpenTelemetry e Deployment endurecido

Você é dono do **env no manifesto**; `otel-app` é dono do código Java e do `-javaagent`.

```yaml
env:
  - name: OTEL_SERVICE_NAME
    value: "workshop-service"
  - name: OTEL_RESOURCE_ATTRIBUTES
    value: "service.namespace=workshop,deployment.environment=prod"
  - name: OTEL_EXPORTER_OTLP_ENDPOINT
    value: "http://otel-collector.observability.svc.cluster.local:4318"
  - name: OTEL_EXPORTER_OTLP_PROTOCOL
    value: "http/protobuf"
  - name: OTEL_TRACES_EXPORTER
    value: "otlp"
  - name: OTEL_METRICS_EXPORTER
    value: "otlp"
```

O endpoint do Collector é contrato com `observability-platform` — combine o Service/namespace
antes de fixar. Credenciais do Grafana Cloud (`instanceID:token`) vão no Secret, nunca no
ConfigMap e nunca versionadas.

```yaml
# base/deployment.yaml — trechos que mudam na W4-B
resources:
  requests:
    cpu: "250m"
    memory: "512Mi"   # revisar para cima se o -javaagent do OTel elevar o baseline
  limits:
    cpu: "1000m"
    memory: "1Gi"
securityContext:
  allowPrivilegeEscalation: false
  runAsNonRoot: true
  runAsUser: 100         # UID NUMERICO real da imagem — ver secao acima. Validar em cluster.
  runAsGroup: 101
  readOnlyRootFilesystem: true
  capabilities:
    drop: ["ALL"]
  seccompProfile:
    type: RuntimeDefault
```

`readOnlyRootFilesystem: true` exige `emptyDir` em `/tmp` (a JVM escreve lá). Adicione o
volume ou não ative a flag — não deixe o pod em CrashLoop por meia mudança.

Adicione também PDB (2 réplicas → `minAvailable: 1`) e ServiceAccount dedicada.

## Gate

### G2 (fim da W2)
```bash
# 1. Manifestos em main, convertidos para Kustomize
kustomize build k8s/overlays/prod    # renderiza sem erro
kustomize build k8s/overlays/homolog
kubeconform -strict -summary -skip Secret <(kustomize build k8s/overlays/prod)

# 2. Cluster pronto (dono: terraform-cluster; você depende)
kubectl get nodes                                   # todos Ready
kubectl -n kube-system get deploy metrics-server    # Running / Available
```
- `10-postgres.yaml` **removido** — nenhum Postgres em pod no overlay de cloud
- `50-ingress.yaml` removido (ou removido na W4-B, conforme cronograma)

### G4 (fim da W4) — itens que dependem de você
```bash
kubectl -n workshop rollout status deploy/workshop-service --timeout=300s
kubectl -n workshop get pods                        # 2/2 Ready, sem restarts
kubectl -n workshop get hpa workshop-service        # TARGETS != <unknown>
kubectl -n workshop top pods                        # métricas chegando
kubectl -n workshop get svc workshop-service        # EXTERNAL-IP = DNS do NLB interno

# G4 item 6 — PROVA DE QUE NAO HA BYPASS DO API GATEWAY:
curl -m 10 https://<dns-do-nlb>/actuator/health     # DEVE FALHAR (timeout / no route)
```
Esse `curl` **falhando** é o critério, não um erro. Um NLB `internal` não tem DNS resolvível
de fora da VPC. Se ele responder, o bypass existe e o G4 está reprovado.

Rollback documentado e testado:
```bash
kubectl -n workshop rollout undo deploy/workshop-service
kubectl -n workshop rollout history deploy/workshop-service
```

## Riscos que você mitiga
| Risco | Mitigação |
|---|---|
| `k8s/` fora de `main`, em YAML puro | Merge + conversão Kustomize como **primeira** tarefa da W2; sub-gate do G2 |
| Postgres em pod concorrendo com o RDS | `10-postgres.yaml` removido na W2; `DB_HOST` aponta para o endpoint do RDS |
| Ingress órfão como segunda borda | `50-ingress.yaml` removido; só o NLB interno + API Gateway |
| `runAsUser` errado derruba o pod | UID numérico real da imagem, verificado com `docker run --entrypoint id`; validar em cluster real |
| `readOnlyRootFilesystem` sem `/tmp` | `emptyDir` em `/tmp` no mesmo commit da flag |
| Probes poluindo métricas de latência | Sinalizar a `otel-app` / `observability-platform`; filtro é do lado deles |
| Flyway concorrente com 2 réplicas | `startupProbe` generoso já cobre o pod que espera o lock; não reduzir `failureThreshold` |
| Agente OTel elevando memória/startup | Medir e ajustar requests/limits e `startupProbe` na mesma onda |

## Como usar este agente
1. Recuperar os manifestos reais antes de escrever qualquer coisa:
   `git show feat/dev3-kubernetes:k8s/<arquivo>` para os 9 arquivos listados na tabela, e
   `git show feat/dev3-kubernetes:k8s/README.md` para o contrato já documentado.
2. **W2:** merge de `feat/dev3-kubernetes` em `main`; converter para `base/` + overlays por
   ambiente; remover `10-postgres.yaml`; provar `kustomize build` nos dois overlays.
3. **W4-B:** patch do Service para NLB interno; remover `50-ingress.yaml`; env do OTel; HPA +
   requests/limits revisados; PDB; endurecimento do `securityContext` com o UID correto.
4. Preservar o que já está adequado: 2 réplicas, `maxUnavailable: 0`, as três probes, HPA só
   por CPU, non-root, capabilities dropadas, ConfigMap e Secret separados.
5. Validar sempre em cluster real além do `--dry-run`: `--dry-run` não pega `runAsUser`, não
   pega probe quebrada, não pega `readOnlyRootFilesystem` sem volume.
6. Nunca editar `src/**`, `Dockerfile`, `.tf` ou workflows. Nunca commitar Secret com valor
   real — só o template em `k8s/examples/`.

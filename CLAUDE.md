# workshop-service

API REST de gestão de oficina mecânica — Spring Boot 3.4.1, Java 21, PostgreSQL, Clean Architecture.

## Estrutura de camadas

```
api/            → Controllers REST + DTOs (entrada HTTP)
application/    → Use Cases (regras de negócio, um arquivo por ação)
domain/         → Entidades, Value Objects, Enums, interfaces de repositório (puro, sem Spring)
infrastructure/ → JPA Entities, Mappers, Spring Data repos, implementações de repositório
```

## Agentes especializados

Para qualquer nova feature, use os agentes abaixo em vez de implementar tudo em um único passo.
Os prompts-modelo ficam em `.claude/agents/`.

### Camada de aplicação (features do domínio da oficina)

| Agente | Arquivo | Quando usar |
|---|---|---|
| **migration** | `.claude/agents/migration.md` | Nova tabela ou alteração de schema |
| **domain** | `.claude/agents/domain.md` | Nova entidade, value object, enum ou interface de repositório |
| **infrastructure** | `.claude/agents/infrastructure.md` | JPA entity, mapper, Spring Data repo, RepositoryImpl |
| **application** | `.claude/agents/application.md` | Novos use cases |
| **api** | `.claude/agents/api.md` | Novos controllers e DTOs |
| **openapi** | `.claude/agents/openapi.md` | Manutenção do contrato OpenAPI |
| **tests** | `.claude/agents/tests.md` | Testes unitários e de integração |

#### Ordem de execução para uma feature completa

```
migration ──┐
            ├──► domain ──► infrastructure ──► application ──► api ──► tests
            │              (paralelo com application)
```

- `migration` e `domain` podem rodar em paralelo
- `infrastructure` e `application` podem rodar em paralelo (ambos dependem apenas do `domain`)
- `api` depende do `application`
- `tests` depende de tudo

### Plataforma e infraestrutura (Fase 3)

Agentes de infra, cloud, observabilidade e governança. Plano de orquestração completo
(ondas W0–W7, gates G0–G6, caminho crítico) em
`~/Downloads/planejamento-fase-3/10-plano-de-orquestracao-e-agentes.md`.

| Agente | Arquivo | Owns | Ondas |
|---|---|---|---|
| **terraform-cluster** | `.claude/agents/terraform-cluster.md` | VPC, EKS, node group, LB Controller, contrato de outputs | W0, W1, W2 |
| **terraform-database** | `.claude/agents/terraform-database.md` | Subnet group, SG, `aws_db_instance`, backup | W3 |
| **serverless-lambda** | `.claude/agents/serverless-lambda.md` | Handler Java 21, JWT, IaC de Lambda + API Gateway + VPC Link | W0, W4-A |
| **k8s-workloads** | `.claude/agents/k8s-workloads.md` | `k8s/**` — Kustomize, NLB interno, HPA | W2, W4-B |
| **otel-app** | `.claude/agents/otel-app.md` | `src/**` + `logback-spring.xml` — OTel, MDC, claims JWT | W2, W4-B, W5 |
| **observability-platform** | `.claude/agents/observability-platform.md` | Grafana Cloud, OTel Collector, 6 dashboards, alertas | W0, W5, W6 |
| **cicd-pipelines** | `.claude/agents/cicd-pipelines.md` | `.github/workflows/**` dos 4 repos | W1, W4-A, W6 |
| **repo-governance** | `.claude/agents/repo-governance.md` | `gh` CLI — repos, branch protection, Environments, secrets | W1, W6 |
| **docs-architecture** | `.claude/agents/docs-architecture.md` | `docs/architecture/` — ADRs, RFCs, diagramas, ER, vídeo | W1, W3, W5, W7 |

**Regra de fronteira:** exatamente **um** agente escreve num dado caminho. É o que torna
seguro o paralelismo de até 4 agentes por onda — cada prompt declara `Owns` e `Não toca`.

**Co-agendamento obrigatório:** toda onda que toca `src/` roda `tests` junto, porque o gate
do JaCoCo é BUNDLE 80% INSTRUCTION e código de instrumentação sem teste quebra o build.

## Convenções importantes

- Migrations: `V0.{YYYYMMDDHHmmss}__{descricao}.sql`
- Use cases: método sempre chamado `executar(...)`, um arquivo por ação
- Enums de domínio ficam em `domain/enums/`
- `RegraDeNegocioException` → HTTP 422; `RecursoNaoEncontradoException` → HTTP 404
- Cobertura mínima: **80%** (JaCoCo — build falha se não atingir)
- Formatação: `spring-javaformat-plugin` valida no `mvn validate`
- Testes de integração estendem `PostgresTestContainer` e têm sufixo `IT`

## Rodar localmente

```bash
./mvnw spring-boot:run          # sobe app + docker-compose automaticamente
./mvnw verify                   # testes + cobertura + formatação
./mvnw spring-javaformat:apply  # corrige formatação
```

Neste ambiente (Colima), os testes de integração exigem o override de host:

```bash
TESTCONTAINERS_HOST_OVERRIDE=localhost ./mvnw verify
```

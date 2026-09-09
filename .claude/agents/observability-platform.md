# Agente: Observability Platform (Grafana Cloud)

## Responsabilidade
Montar a plataforma de observabilidade da Fase 3: stack no Grafana Cloud, OTel Collector no EKS,
**6 dashboards versionados em JSON**, alertas com canal de notificação e runbooks.

Atua em três ondas: **W0** (spike de ingest — destrava a W5), **W5** (Collector, dashboards,
alertas) e **W6** (evidências finais).

Este agente cuida da **plataforma**. Não escreve código Java.

## Contexto do projeto

### Decisão de ferramenta: OpenTelemetry + Grafana Cloud

**ADR-006 substitui explicitamente a recomendação de New Relic dos documentos de planejamento.**

O doc `06-observabilidade-*.md` recomenda New Relic ("a live destacou que ele tende a ser mais
sustentável para estudantes que o trial curto do Datadog"), e o doc 01 §6 repete. Essa
recomendação **não vale mais**. A escolha é OpenTelemetry + Grafana Cloud, e o motivo é o que o
próprio doc 06 pede duas linhas depois: "o plano de telemetria deve permanecer o mais portável
possível — nomes de métricas, campos de log e propagação W3C `traceparent` não devem depender de
terminologia exclusiva do fornecedor". OTel entrega exatamente isso; o free tier do Grafana Cloud
é permanente, não trial.

Consequências práticas de leitura dos docs: onde eles dizem "agente APM", leia **OTel Collector**;
onde dizem "NRQL", leia **PromQL / LogQL / TraceQL**. O plano de telemetria (convenções, métricas,
campos de log) permanece válido palavra por palavra — só o destino muda.

### Situação de partida: greenfield completo — verificado no repo

O doc 06 lista o que "não foi encontrado". Confirmado arquivo por arquivo, e é pior do que
parece: não há **nada** além do Actuator.

| Item | Verificação | Estado |
|---|---|---|
| Actuator | `grep actuator pom.xml` → `spring-boot-starter-actuator` | única dependência de observabilidade |
| micrometer-registry-otlp | `grep -n "micrometer\|otel\|opentelemetry" pom.xml` | **zero ocorrências** |
| Arquivo logback | `find . -name "logback*" -not -path "./target/*"` | **nenhum arquivo** |
| MDC / correlation id | `grep -rn "MDC" src/` e `grep -rni "correlation" src/` | **zero ocorrências** |
| Tracing | `grep -rni "traceparent\|tracing" src/` | **zero ocorrências** |

Logs hoje são SLF4J em texto plano com a configuração default do Spring Boot. Não há
`logback-spring.xml` para editar — ele **nasce** na W2, escrito pelo `otel-app`.

O que **já existe** e é reaproveitado:
- `historico_status_os` (migration `V0.20260507101000`): `ordem_servico_id`, `status_anterior`,
  `status_novo`, `data_transicao`, `usuario_id`, `usuario_username`, mais colunas de auditoria.
  É a fonte do dashboard de tempo por etapa.
- `Documento.mascarado()` em `src/main/java/.../domain/valueobjects/Documento.java:99` — já usado
  em `ClienteController`, `VeiculoController` e `OrdemServicoResponse`. É a primitiva de redaction
  do G5.
- `metrics-server` (para HPA) — **não** substitui a coleta de CPU/memória da plataforma.

### Restrição operacional
AWS Academy: credencial expira em ~4h. Gerar tráfego e capturar evidência tem janela curta.
Exporte dashboard e capturas **assim que** os painéis tiverem dados — a conta/ambiente pode
desaparecer depois do vídeo.

## Fronteira

### Owns
- Conta e stack Grafana Cloud: datasources, tokens de ingest, limites do free tier
- **OTel Collector no EKS**: Helm values / manifesto do Collector, ServiceAccount, RBAC, pipelines
- `docs/observability/dashboards/*.json` — os 6 dashboards versionados
- `docs/observability/alerts/*.yaml` — regras de alerta e contact point
- `docs/observability/runbooks/*.md` — runbooks versionados
- Health/uptime monitor (Grafana Synthetic Monitoring)

### Não toca
- **Código Java** — `src/**`, `pom.xml`, `logback-spring.xml`, `CorrelationIdFilter`, métricas de
  negócio: tudo é do agente **`otel-app`**. Você **consome** o que ele emite e diz **o que precisa
  ser emitido**; não instrumenta
- `k8s/**` dos workloads da app (Deployment, Service, HPA, env OTel) — é do `k8s-workloads`. O
  Collector é seu; o `OTEL_EXPORTER_OTLP_ENDPOINT` no Deployment da app é dele
- `.tf` — `terraform-cluster` / `terraform-database`
- `.github/workflows/**` — `cicd-pipelines`
- Capturas e roteiro do vídeo — `docs-architecture`

Contrato com o `otel-app`: você declara os nomes de métrica, os campos de log e os atributos de
recurso; ele implementa. Divergência de nome = painel vazio, e isso só aparece na W5 quando não
há mais onda de sobra.

## W0 — Spike de ingest (destrava a W5)

Este spike é uma **aresta pontilhada `G0 → W5`** no grafo de ondas: "token Grafana pronto". Se
falhar, a W5 inteira não tem para onde enviar telemetria, e a descoberta na W5 custa a entrega.

**Escopo:** provar **1 métrica + 1 log + 1 trace** via OTLP, do laptop, sem EKS e sem código da
app. Branch descartável.

Auth do Grafana Cloud é `instanceID:token` em Basic — cada sinal tem um `instanceID` distinto
(Prometheus, Loki e Tempo são stacks separadas):

```bash
# Endpoint e instanceID vêm do painel "OpenTelemetry" da stack no Grafana Cloud
export OTLP_ENDPOINT="https://otlp-gateway-prod-us-east-0.grafana.net/otlp"
export GRAFANA_INSTANCE_ID="<numérico>"
export GRAFANA_API_TOKEN="glc_..."
export OTLP_AUTH=$(printf '%s:%s' "$GRAFANA_INSTANCE_ID" "$GRAFANA_API_TOKEN" | base64 -w0)
```

Collector local mínimo:

```yaml
# spike/collector.yaml
receivers:
  otlp:
    protocols:
      grpc: { endpoint: 0.0.0.0:4317 }
      http: { endpoint: 0.0.0.0:4318 }

processors:
  batch: {}
  resource:
    attributes:
      - { key: service.name, value: workshop-service, action: upsert }
      - { key: deployment.environment, value: spike, action: upsert }

exporters:
  otlphttp/grafana:
    endpoint: ${OTLP_ENDPOINT}
    auth: { authenticator: basicauth/grafana }

extensions:
  basicauth/grafana:
    client_auth:
      username: ${GRAFANA_INSTANCE_ID}
      password: ${GRAFANA_API_TOKEN}

service:
  extensions: [basicauth/grafana]
  pipelines:
    metrics: { receivers: [otlp], processors: [resource, batch], exporters: [otlphttp/grafana] }
    logs:    { receivers: [otlp], processors: [resource, batch], exporters: [otlphttp/grafana] }
    traces:  { receivers: [otlp], processors: [resource, batch], exporters: [otlphttp/grafana] }
```

```bash
docker run --rm -p 4317:4317 -p 4318:4318 \
  -v "$PWD/spike/collector.yaml:/etc/otelcol/config.yaml" \
  -e GRAFANA_INSTANCE_ID -e GRAFANA_API_TOKEN -e OTLP_ENDPOINT \
  otel/opentelemetry-collector-contrib:latest

# 1 trace de teste
curl -sS -X POST http://localhost:4318/v1/traces \
  -H 'Content-Type: application/json' -d @spike/trace.json -w '\n%{http_code}\n'
```

**Veredicto a registrar (entra no ADR-006):**
- Os três sinais aparecem nas UIs de Metrics/Logs/Traces? Prints de cada um.
- Limites do free tier medidos: séries de métrica, GB de log, spans/mês, **retenção**. A
  retenção precisa cobrir da geração de dados até a avaliação — se não cobrir, o export JSON +
  capturas passam de "boa prática" a **obrigatórios**.
- Endpoint, `instanceID` de cada sinal, e formato do token → entregues ao `repo-governance`
  como `GRAFANA_CLOUD_OTLP_ENDPOINT`, `GRAFANA_CLOUD_INSTANCE_ID`, `GRAFANA_CLOUD_API_TOKEN`.
- Se o free tier não sustentar os 3 sinais: acionar fallback documentado **antes da W1**, não na W5.

## W5 — OTel Collector no EKS

Um Collector como Deployment (recebe OTLP da app e da Lambda) e, para logs de container, um
DaemonSet com `filelog`. Pipelines: **OTLP → Loki (logs) / Tempo (traces) / Prometheus (métricas)**.

```yaml
# docs/observability/collector/values.yaml  (chart: open-telemetry/opentelemetry-collector)
mode: deployment
image: { repository: otel/opentelemetry-collector-contrib }

extraEnvs:
  - name: GRAFANA_INSTANCE_ID
    valueFrom: { secretKeyRef: { name: grafana-cloud, key: instanceId } }
  - name: GRAFANA_API_TOKEN
    valueFrom: { secretKeyRef: { name: grafana-cloud, key: apiToken } }

config:
  receivers:
    otlp:
      protocols:
        grpc: { endpoint: 0.0.0.0:4317 }
        http: { endpoint: 0.0.0.0:4318 }
    k8s_cluster:
      collection_interval: 30s        # réplicas desejadas/disponíveis, pods não prontos
    kubeletstats:
      collection_interval: 30s
      metrics: { k8s.pod.cpu.utilization: { enabled: true },
                 k8s.pod.memory.working_set: { enabled: true } }

  processors:
    batch: { timeout: 5s, send_batch_size: 1024 }
    memory_limiter: { check_interval: 1s, limit_percentage: 80, spike_limit_percentage: 25 }
    k8sattributes:
      extract:
        metadata: [k8s.namespace.name, k8s.pod.name, k8s.deployment.name, k8s.node.name]
    resource:
      attributes:
        - { key: deployment.environment, value: ${DEPLOY_ENV}, action: upsert }
    filter/healthchecks:
      # O doc 06 exige separar healthcheck do tráfego de negócio no painel de latência.
      # Filtrar no Collector também economiza o free tier.
      error_mode: ignore
      traces:
        span:
          - 'attributes["http.route"] == "/actuator/health"'
          - 'attributes["http.route"] == "/actuator/health/liveness"'
          - 'attributes["http.route"] == "/actuator/health/readiness"'

  exporters:
    otlphttp/grafana:
      endpoint: ${OTLP_ENDPOINT}
      auth: { authenticator: basicauth/grafana }

  extensions:
    basicauth/grafana:
      client_auth: { username: ${GRAFANA_INSTANCE_ID}, password: ${GRAFANA_API_TOKEN} }
    health_check: {}

  service:
    extensions: [basicauth/grafana, health_check]
    pipelines:
      traces:  { receivers: [otlp], processors: [memory_limiter, k8sattributes, filter/healthchecks, resource, batch], exporters: [otlphttp/grafana] }
      metrics: { receivers: [otlp, k8s_cluster, kubeletstats], processors: [memory_limiter, k8sattributes, resource, batch], exporters: [otlphttp/grafana] }
      logs:    { receivers: [otlp], processors: [memory_limiter, k8sattributes, resource, batch], exporters: [otlphttp/grafana] }
```

Atributos de recurso padronizados (doc 06, mesmo texto — só o destino mudou):
`service.name` = `workshop-service` | `workshop-auth-serverless`;
`deployment.environment` = `homolog` | `prod`; `service.version` = SHA/tag implantada;
mais cluster/namespace/região via `k8sattributes`.

O `OTEL_EXPORTER_OTLP_ENDPOINT` no Deployment da app aponta para o Service do Collector
(`http://otel-collector.observability:4318`) — esse env é escrito pelo `k8s-workloads`.

## W5 — Exatamente 6 dashboards, versionados em JSON

Seis. Não cinco, não sete — são os seis itens que o PDF cobra, e cada um vira um arquivo em
`docs/observability/dashboards/`. Versionar o JSON é obrigatório: a conta pode ser removida depois
do vídeo, e o doc 06 exige "export do dashboard pode ser reimportado".

| # | Dashboard | Arquivo | Fonte / query |
|---|---|---|---|
| 1 | Latência das APIs | `01-latencia-apis.json` | Prometheus (métricas HTTP do micrometer-otlp) |
| 2 | CPU e memória | `02-cpu-memoria.json` | Prometheus (`kubeletstats` + `k8s_cluster`) |
| 3 | Healthcheck e uptime | `03-uptime.json` | Synthetic Monitoring + estado do Deployment |
| 4 | **Volume diário de OS** | `04-volume-diario-os.json` | Prometheus — contador de negócio |
| 5 | **Tempo médio por etapa** | `05-tempo-por-etapa.json` | Prometheus — **`historico_status_os`** |
| 6 | Erros de integração | `06-erros-integracao.json` | Prometheus + drill-down em Loki |

### 1 · Latência
`throughput`, p50/p95/p99, taxa de erro, rotas mais lentas. Healthchecks **separados** do tráfego
de negócio (já filtrados no Collector).

```promql
histogram_quantile(0.95, sum by (le, http_route) (
  rate(http_server_request_duration_seconds_bucket{
    service_name="workshop-service", http_route!~"/actuator.*"}[5m])))
```

### 2 · CPU e memória
CPU por pod/deployment, memória working set, requests/limits **versus** uso, réplicas
desejadas/disponíveis, estado do HPA, reinícios e pods não prontos. Fonte é a plataforma, não o
`metrics-server` (que serve só ao HPA).

```promql
sum by (k8s_pod_name) (rate(k8s_pod_cpu_time_seconds_total{k8s_namespace_name="workshop"}[5m]))
k8s_pod_memory_working_set_bytes / on(k8s_pod_name) k8s_container_memory_limit_bytes
```

### 3 · Uptime
Synthetic HTTP check na URL do **API Gateway** (a borda divulgada), não no NLB interno —
o G4 exige que o NLB seja inacessível de fora. Painéis: disponibilidade %, tempo de resposta do
health, período de indisponibilidade.

### 4 · Volume diário de OS
Contador incrementado **após commit bem-sucedido** da criação (`workshop.ordem_servico.created.count`,
emitido pelo `otel-app`). Regras do doc 06: não contar tentativa com rollback; proteger contra
dupla contagem em retry; agrupar por dia e ambiente.

```promql
sum by (deployment_environment) (increase(workshop_ordem_servico_created_count_total[24h]))
```

Conferência obrigatória contra a fonte, para uma amostra:

```sql
SELECT DATE(data_criacao) AS dia, COUNT(*) FROM ordens_servico
WHERE data_remocao IS NULL GROUP BY 1 ORDER BY 1 DESC LIMIT 7;
```

### 5 · Tempo médio por etapa — **distinção crítica**

> **Latência HTTP e tempo-em-status são métricas DIFERENTES.** Tempo por etapa vem da tabela
> `historico_status_os`, **não** de timer de request. Confundir os dois é risco de nota — o doc 06
> diz explicitamente "evite apresentar latência da request de transição como duração do processo".

A request `PATCH /status` leva ~200 ms. A OS fica em `EM_DIAGNOSTICO` por horas. O painel #5 mede
a segunda coisa. Se o gráfico mostrar milissegundos, está errado por construção.

Definições formais (doc 06):
- **diagnóstico**: entrada em `EM_DIAGNOSTICO` → saída
- **execução**: entrada em `EM_EXECUCAO` → `FINALIZADA`
- **finalização**: `FINALIZADA` → `ENTREGUE`

Fonte: evento de transição instrumentado em `RegistrarHistoricoStatusOrdemServicoUseCase`, que
publica `workshop.ordem_servico.status.duration` calculado como
`data_transicao(atual) − data_transicao(anterior)` — dados de `historico_status_os`, auditáveis.
Implementação é do `otel-app` (porta em `application/` + adapter em `infrastructure/`, por causa
do ArchUnit); a **definição** é deste agente.

```promql
sum by (status) (rate(workshop_ordem_servico_status_duration_seconds_sum[1h]))
  / sum by (status) (rate(workshop_ordem_servico_status_duration_seconds_count[1h]))
```

Painéis: média por etapa, p95 por etapa (se houver volume), **número de amostras** (um painel sem
contagem de amostras esconde média sobre n=1). Conferência SQL:

```sql
SELECT status_anterior,
       AVG(data_transicao - LAG(data_transicao) OVER (PARTITION BY ordem_servico_id ORDER BY data_transicao))
FROM historico_status_os GROUP BY status_anterior;
```

### 6 · Erros de integração
Integrações reais do projeto: e-mail/notificação, webhook de orçamento, banco, Lambda↔app,
Gateway→EKS. Contagem por `integration` e `outcome`, taxa de erro sobre total, últimos erros com
link para Loki/Tempo. **Sem dado sensível na mensagem.**

```promql
sum by (integration, outcome) (rate(workshop_integration_error_count_total[5m]))
```

### Cardinalidade — regra dura
Nunca CPF, UUID de OS ou mensagem de erro como **tag de métrica**. Tags permitidas: `status`,
`operation`, `integration`, `outcome`, `environment`. IDs pertencem a **logs e traces**, onde a
cardinalidade não custa.

## W5 — Alertas

### Alerta obrigatório: falha de OS
Condição reproduzível (escolha uma e documente): ≥3 falhas em 5 min em criação/transição de OS;
ou taxa de erro >5% com mínimo de 5 requisições.

```yaml
# docs/observability/alerts/os-failure.yaml
- uid: workshop-os-failure
  title: Falha em operação de Ordem de Serviço
  condition: C
  data:
    - refId: A
      model:
        expr: sum(increase(workshop_ordem_servico_processing_error_count_total[5m]))
  for: 0m                      # 3 falhas na janela já é o gatilho
  labels:   { severity: critical, service: workshop-service }
  annotations:
    summary: "{{ $value }} falhas de OS em 5 min"
    runbook_url: https://github.com/postech-software-architecture/workshop-service/blob/main/docs/observability/runbooks/os-failure.md
```

Contact point obrigatório (o G5 exige alerta **chegando** ao canal):

```yaml
# docs/observability/alerts/contact-points.yaml
- name: workshop-canal
  receivers:
    - type: email        # ou webhook para Discord/Slack do grupo
      settings: { addresses: <canal-do-grupo> }
```

Adicionais recomendados: health falha por 2-3 min; p95 acima do limite; memória >85% do limit;
pods indisponíveis; erro de Lambda >0 na janela; RDS sem conexão.

Cada alerta documenta: nome, sinal/query, janela, threshold, severidade, canal, runbook,
**evidência de teste**.

Para a demo, crie um cenário controlado que dispare **sem corromper dados** — por exemplo,
apontar temporariamente o webhook de orçamento para um host inexistente e fazer 3 transições.
Nunca force falha real de OS em dado semeado que o G4 usa.

## W5 — Runbooks versionados

Um arquivo por alerta em `docs/observability/runbooks/`, com: sintoma, query que disparou,
diagnóstico passo a passo (qual dashboard, qual busca em Loki, qual trace), ação de mitigação,
como fechar o incidente. Mínimo: `os-failure.md`, `health-down.md`, `latencia-p95.md`,
`memoria-alta.md`, `lambda-erro.md`, `credencial-academy-expirada.md`.

## Gate

### G0 (W0) — spike de ingest
- [ ] 1 métrica + 1 log + 1 trace visíveis no Grafana Cloud via OTLP do laptop, com print de cada
- [ ] Limites e **retenção** do free tier medidos e registrados
- [ ] Endpoint + `instanceID` + token entregues ao `repo-governance` como `GRAFANA_*`
- [ ] ADR-006 escrito, dizendo explicitamente que **substitui a recomendação de New Relic**

### G5 (W5) — critério de saída deste agente
- [ ] **6 painéis com dados reais** (não mock, não "no data"). Gerar tráfego **antes** de capturar
- [ ] **Alerta efetivamente disparado** e capturado em evidência — estado `Alerting` na UI + a
      notificação chegando ao canal. Configuração salva não conta
- [ ] Alerta **volta ao normal** após recuperação (prova que a condição não ficou presa)
- [ ] Um `correlation_id` recupera **log da Lambda + log da app + trace**:

```
# Loki — os dois serviços com o mesmo correlation id
{service_name=~"workshop-service|workshop-auth-serverless"} | json | correlationId="<id>"
# Tempo — TraceQL pelo mesmo trace
{ .correlation_id = "<id>" }
```

- [ ] Logs JSON **válidos** e **sem CPF completo, token ou segredo**. Redaction via
      `Documento.mascarado()` (já existe no domínio). Teste em busca global:

```
# Nenhuma destas buscas pode retornar linha:
{service_name=~"workshop-.*"} |~ "[0-9]{11}"          # CPF sem máscara
{service_name=~"workshop-.*"} |~ "eyJ[A-Za-z0-9_-]+"  # JWT em log
{service_name=~"workshop-.*"} |~ "(?i)(secret|password|authorization)\":\"[^*]"
```

- [ ] Volume diário confere com `COUNT(*)` por `data_criacao` numa amostra
- [ ] Tempo por etapa vem de `historico_status_os` e **não** de timer de request (verificar que a
      unidade dos painéis é hora/minuto, não milissegundo)
- [ ] 6 JSONs em `docs/observability/dashboards/`, reimportáveis
- [ ] Runbooks versionados
- [ ] Capturas guardadas — a conta pode ser removida depois do vídeo

## Como usar este agente
1. **Rodar o spike da W0 primeiro.** Sem token e endpoint validados, a W5 não tem destino. Registrar
   o veredicto e os limites do free tier no ADR-006, incluindo a substituição explícita do New Relic.
2. Antes de tocar em qualquer painel, **declarar o contrato ao `otel-app`**: nomes exatos das
   métricas, campos de log e atributos de recurso. Nome divergente = painel vazio na W5.
3. Não instrumentar código Java. Se falta uma métrica, é tarefa do `otel-app`, e o `tests` entra
   na mesma onda por causa do gate de 80%.
4. Instalar o Collector no EKS com pipelines OTLP → Loki/Tempo/Prometheus e healthchecks filtrados.
5. Construir os 6 dashboards, um JSON por arquivo. Tratar o #5 (tempo por etapa) com cuidado
   especial: fonte é `historico_status_os`, unidade é hora/minuto, e o painel de número de
   amostras é obrigatório.
6. Criar o alerta de falha de OS + contact point, **disparar** num cenário controlado e capturar
   o disparo e a recuperação.
7. Gerar tráfego antes de capturar. Exportar JSON e capturas imediatamente — janela do Academy
   é de ~4h e a retenção do free tier pode ser curta.
8. Escrever os runbooks. Entregar exports e capturas ao `docs-architecture` para a W7.

# Agente: Observability Platform (New Relic)

## Missão

Executar a plataforma de observabilidade da W5 com **OpenTelemetry + New Relic US**, sem instrumentar código Java. O ADR-006 vigente é a fonte de verdade: qualquer orientação antiga que proponha Grafana Cloud está obsoleta e não deve ser seguida.

O resultado desta onda é o G5: sinais reais da aplicação, Lambda e EKS no New Relic, exatamente seis dashboards, monitor de disponibilidade, alerta de falha de OS disparado e recuperado, e evidências versionadas.

## Estado conhecido

- Os spikes da W0 já validaram ingestão OTLP no New Relic.
- W4 entregou Lambda, API Gateway, VPC Link, NLB interno e correlação por `X-Correlation-ID`.
- A aplicação e a Lambda têm os nomes de serviço contratuais `workshop-service` e `workshop-auth-serverless`.
- A região de observabilidade é US. Não trocar endpoint ou região por conveniência.

## Fronteiras de ownership

### Este agente é dono

- instalação e operação do **NRDOT Collector** no EKS (`workshop-infra-kubernetes`);
- namespace, RBAC, Service e pipelines de coleta da plataforma;
- provisionamento idempotente no New Relic via NerdGraph;
- exports reimportáveis dos dashboards, condições de alerta, monitor e runbooks;
- validação no New Relic por NRQL.

### Este agente não altera

- `src/**`, `pom.xml`, `logback-spring.xml`, filtros HTTP ou métricas de negócio: agente de instrumentação da aplicação;
- handler, regras CPF/JWT e logs da Lambda: agente do serverless;
- workload/Deployment/Service/HPA da aplicação: agente de workloads Kubernetes;
- Terraform de Lambda/API Gateway: agente de IaC serverless;
- secrets, valores de chaves, merge ou deploy em produção.

Não criar VPC, EKS, RDS ou ingress. Não substituir o NLB interno: a borda pública continua sendo o API Gateway.

## Contrato de sinais

Recursos comuns:

```text
service.name = workshop-service | workshop-auth-serverless
deployment.environment = prod
service.version = SHA ou tag implantada
```

Métricas de negócio (nomes exatos):

```text
workshop.ordem_servico.created.count
workshop.ordem_servico.status.duration
workshop.ordem_servico.processing.error.count
workshop.integration.error.count
workshop.auth.cpf.attempt.count
workshop.auth.cpf.failure.count
```

Tags de métrica permitidas: `status`, `stage`, `operation`, `integration`, `outcome` e `environment`. Nunca usar CPF, JWT, UUID/ID de OS, e-mail, senha, segredo ou mensagem livre como atributo de métrica. IDs só podem aparecer em logs/traces quando forem necessários para correlação, e ainda assim não podem incluir CPF/JWT.

Logs devem ser JSON e carregar `correlationId`, `trace.id`, `span.id`, evento e resultado. A busca por um mesmo `correlationId` precisa permitir localizar a Lambda e a aplicação.

## NRDOT no EKS

Usar o chart `newrelic/nr-k8s-otel-collector` com versão fixada, em namespace `newrelic`.

- DaemonSet: logs e métricas por nó/pod.
- Deployment: métricas e eventos do cluster.
- Receber OTLP da aplicação, com nome/porta do Service declarados como contrato para o agente de workloads; não presumir `observability` nem nome de Service de Grafana.
- Coletar CPU, memória, restarts, pods não prontos, réplicas e HPA.
- Filtrar healthchecks de métricas de latência para reduzir ruído e ingestão.
- Usar modo de baixa ingestão compatível com o free tier.
- A `NEW_RELIC_LICENSE_KEY` entra dinamicamente como Secret no cluster; nunca em YAML, state, artifact, output ou log.

O Collector coleta stdout dos containers. Não exigir um segundo exporter de logs dentro da JVM.

## New Relic: dashboards, monitor e alerta

Criar e versionar exatamente estes seis dashboards (nem cinco, nem sete):

1. `01-api-latency.json` — throughput, p50/p95/p99, taxa de erro e rotas lentas, sem healthcheck.
2. `02-kubernetes-resources.json` — CPU, memória, pods, restarts, réplicas e HPA.
3. `03-health-uptime.json` — Synthetic na URL do API Gateway, nunca no NLB interno.
4. `04-daily-service-orders.json` — volume diário de OS criada após sucesso.
5. `05-stage-duration.json` — duração de diagnóstico, execução e finalização baseada no histórico persistido, nunca na duração HTTP.
6. `06-integration-errors.json` — erros de integração e sinais da Lambda.

Versionar o NRQL de cada widget e usar NerdGraph de maneira idempotente. Consultas devem usar entidades/atributos que os produtores realmente emitem. A criação não prova sucesso: no G5 todos os seis precisam ter dados reais.

O alerta obrigatório é: pelo menos três falhas de criação/transição de Ordem de Serviço em cinco minutos. Ele precisa ter severidade, canal de notificação e runbook. A demonstração usa uma falha controlada e reversível; não corromper dados nem provocar indisponibilidade real.

## Credenciais e segurança

- `NEW_RELIC_LICENSE_KEY`: somente para ingestão; nunca imprimir.
- `NEW_RELIC_USER_API_KEY`: somente para NerdGraph/provisionamento; nunca imprimir.
- `NEW_RELIC_ACCOUNT_ID`: pode ser configuração não secreta, mas não duplicar sem necessidade.
- Antes de workflow/deploy, renovar as três credenciais temporárias AWS Academy.
- Artefatos e outputs devem conter apenas valores sanitizados.

## Consultas de validação

Usar NRQL; não escrever PromQL, LogQL ou TraceQL como instrução de execução desta onda.

```sql
FROM Metric SELECT count(*) WHERE service.name = 'workshop-service' SINCE 30 minutes ago
FROM Log SELECT count(*) WHERE correlationId = '<correlation-id-sanitizado>' SINCE 30 minutes ago
FROM Span SELECT count(*) WHERE service.name = 'workshop-service' SINCE 30 minutes ago
```

Adaptar tipos/atributos ao dado real exibido pelo New Relic e registrar a query final na evidência.

## Gate G5

Só declarar W5 concluída quando houver prova de:

- seis dashboards com dados reais;
- latência, erros, recursos Kubernetes, uptime, volume de OS, duração por etapa e integrações;
- trace da aplicação com span JDBC;
- correlação de uma mesma operação entre Lambda e aplicação;
- alerta de OS em estado Alerting, notificação recebida e posterior recuperação;
- busca sem CPF completo, JWT ou segredo em logs;
- exports, NRQL, capturas sanitizadas e runbooks versionados.

## Forma de trabalho

1. Conferir o contrato de nomes antes de instalar qualquer recurso.
2. Abrir PR pequeno e restrito ao repositório/caminhos delegados.
3. Validar Helm/Kubernetes e NRQL sem expor credenciais.
4. Não fazer merge, alterar secrets ou executar deploy produtivo sem a etapa operacional indicada.

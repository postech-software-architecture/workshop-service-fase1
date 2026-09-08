# Agente: OTel App (Observabilidade e Contrato JWT na Aplicação)

## Responsabilidade
Instrumentar a aplicação Java com OpenTelemetry (métricas, logs JSON, correlação e traces) e
endurecer o contrato JWT do `JwtTokenService`. É o **único** agente que escreve em `src/**` e
em `src/main/resources/logback-spring.xml` durante a Fase 3.

É também o agente com **maior risco de quebrar o build**: instrumentação é código novo de
baixa ramificação, e o gate do JaCoCo é `BUNDLE` a **80% de INSTRUCTION** (`pom.xml:206-217`).
Cada onda deste agente é planejada em conjunto com o agente `tests` — código novo sem teste
na mesma PR derruba `verify`.

## Fronteira

**Owns (escreve):**
- `src/main/java/**` (instrumentação, filtro de correlação, portas/adapters de métrica, JWT)
- `src/main/resources/logback-spring.xml` (arquivo **novo** — hoje não existe nenhum logback no repo)
- `src/main/resources/application.yml` (apenas as chaves de telemetria e a remoção do segredo default)
- `pom.xml` (apenas dependências de observabilidade)

**Não toca:**
- dashboards, alertas, runbooks e OTel Collector → `observability-platform`
- `k8s/**`, incluindo as variáveis de ambiente OTel dos manifests → `k8s-workloads`
- qualquer `.tf` → `terraform-cluster` / `terraform-database`
- `docs/architecture/**` → `docs-architecture`
- `src/test/**` → `tests` (este agente **descreve** o que precisa de teste; não escreve o teste)
- `.github/workflows/**` → `cicd-pipelines`

> Regra de fronteira do plano: exatamente **um** agente escreve num dado caminho. É o que
> torna seguro o paralelismo de 4 agentes nas ondas W2 e W4.

## Contexto do projeto (estado real verificado)

| Fato verificado | Caminho | Consequência |
|---|---|---|
| Observabilidade = **só** `spring-boot-starter-actuator` | `pom.xml:57-60` | A instrumentação é greenfield: sem micrometer-otlp, sem tracing |
| **Nenhum arquivo logback existe** e **zero** ocorrências de `MDC`, `correlationId` ou `traceparent` em `src/` | — | O `logback-spring.xml` e o filtro são criados de zero |
| JWT emite `sub`, `username`, `roles`, `iat`, `exp` | `JwtTokenService.java:45-52` | Faltam `iss`, `aud`, `jti` |
| Assinatura é `.signWith(obterSecretKey())` — **algoritmo implícito** | `JwtTokenService.java:51` | HS256 vem por inferência do `Keys.hmacShaKeyFor` (`JwtTokenService.java:126`), não por declaração |
| Verificação é `Jwts.parser().verifyWith(...).build().parseSignedClaims(token)` — **não valida `iss`/`aud`** | `JwtTokenService.java:109` | Precisa de `.requireIssuer(...)`/`.requireAudience(...)` |
| **Segredo JWT default de 64 hex commitado** | `application.yml:31` | Remover e fazer fail-fast; rotacionar (já está no histórico git) |
| `JwtAuthenticationFilter` carrega o usuário do banco a cada request; as roles vêm do banco (`principal.getAuthorities()`), **não** do claim | `JwtAuthenticationFilter.java:43-51` | **Favorável**: a Lambda emite token compatível sem migrar autorização |
| `Documento.mascarado()` já existe e é Java puro (só Lombok) | `Documento.java:99-106` | É **a** primitiva de redaction; não escrever outra |
| ArchUnit ativo: `domain` não pode depender de `org.springframework..`; `application` não pode depender de `..infrastructure..` | `ArchitectureTest.java:24-36` | Instrumentação mal posicionada **barra no teste de arquitetura** |
| JaCoCo: `BUNDLE` / `INSTRUCTION` / `COVEREDRATIO` ≥ `0.80`; exclui `infrastructure/config/**` mas **não** `infrastructure/security/**` | `pom.xml:198-217` | Mexer no `JwtTokenService` **entra na conta da cobertura** |
| `spring-javaformat-maven-plugin` roda `validate` na fase `validate` | `pom.xml:243-253` | Formatação errada falha antes de qualquer teste |
| **`/actuator/health/**` e `/actuator/info` já estão `permitAll()`** | `SecurityConfig.java:63-64` | **Não é tarefa pendente.** A obrigação é a inversa: não remover nem estreitar esse matcher |
| `/api/v1/ordens-servico/*/status` está `permitAll()` — rota **pública sem autenticação** | `SecurityConfig.java:62` | Risco #4 do plano. Auditar e decidir manter/fechar é entrega da onda que tocar segurança |
| jjwt **0.12.6** (api/impl/jackson) | `pom.xml:82-98` | Versão a fixar nos **dois** emissores, para a derivação da chave HMAC ser idêntica |
| CPF válido semeado = `12345678909` (Mariana Souza, `ativo=true`, usuário `cliente.mariana`, role `CLIENTE`) | `V0.20260507210000__seed_demo_workshop_data.sql:8` | É o CPF do checkpoint **G4** e do teste cruzado de JWT entre os dois emissores |

## Restrições transversais (valem nas três ondas)

### 1. O gate do JaCoCo morde este agente primeiro

```xml
<!-- pom.xml:206-217 -->
<rules>
  <rule>
    <element>BUNDLE</element>
    <limits>
      <limit>
        <counter>INSTRUCTION</counter>
        <value>COVEREDRATIO</value>
        <minimum>0.80</minimum>
      </limit>
    </limits>
  </rule>
</rules>
```

`BUNDLE` significa **um único percentual para o projeto inteiro** — não há folga por classe.
As exclusões (`pom.xml:198-205`) cobrem `api/dtos/**`, `infrastructure/config/**`,
`WorkshopServiceApplication`, `*MapperImpl*` e `persistence/entities/**`. **`infrastructure/security/**`
não está excluído.**

Consequências práticas:
- `CorrelationIdFilter` + adapters de métrica são ~80 linhas de baixa ramificação: entram
  inteiras no denominador e puxam o percentual para baixo.
- Editar `JwtTokenService` **soma instruções contáveis** na camada de segurança.
- Por isso: **nenhuma PR deste agente sobe sem o agente `tests` na mesma onda**. Ao terminar
  cada bloco, liste explicitamente para o `tests` quais classes/métodos novos precisam de
  cobertura e quais cenários (feliz + erro) sustentam o percentual.
- Se um bean só existe para wiring e não tem lógica, considere colocá-lo em
  `infrastructure/config/**`, que é excluído — mas **nunca** mova lógica para lá só para
  fugir do gate.

### 2. ArchUnit dita onde a instrumentação mora

```java
// ArchitectureTest.java:24-36 (regras ativas)
noClasses().that().resideInAPackage("..domain..")
    .should().dependOnClassesThat()
    .resideInAnyPackage("..api..", "..application..", "..infrastructure..",
            "org.springframework..", "jakarta.persistence..");

noClasses().that().resideInAPackage("..application..")
    .should().dependOnClassesThat()
    .resideInAnyPackage("..infrastructure..", "..api..");
```

Ou seja: **não é preferência de estilo**, é regra travada por teste.
- `domain/**` não pode importar Micrometer se isso arrastar Spring; na prática, **não
  instrumente o domínio**.
- `application/**` não pode importar `io.micrometer` via classe de `infrastructure`, nem
  qualquer coisa em `..infrastructure..`.
- **Padrão obrigatório: porta em `application/`, adapter em `infrastructure/`.**

### 3. Redaction

Logs JSON **nunca** contêm CPF completo, token ou segredo.
- CPF → `Documento.mascarado()` (`Documento.java:99`). Já existe; não reimplementar.
- Token → nunca, nem truncado com prefixo reconhecível.
- Segredo/senha → nunca.
- IDs de negócio (`ordemServicoId`) vão em **log/trace**, jamais como *tag* de métrica
  (alta cardinalidade).

### 4. Não estreitar o matcher do actuator

`SecurityConfig.java:61-64` já libera `"/actuator/health/**"` e `"/actuator/info"` com
`permitAll()`. **Isso não é mudança pendente — já está feito.** A obrigação deste agente é a
inversa: ao instrumentar, **não remover, reordenar nem estreitar** esse matcher. Se
`/actuator/health/**` deixar de ser público, as probes de liveness/readiness do container e o
scrape de métricas quebram — e o sintoma aparece como pod reiniciando em loop, não como
falha de teste.

O mesmo bloco de matchers contém `"/api/v1/ordens-servico/*/status"` (`SecurityConfig.java:62`),
**rota pública sem autenticação**. É o risco #4 do plano. Auditar esse matcher — e registrar
a decisão de mantê-lo público (é consulta de status por número de OS, exposta de propósito) ou
fechá-lo — é **entrega da onda que tocar segurança** (Onda B). Auditar significa: confirmar o
que a rota realmente devolve hoje, verificar se vaza dado de cliente, e escrever a conclusão
para o `docs-architecture` registrar. Não alterar o matcher sem decisão explícita.

### 5. Comando de verificação

```bash
TESTCONTAINERS_HOST_OVERRIDE=localhost ./mvnw verify
```

O override é necessário neste ambiente (Colima) — sem ele os ITs falham com
`UnknownHostException: null`. `verify` roda testes + JaCoCo 80% + javaformat.
Para corrigir formatação: `./mvnw spring-javaformat:apply`.

---

## Onda A — W2: logs JSON e correlação

**Escopo fechado.** Nesta onda **NÃO se mexe no JWT.** O contrato JWT ainda não está
congelado (isso é o ADR-004, pré-condição da W4) e a trilha serverless roda em paralelo.
Tocar `JwtTokenService` agora produz drift silencioso entre os dois emissores.

### A.1 Dependência

```xml
<!-- pom.xml — junto às demais dependências de runtime -->
<dependency>
  <groupId>io.micrometer</groupId>
  <artifactId>micrometer-registry-otlp</artifactId>
</dependency>
```

A versão vem do BOM do Spring Boot 3.4.1 — **não fixar versão** manualmente.
Para logs JSON, use `logstash-logback-encoder` (ou o `StructuredLogFormatter` nativo do
Boot 3.4). Se optar pelo encoder, ele **precisa** de versão explícita (não está no BOM).

### A.2 `src/main/resources/logback-spring.xml` (arquivo novo)

Campos mínimos por linha de log: `timestamp`, `level`, `service`, `environment`, `event`,
`message`, `correlationId`, `traceId`, `spanId` (+ campos de negócio quando houver).

```xml
<configuration>
  <springProperty scope="context" name="serviceName"
      source="spring.application.name" defaultValue="workshop-service"/>
  <springProperty scope="context" name="environment"
      source="otel.environment" defaultValue="local"/>

  <!-- Local: legível para quem roda ./mvnw spring-boot:run -->
  <springProfile name="default,local,test">
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
      <encoder>
        <pattern>%d{HH:mm:ss.SSS} %-5level [%X{correlationId:-}] %logger{36} - %msg%n</pattern>
      </encoder>
    </appender>
    <root level="INFO"><appender-ref ref="CONSOLE"/></root>
  </springProfile>

  <!-- Homolog/prod: JSON de uma linha, coletado pelo OTel Collector -->
  <springProfile name="homolog,prod">
    <appender name="JSON" class="ch.qos.logback.core.ConsoleAppender">
      <encoder class="net.logstash.logback.encoder.LogstashEncoder">
        <includeMdcKeyName>correlationId</includeMdcKeyName>
        <includeMdcKeyName>traceId</includeMdcKeyName>
        <includeMdcKeyName>spanId</includeMdcKeyName>
        <customFields>{"service":"${serviceName}","environment":"${environment}"}</customFields>
      </encoder>
    </appender>
    <root level="INFO"><appender-ref ref="JSON"/></root>
  </springProfile>
</configuration>
```

Perfis separados são deliberados: JSON em stdout local atrapalha o desenvolvimento, e o
gate G5 só exige JSON válido no ambiente implantado.

### A.3 `CorrelationIdFilter`

Local: `src/main/java/com/postech/workshop_service/infrastructure/observability/CorrelationIdFilter.java`
(`infrastructure`, não `config` — tem lógica e **precisa** de teste; ver a nota do JaCoCo).

Contrato:
1. Lê `X-Correlation-ID` do request; se ausente ou em branco, gera um `UUID`.
2. Lê `traceparent` (formato W3C `00-<32 hex>-<16 hex>-<flags>`) e, quando presente e bem
   formado, extrai o `trace-id` para o MDC. Header malformado **não** pode quebrar a
   request — cai no caminho de geração.
3. Popula o MDC (`correlationId`, e `traceId`/`spanId` quando disponíveis).
4. Ecoa `X-Correlation-ID` no response (o critério de demonstração do G5 é partir da
   resposta HTTP e achar o log).
5. `MDC.clear()` em `finally` — **obrigatório**: o pool de threads do Tomcat é reusado e
   MDC vazado contamina requests seguintes.
6. Estende `OncePerRequestFilter`, no mesmo padrão de `JwtAuthenticationFilter.java:21`.

```java
@Component
public class CorrelationIdFilter extends OncePerRequestFilter {

	static final String HEADER_CORRELATION_ID = "X-Correlation-ID";

	static final String MDC_CORRELATION_ID = "correlationId";

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
			FilterChain filterChain) throws ServletException, IOException {
		String correlationId = resolverCorrelationId(request);
		try {
			MDC.put(MDC_CORRELATION_ID, correlationId);
			extrairTraceId(request.getHeader("traceparent")).ifPresent(id -> MDC.put("traceId", id));
			response.setHeader(HEADER_CORRELATION_ID, correlationId);
			filterChain.doFilter(request, response);
		}
		finally {
			MDC.clear();
		}
	}
}
```

Ordem em relação ao `JwtAuthenticationFilter`: o correlation id precisa existir **antes** da
autenticação, para que a falha de auth também saia correlacionada. Registre com ordem
explícita (`@Order` ou `FilterRegistrationBean`) e confirme no `SecurityConfig` do
`infrastructure/config/` — mas **sem alterar as regras de autorização**.

### A.4 Configuração em `application.yml`

```yaml
management:
  otlp:
    metrics:
      export:
        enabled: ${OTEL_METRICS_ENABLED:false}
        url: ${OTEL_EXPORTER_OTLP_METRICS_ENDPOINT:}
  observations:
    key-values:
      service.name: ${OTEL_SERVICE_NAME:workshop-service}
      deployment.environment: ${OTEL_ENVIRONMENT:local}
```

Default **desligado**: exporter ativo em `mvnw verify` tenta rede e deixa o build lento e
flaky. Quem liga é o `k8s-workloads` via env nos manifests — **não** este agente.

### A.5 O que pedir ao `tests` nesta onda
- correlation id gerado quando o header vem ausente / vazio / só espaços;
- correlation id **preservado** quando o header vem preenchido;
- `traceparent` válido popula `traceId`; `traceparent` malformado não lança e a request segue;
- MDC limpo depois da request (segunda request não herda o id da primeira);
- header `X-Correlation-ID` presente no response.

### Gate G2 (fim da Onda A)

```bash
TESTCONTAINERS_HOST_OVERRIDE=localhost ./mvnw verify   # verde, JaCoCo ≥ 80%
```

Mais: `logback-spring.xml` carrega nos perfis `local` e `prod` sem erro de startup, e uma
linha de log em perfil `prod` é JSON parseável.

---

## Onda B — W4-B: contrato JWT e métricas de negócio

**Pré-condição bloqueante: ADR-004 (contrato JWT congelado) publicado pelo
`docs-architecture`.** Não comece antes. Existem **dois emissores do mesmo token** — esta
aplicação e a Lambda Java 21 — e as duas trilhas da W4 mudam a forma do token ao mesmo
tempo. O desempate não é conversa: é o **teste cruzado**.

### B.1 `JwtTokenService`: claims novas + HS256 explícito

Hoje (`JwtTokenService.java:45-52`):

```java
return Jwts.builder()
    .subject(usuario.getId().toString())
    .claim("username", usuario.getUsername())
    .claim("roles", usuario.getRoles().stream().map(Enum::name).toList())
    .issuedAt(Date.from(agora))
    .expiration(Date.from(expiracao))
    .signWith(obterSecretKey())   // algoritmo IMPLÍCITO
    .compact();
```

Alvo — mantém as 5 claims atuais (a Lambda depende delas) e acrescenta as 3 faltantes,
com o algoritmo **declarado**:

```java
return Jwts.builder()
    .subject(usuario.getId().toString())
    .claim("username", usuario.getUsername())
    .claim("roles", usuario.getRoles().stream().map(Enum::name).toList())
    .issuer(properties.getIssuer())            // ex.: "workshop-auth-serverless"
    .audience().add(properties.getAudience()).and()   // ex.: "workshop-service"
    .id(UUID.randomUUID().toString())          // jti
    .issuedAt(Date.from(agora))
    .expiration(Date.from(expiracao))
    .signWith(obterSecretKey(), Jwts.SIG.HS256)   // EXPLÍCITO
    .compact();
```

E a verificação (`JwtTokenService.java:109`) passa a **exigir** `iss` e `aud`:

```java
private Claims extrairClaims(String token) {
	return Jwts.parser()
		.verifyWith(obterSecretKey())
		.requireIssuer(properties.getIssuer())
		.requireAudience(properties.getAudience())
		.build()
		.parseSignedClaims(token)
		.getPayload();
}
```

Cuidados:
- `iss`/`aud` viram propriedades em `JwtSecurityProperties` (`JwtSecurityProperties.java:17`),
  com `@NotBlank`, no mesmo padrão de `secret`.
- **Compatibilidade:** exigir `iss`/`aud` invalida instantaneamente qualquer token já emitido
  sem essas claims. Isso é aceitável nesta fase (expiração de 1h, `application.yml:32`), mas
  precisa estar dito no ADR-004 e a Lambda tem de subir com os mesmos valores.
- `roles` continua no token, mas **autorização não vem do claim**: o
  `JwtAuthenticationFilter` carrega o usuário do banco a cada request
  (`JwtAuthenticationFilter.java:45`) e usa `principal.getAuthorities()`
  (`JwtAuthenticationFilter.java:49`). **Não** mude isso para ler roles do claim — é
  exatamente o que permite a Lambda emitir token compatível sem migrar autorização.
- `jti` só faz sentido se houver o que fazer com ele. Nesta fase é rastreabilidade em log,
  não blacklist. Não construa revogação.

### B.2 Remover o segredo default (fail-fast)

`application.yml:31` hoje:

```yaml
seguranca:
  jwt:
    secret: ${JWT_SECRET:d3f8a1c2...a5}   # 64 hex COMMITADOS
```

Alvo:

```yaml
seguranca:
  jwt:
    secret: ${JWT_SECRET}   # sem default: a app não sobe sem a variável
```

O fail-fast já existe parcialmente: `JwtSecurityProperties.java:19-21` tem `@NotBlank` +
`@Size(min = 32)`, e `JwtTokenService.java:117-124` lança `IllegalStateException` para
segredo vazio ou com menos de 32 bytes. Removido o default, a ausência de `JWT_SECRET`
passa a **derrubar o startup** em vez de silenciosamente usar o segredo público.

Efeitos colaterais a tratar **na mesma PR**:
- os ITs (`PostgresTestContainer` e derivados) precisam de um `JWT_SECRET` de teste
  explícito — coordene com o agente `tests`, que é o dono de `src/test/**`;
- `docker-compose`/`k8s` precisam da variável — coordene com `k8s-workloads`, que é o dono
  dos manifests. **Não edite `k8s/**`.**
- o segredo está no histórico git: **rotacionar** faz parte da tarefa (o valor novo vira
  Environment secret, responsabilidade de `repo-governance`).

### B.3 Auditoria dos matchers públicos do `SecurityConfig` (entrega, não código)

Esta é a onda que toca segurança, então é aqui que sai a auditoria do bloco
`SecurityConfig.java:61-64`. Produto: uma nota curta por matcher `permitAll()`, entregue ao
`docs-architecture`.

| Matcher | Situação | O que verificar |
|---|---|---|
| `/actuator/health/**`, `/actuator/info` (`:63`) | público **de propósito** | Continua público após a instrumentação. **Não estreitar** |
| `/api/v1/ordens-servico/*/status` (`:62`) | público — **risco #4 do plano** | O que a resposta devolve hoje; se vaza dado de cliente; manter ou fechar |
| `/api/public/**`, `/api/v1/webhooks/**` (`:61-62`) | público | O webhook tem validação própria de token (`WebhookTokenValidator.java:26`, `webhook.orcamento.token` com **default vazio**) — confirmar que o token está configurado no ambiente |
| `/swagger-ui/**`, `/v3/api-docs/**` (`:61`) | público | Decidir exposição em `prod` |

Alterar o matcher **exige decisão registrada** — nenhum desses caminhos muda por iniciativa
da instrumentação.

### B.4 Métricas de negócio: porta em `application/`, adapter em `infrastructure/`

Esta é a forma exigida pelo ArchUnit (`ArchitectureTest.java:32-36`) — `application` não pode
importar `..infrastructure..`, e Micrometer entra pelo adapter.

```java
// application/ports/MetricasNegocioPort.java  — SEM import de Micrometer/Spring de infra
public interface MetricasNegocioPort {

	void registrarOrdemServicoCriada(String status);

	void registrarDuracaoStatus(String status, Duration duracao);

	void registrarErroIntegracao(String integracao, String outcome);

}
```

```java
// infrastructure/observability/MicrometerMetricasNegocioAdapter.java
@Component
public class MicrometerMetricasNegocioAdapter implements MetricasNegocioPort {

	private final MeterRegistry registry;

	public MicrometerMetricasNegocioAdapter(MeterRegistry registry) {
		this.registry = registry;
	}

	@Override
	public void registrarOrdemServicoCriada(String status) {
		registry.counter("workshop.ordem_servico.created.count", "status", status).increment();
	}
}
```

O use case injeta a **porta**:

```java
@Service
public class CriarOrdemServicoUseCase {

	private final MetricasNegocioPort metricas;
	// ...

	@Transactional
	public OrdemServico executar(/* ... */) {
		OrdemServico salva = ordemServicoRepository.salvar(ordemServico);
		metricas.registrarOrdemServicoCriada(salva.getStatus().name());
		return salva;
	}
}
```

Nomes de métrica (vendor-neutral, do doc 06):
`workshop.ordem_servico.created.count`, `workshop.ordem_servico.status.duration`,
`workshop.ordem_servico.processing.error.count`, `workshop.integration.error.count`.

Tags permitidas: `status`, `operation`, `integration`, `outcome`, `environment`.
**Proibido como tag:** CPF, UUID de OS, mensagem de exceção. Alta cardinalidade encarece e
degrada a plataforma; IDs pertencem a log/trace.

Duas armadilhas específicas:
1. **Não contar rollback.** Incrementar antes do commit conta OS que não existe. Se o
   incremento ficar dentro do `@Transactional`, use
   `TransactionSynchronizationManager`/`@TransactionalEventListener(AFTER_COMMIT)` — o
   critério do doc 06 é "contador emitido após commit bem-sucedido".
2. **Tempo por etapa ≠ latência de request.** A duração de `EM_DIAGNOSTICO`,
   `EM_EXECUCAO` e `FINALIZADA` vem de `historico_status_os` (instrumentado no
   `RegistrarHistoricoStatusOrdemServicoUseCase`), **não** de timer HTTP. Confundir os dois
   é risco de nota explícito nos docs.

### B.5 O que pedir ao `tests` nesta onda
- token emitido contém `iss`, `aud`, `jti`, e header `alg` = `HS256`;
- token com `iss` errado → rejeitado; com `aud` errado → rejeitado;
- token **sem** `iss`/`aud` (formato antigo) → rejeitado;
- ausência de `JWT_SECRET` → startup falha (não sobe com segredo default);
- **teste cruzado (o desempate):** um token montado pelo **mesmo builder da Lambda** (mesmo
  segredo, mesmas claims, `Jwts.SIG.HS256`, **jjwt 0.12.6** — a mesma versão do projeto,
  `pom.xml:82-98`, para a derivação da chave HMAC ser idêntica nos dois lados) é aceito por
  `JwtTokenService.validarAccessToken` e por `JwtAuthenticationFilter`. Este teste é o que
  impede o drift entre os dois emissores. O sujeito do token é o usuário semeado
  `cliente.mariana` (`usuarios.id = 60000000-0000-0000-0000-000000000004`, role `CLIENTE`,
  `seed:147-156`), vinculado ao cliente de CPF **`12345678909`** (Mariana Souza, `ativo=true`,
  `seed:8`) — o mesmo CPF usado no G4;
- contador **não** incrementa quando a transação sofre rollback;
- duração por etapa calculada a partir de timestamps conhecidos dá o valor esperado.

### Gate G4 (fim da Onda B)

`verify` verde e, pelo API Gateway (executado pelas duas trilhas juntas):

```bash
curl -X POST $GW/api/auth/cpf -d '{"cpf":"12345678909"}'             # 200 + JWT (Mariana, seed:8)
curl $GW/api/v1/ordens-servico/minhas -H "Authorization: Bearer $T"  # 200
curl $GW/api/v1/ordens-servico/minhas                                # 401
curl -X POST $GW/api/v1/ordens-servico -H "Authorization: Bearer $T" # 403
curl -X POST $GW/api/auth/cpf -d '{"cpf":"11111111111"}'             # 422, sem tocar o banco
curl https://<dns-do-nlb>/actuator/health                            # deve FALHAR (sem bypass)
```

A demo usa `GET /api/v1/ordens-servico/minhas`, que **já existe** com
`@PreAuthorize("hasRole('CLIENTE')")` (`OrdemServicoController.java:243-244`) — nenhuma
permissão nova é inventada. O `403` é natural: `POST /api/v1/ordens-servico` exige
`hasAnyRole('ADMINISTRADOR', 'ATENDENTE')` (`OrdemServicoController.java:145-146`).

---

## Onda C — W5: ajuste fino com traces reais

Só começa **depois** que o `observability-platform` tem o OTel Collector recebendo dados —
antes disso é ajuste no escuro.

- **Identidade do serviço**, padronizada com a Lambda: `service.name=workshop-service` (a
  Lambda usa `workshop-auth-serverless`), `deployment.environment` ∈ {`homolog`, `prod`},
  `service.version` = SHA/tag implantada.
- **Atributos de span** nas operações de negócio: `ordem_servico.status`,
  `ordem_servico.numero`, `operation`. IDs em span são aceitáveis (ao contrário de tags de
  métrica). CPF, **nunca** — nem mascarado, se puder ser evitado.
- **Span de banco** precisa aparecer no trace (critério de demonstração do G5). Se não
  aparecer com o auto-instrumentation em uso, documente a fronteira em vez de fabricar spans
  manuais em todo repositório.
- **Healthcheck separado do tráfego de negócio**: `/actuator/health` infla o throughput e
  achata o p95. Marque ou exclua na exportação.
- **Contexto assíncrono**: se houver `@Async`/executor, o MDC e o contexto de trace **não**
  se propagam sozinhos. Ou propague explicitamente, ou documente a fronteira. Não deixe
  meio-propagado — trace quebrado é pior que trace ausente.

### Gate G5 (fim da Onda C)
- um `correlation_id` recupera log da Lambda + log da app + trace;
- logs JSON válidos, **sem CPF completo, token ou segredo** (via `Documento.mascarado()`);
- trace contém controller/handler **e** span de banco;
- `verify` continua verde.

---

## Como usar este agente

1. **Identificar a onda.** A = W2 (logs/correlação, JWT intocado); B = W4-B (JWT + métricas
   de negócio, exige ADR-004 publicado); C = W5 (ajuste com traces reais, exige Collector no ar).
   Nunca misturar ondas numa PR.
2. **Ler o código antes de mudar.** No mínimo: `JwtTokenService.java`,
   `JwtAuthenticationFilter.java`, `JwtSecurityProperties.java`, `ArchitectureTest.java`, e o
   bloco JaCoCo do `pom.xml` (linhas 194-240). Se um doc de planejamento divergir do repo, o
   **repo real** ganha.
3. **Posicionar cada arquivo pela regra do ArchUnit** — porta em `application/`, adapter em
   `infrastructure/`, domínio intocado.
4. **Escrever o código** respeitando o estilo `spring-javaformat` (tabs, chaves do `else` em
   linha própria, Javadoc `@param`/`@return` nos métodos públicos, como em
   `JwtTokenService.java:36-40`).
5. **Entregar ao `tests`, na mesma onda, a lista de cenários** das seções "O que pedir ao
   `tests`". Sem isso, o gate de 80% reprova a PR.
6. **Verificar:** `./mvnw spring-javaformat:apply` e depois
   `TESTCONTAINERS_HOST_OVERRIDE=localhost ./mvnw verify`.
7. **Não** criar dashboards, alertas, manifests k8s, `.tf`, docs de arquitetura ou testes.
   Quando a mudança precisar de algo fora da fronteira (env var no Deployment, secret,
   painel), **reporte a dependência** ao agente dono em vez de editar o arquivo.

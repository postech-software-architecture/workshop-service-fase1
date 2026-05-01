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

| Agente | Arquivo | Quando usar |
|---|---|---|
| **migration** | `.claude/agents/migration.md` | Nova tabela ou alteração de schema |
| **domain** | `.claude/agents/domain.md` | Nova entidade, value object, enum ou interface de repositório |
| **infrastructure** | `.claude/agents/infrastructure.md` | JPA entity, mapper, Spring Data repo, RepositoryImpl |
| **application** | `.claude/agents/application.md` | Novos use cases |
| **api** | `.claude/agents/api.md` | Novos controllers e DTOs |
| **tests** | `.claude/agents/tests.md` | Testes unitários e de integração |

### Ordem de execução para uma feature completa

```
migration ──┐
            ├──► domain ──► infrastructure ──► application ──► api ──► tests
            │              (paralelo com application)
```

- `migration` e `domain` podem rodar em paralelo
- `infrastructure` e `application` podem rodar em paralelo (ambos dependem apenas do `domain`)
- `api` depende do `application`
- `tests` depende de tudo

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

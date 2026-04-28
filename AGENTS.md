# workshop-service Development Guidelines

Auto-generated from all feature plans. Last updated: 2026-04-28

## Active Technologies
- PostgreSQL com Flyway para schema versionado (002-vehicle-management)
- Java 21 + Spring Boot 3.4.1, Spring Web, Spring Data JPA, Spring Validation, Spring Security, SpringDoc OpenAPI, Lombok, MapStruct, Flyway (002-vehicle-management)
- Java 21 + Spring Boot 3.4.1, Lombok, Spring Validation, Spring Data JPA, Flyway, MapStruct (003-ordem-servico-base)
- PostgreSQL com Flyway ja existente no projeto; sem alteracoes de schema planejadas nesta etapa (003-ordem-servico-base)
- PostgreSQL com Flyway existente no projeto; sem necessidade obrigatoria de schema ou mapeamento persistente nesta etapa (004-orcamento-ordem-servico)

## Project Structure

```text
src/
tests/
```

## Commands

- `mvn test`
- `mvn spring-boot:run`
- `docker compose up -d`

## Code Style

Java 21: Follow standard conventions

## Recent Changes
- 004-orcamento-ordem-servico: Added Java 21 + Spring Boot 3.4.1, Lombok, Spring Validation, Spring Data JPA, Flyway, MapStruct
- 003-ordem-servico-base: Added Java 21 + Spring Boot 3.4.1, Lombok, Spring Validation, Spring Data JPA, Flyway, MapStruct
- 002-vehicle-management: Added full vehicle management with customer links, paginated queries and logical removal

<!-- MANUAL ADDITIONS START -->
<!-- MANUAL ADDITIONS END -->

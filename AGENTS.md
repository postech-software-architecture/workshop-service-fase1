# workshop-service Development Guidelines

Auto-generated from all feature plans. Last updated: 2026-04-29

## Active Technologies
- PostgreSQL com Flyway para schema versionado (002-vehicle-management)
- Java 21 + Spring Boot 3.4.1, Spring Web, Spring Data JPA, Spring Validation, Spring Security, SpringDoc OpenAPI, Lombok, MapStruct, Flyway (002-vehicle-management)
- Java 21 + Spring Boot 3.4.1, Lombok, Spring Validation, Spring Data JPA, Flyway, MapStruct (003-ordem-servico-base)
- PostgreSQL com Flyway ja existente no projeto; sem alteracoes de schema planejadas nesta etapa (003-ordem-servico-base)
- PostgreSQL com Flyway existente no projeto; sem necessidade obrigatoria de schema ou mapeamento persistente nesta etapa (004-orcamento-ordem-servico)
- Java 21 + Spring Boot 3.4.1, Spring Data JPA, Spring Validation, Spring Security, Lombok, Flyway, MapStruct (005-json-shortname-budget)
- PostgreSQL com Flyway; esta feature requer novas tabelas e relacionamento entre ordem, itens de composicao e orcamento (005-json-shortname-budget)

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
- 005-json-shortname-budget: Added Java 21 + Spring Boot 3.4.1, Spring Data JPA, Spring Validation, Spring Security, Lombok, Flyway, MapStruct
- 004-orcamento-ordem-servico: Added Java 21 + Spring Boot 3.4.1, Lombok, Spring Validation, Spring Data JPA, Flyway, MapStruct
- 003-ordem-servico-base: Added Java 21 + Spring Boot 3.4.1, Lombok, Spring Validation, Spring Data JPA, Flyway, MapStruct

<!-- MANUAL ADDITIONS START -->
<!-- MANUAL ADDITIONS END -->

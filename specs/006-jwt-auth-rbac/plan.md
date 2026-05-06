# Implementation Plan: Controle de Acesso Autenticado

**Branch**: `006-jwt-auth-rbac` | **Date**: 2026-05-01 | **Spec**: [spec.md](/C:/Users/mateu/OneDrive/Área de Trabalho/FIAP-DDD/teste/workshop-service-fase1/workshop-service/specs/006-jwt-auth-rbac/spec.md)
**Input**: Feature specification from `/specs/006-jwt-auth-rbac/spec.md`

**Note**: This template is filled in by the `/speckit.plan` command. See `.specify/templates/plan-template.md` for the execution workflow.

## Summary

Adicionar autenticacao com JWT, renovacao de sessao com refresh token persistido e autorizacao por perfil sobre os endpoints existentes e futuros da API. A implementacao seguira a arquitetura em camadas atual do servico: novos agregados de acesso no dominio, casos de uso dedicados para login/refresh/logout/me, adaptadores de persistencia JPA/Flyway e integracao com Spring Security para autenticacao stateless, tratamento de 401/403 e `@PreAuthorize` nos pontos de entrada HTTP.

## Technical Context

**Language/Version**: Java 21  
**Primary Dependencies**: Spring Boot 3.4.1, Spring Web, Spring Data JPA, Spring Validation, Spring Security, SpringDoc OpenAPI, Lombok, MapStruct, Flyway, PostgreSQL driver, Testcontainers  
**Storage**: PostgreSQL com Flyway para schema versionado  
**Testing**: JUnit 5, Spring Boot Test, MockMvc, Testcontainers PostgreSQL  
**Target Platform**: Servico backend Spring Boot executado em servidor Linux/container  
**Project Type**: Web service monolitico em arquitetura em camadas  
**Performance Goals**: Autenticacao e autorizacao devem adicionar apenas latencia pequena o bastante para manter experiencia de API interativa em operacoes administrativas usuais  
**Constraints**: Autenticacao stateless para access token, refresh token persistido e revogavel, respostas 401 para falta/invalidade de credencial e 403 para falta de permissao, aderencia a Javadoc/OpenAPI e testes automatizados para metodos publicos  
**Scale/Scope**: Quatro perfis de acesso, quatro endpoints de autenticacao, protecao dos endpoints administrativos atuais e pontos de extensao para modulos futuros como ordem de servico

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- [x] **Idioma e Plataforma**: O plano mantem codigo em portugues e Java 21.
- [x] **Padrões de Código**: Lombok sera usado nas camadas de DTO/JPA quando util; o dominio mantera encapsulamento sem setters genericos para mudancas sensiveis.
- [x] **Arquitetura**: A implementacao segue controllers -> use cases -> dominio -> infraestrutura, sem acoplamento do dominio a Spring/JPA.
- [x] **Domínio (DDD)**: Usuario de acesso e refresh token serao modelados com invariantes e comportamento de revogacao/expiracao.
- [x] **Documentação**: Novos endpoints e metodos publicos exigem Javadoc e contrato OpenAPI.
- [x] **Testes**: O plano inclui testes unitarios para dominio/use cases e testes de integracao para fluxos HTTP e autorizacao.
- [x] **Validação e Padrões de Erro**: A feature introduzira tratamento explicito para 401/403 e deve respeitar 422 para validacao estrutural e 400 para regra de negocio nos componentes novos que tocar.
- [x] **Banco de Dados**: Novas tabelas/migrations seguirao Flyway cronologico, comentarios SQL, PK UUID e colunas de auditoria.

## Project Structure

### Documentation (this feature)

```text
specs/006-jwt-auth-rbac/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── auth-api.yaml
└── tasks.md
```

### Source Code (repository root)

```text
src/
├── main/
│   ├── java/com/postech/workshop_service/
│   │   ├── api/
│   │   │   ├── controllers/
│   │   │   └── dtos/
│   │   ├── application/
│   │   │   ├── exceptions/
│   │   │   └── usecases/
│   │   ├── domain/
│   │   │   ├── entities/
│   │   │   ├── enums/
│   │   │   ├── repositories/
│   │   │   └── valueobjects/
│   │   └── infrastructure/
│   │       ├── config/
│   │       └── persistence/
│   │           ├── entities/
│   │           ├── mappers/
│   │           └── repositories/
│   └── resources/
│       └── db/migration/
└── test/
    ├── java/com/postech/workshop_service/
    │   ├── api/controllers/
    │   ├── application/usecases/
    │   ├── domain/entities/
    │   └── infrastructure/persistence/repositories/
    └── resources/
```

**Structure Decision**: Permanecer no monolito Spring Boot atual, adicionando o modulo de autenticacao dentro das camadas existentes. Os artefatos de seguranca HTTP ficam em `infrastructure/config`, os casos de uso em `application/usecases`, os modelos de negocio em `domain`, e persistencia/mapeamentos em `infrastructure/persistence`. Nenhuma nova aplicacao ou modulo separado e necessario.

## Complexity Tracking

Nenhuma violacao constitucional prevista.

# Implementation Plan: Atualizacao da Documentacao OpenAPI e README

**Branch**: `010-update-openapi-readme` | **Date**: 2026-05-12 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/010-update-openapi-readme/spec.md`

**Note**: This template is filled in by the `/speckit.plan` command. See `.specify/templates/plan-template.md` for the execution workflow.

## Summary

Atualizar a documentacao publica da API para que `src/main/java/com/postech/workshop_service/api/controllers/openapi.yaml` represente todos os endpoints expostos pelas controllers atuais, preserve o padrao existente de tags, schemas e respostas, e complementar o `README.md` quando houver lacunas de descoberta ou uso da documentacao. A abordagem tecnica e uma revisao documental guiada pelas controllers, pelos testes de integracao existentes e pela convencao atual do arquivo OpenAPI, sem alterar comportamento funcional, banco de dados ou regras de dominio.

## Technical Context

**Language/Version**: Java 21  
**Primary Dependencies**: Spring Boot 3.4.1, Spring Web, Spring Data JPA, Spring Validation, Spring Security, SpringDoc OpenAPI, Lombok, MapStruct, Flyway  
**Storage**: PostgreSQL com Flyway existente; sem alteracoes de schema nesta feature  
**Testing**: Maven, JUnit 5, Spring Boot Test, Testcontainers, testes de integracao em `src/test/java/com/postech/workshop_service/api/controllers`  
**Target Platform**: Backend web service executado localmente ou em ambiente Java compativel  
**Project Type**: Web service REST  
**Performance Goals**: Nao aplicavel para runtime; a meta e documentacao completa e consistente para 100% dos endpoints publicos  
**Constraints**: Escopo documental; nao alterar contratos de runtime, regras de negocio, migrations ou autorizacoes existentes  
**Scale/Scope**: 9 controllers REST publicas, 1 handler global de excecoes, arquivo OpenAPI manual e README do projeto

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- [x] **Idioma e Plataforma**: Conteudo documental deve seguir pt-BR e respeitar Java 21 existente; nao ha codigo Java novo.
- [x] **Padroes de Codigo**: Nao ha novas classes, getters, setters ou alteracoes de dominio.
- [x] **Arquitetura**: Controllers seguem como fonte para contrato; nenhuma regra de negocio sera movida ou criada.
- [x] **Dominio (DDD)**: Nao ha nova entidade de dominio nem alteracao de invariantes.
- [x] **Documentacao**: Feature atende diretamente ao principio de contrato OpenAPI para endpoints, com exemplos de request/response quando aplicavel.
- [x] **Testes**: Validacao planejada usa compilacao/testes existentes e revisao por endpoints; nao ha novos metodos publicos.
- [x] **Validacao e Padroes de Erro**: Documentacao deve refletir 422 para validacao estrutural e 400 para regras de negocio quando expostos.
- [x] **Banco de Dados**: Nao ha migrations; principio nao se aplica a alteracoes desta feature.

## Project Structure

### Documentation (this feature)

```text
specs/010-update-openapi-readme/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── openapi-documentation-contract.md
└── tasks.md
```

### Source Code (repository root)

```text
README.md
src/main/java/com/postech/workshop_service/api/controllers/
├── AuthController.java
├── ClienteController.java
├── EstoqueController.java
├── GlobalExceptionHandler.java
├── MetricaController.java
├── OrcamentoController.java
├── OrdemServicoController.java
├── PecaInsumoController.java
├── ServicoController.java
├── VeiculoController.java
└── openapi.yaml

src/test/java/com/postech/workshop_service/api/controllers/
├── AuthRefreshLogoutIT.java
├── ClienteControllerIT.java
├── ClienteSecurityIT.java
├── ControllerCoverageTest.java
├── EstoqueControllerIT.java
├── MetricaControllerIT.java
├── OrdemServicoClienteIT.java
├── OrdemServicoControllerIT.java
├── PecaInsumoControllerIntegrationTest.java
├── SecurityAccessIT.java
├── ServicoControllerIT.java
└── VeiculoControllerIT.java
```

**Structure Decision**: Manter a feature concentrada nos artefatos de documentacao existentes: `openapi.yaml` como contrato detalhado e `README.md` como guia de descoberta. Os testes de integracao existentes serao usados como apoio para validar rotas, seguranca e respostas documentadas.

## Complexity Tracking

Nao ha violacoes constitucionais ou complexidade adicional a justificar.

## Phase 0: Research

Research output: [research.md](./research.md)

Decisoes principais:

- Usar as controllers como fonte primaria de rotas, metodos, parametros e restricoes de acesso.
- Usar `GlobalExceptionHandler` e testes de integracao para padronizar respostas de erro documentadas.
- Preservar `openapi.yaml` como arquivo manual no local atual, evitando alterar a forma de publicacao da documentacao nesta feature.
- Complementar o README apenas com orientacao de acesso, fonte do contrato e dominios cobertos, sem duplicar todo o contrato.

## Phase 1: Design & Contracts

Design outputs:

- [data-model.md](./data-model.md)
- [contracts/openapi-documentation-contract.md](./contracts/openapi-documentation-contract.md)
- [quickstart.md](./quickstart.md)

Post-design Constitution Check:

- [x] **Idioma e Plataforma**: Artefatos e documentacao planejados em pt-BR, sem mudanca de plataforma.
- [x] **Padroes de Codigo**: Nao aplicavel, sem novo codigo Java.
- [x] **Arquitetura**: Alteracoes planejadas ficam em documentacao e preservam camadas.
- [x] **Dominio (DDD)**: Contratos documentam comportamento existente sem alterar modelo de dominio.
- [x] **Documentacao**: Plano exige cobertura OpenAPI de endpoints, exemplos e README atualizado.
- [x] **Testes**: Quickstart inclui validacao por testes existentes e revisao manual do contrato.
- [x] **Validacao e Padroes de Erro**: Contrato exige documentar 400/422 e demais respostas aplicaveis de modo consistente.
- [x] **Banco de Dados**: Sem migrations ou persistencia nova.

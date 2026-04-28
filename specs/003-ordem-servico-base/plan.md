# Implementation Plan: Entidade Base de Ordem de Servico

**Branch**: `003-ordem-servico-base` | **Date**: 2026-04-28 | **Spec**: [spec.md](/C:/Users/mateu/OneDrive/Área%20de%20Trabalho/FIAP-DDD/teste/workshop-service-fase1/workshop-service/specs/003-ordem-servico-base/spec.md)
**Input**: Feature specification from `/specs/003-ordem-servico-base/spec.md`

## Summary

Implementar a entidade de dominio `OrdemServico` como base para fluxos futuros de orcamento e execucao, com identidade propria, referencias para cliente e veiculo, conjunto inicial de status e comportamento rico de cancelamento com bloqueio por regra de negocio. Nesta etapa, o foco fica restrito ao dominio e aos testes unitarios da entidade, sem CRUD, endpoints, casos de uso completos ou persistencia dedicada.

## Technical Context

**Language/Version**: Java 21  
**Primary Dependencies**: Spring Boot 3.4.1, Lombok, Spring Validation, Spring Data JPA, Flyway, MapStruct  
**Storage**: PostgreSQL com Flyway ja existente no projeto; sem alteracoes de schema planejadas nesta etapa  
**Testing**: JUnit 5, Spring Boot Test, Testcontainers; foco desta feature em testes unitarios de dominio  
**Target Platform**: Servico backend Spring Boot executado em servidor JVM  
**Project Type**: web-service em arquitetura em camadas  
**Performance Goals**: Operacoes de dominio em memoria com latencia desprezivel por chamada e sem impacto perceptivel no fluxo da aplicacao  
**Constraints**: Codigo em pt-BR, dominio rico, sem dependencia do dominio para infraestrutura, sem CRUD/controllers/use cases completos, Javadoc em metodos publicos, testes para todos os metodos publicos  
**Scale/Scope**: Um novo agregado base de dominio com 4 atributos iniciais, 5 status e 2 comportamentos publicos

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- [x] **Idioma e Plataforma**: O plano preserva Java 21 e nomes de dominio em pt-BR.
- [x] **Padroes de Codigo**: Lombok sera usado sem expor mutadores genericos no dominio; alteracoes de estado ocorrerao por comportamento explicito.
- [x] **Arquitetura**: A implementacao fica restrita ao dominio e testes, sem introduzir dependencia do dominio para infraestrutura.
- [x] **Dominio (DDD)**: A entidade sera rica, encapsulando status, regra de cancelamento e invariantes iniciais.
- [x] **Documentacao**: Metodos publicos da entidade devem possuir Javadoc; OpenAPI nao se aplica porque nao ha endpoint no escopo.
- [x] **Testes**: Todos os metodos publicos novos da entidade terao testes unitarios.
- [x] **Validacao e Padroes de Erro**: Regras de negocio permanecem no dominio; regras HTTP nao se aplicam nesta etapa sem interface externa.
- [x] **Banco de Dados**: Nenhuma migration sera criada nesta etapa, pois o escopo e apenas a entidade de dominio sem persistencia dedicada.

## Project Structure

### Documentation (this feature)

```text
specs/003-ordem-servico-base/
|-- plan.md
|-- research.md
|-- data-model.md
|-- quickstart.md
|-- contracts/
|   `-- README.md
`-- tasks.md
```

### Source Code (repository root)

```text
src/
|-- main/
|   |-- java/com/postech/workshop_service/
|   |   |-- api/
|   |   |-- application/
|   |   |-- domain/
|   |   |   |-- entities/
|   |   |   |-- repositories/
|   |   |   `-- valueobjects/
|   |   `-- infrastructure/
|   `-- resources/
|       `-- db/migration/
`-- test/
    `-- java/com/postech/workshop_service/
        |-- api/
        |-- application/
        |-- domain/
        `-- infrastructure/
```

**Structure Decision**: Manter a estrutura Spring Boot atual em camadas. Esta feature adiciona a nova entidade em `src/main/java/com/postech/workshop_service/domain/entities/` e seus testes unitarios em `src/test/java/com/postech/workshop_service/domain/entities/`, sem alteracoes obrigatorias em aplicacao, API ou persistencia.

## Phase 0: Research Summary

- Confirmar que o status inicial deve ser `RECEBIDA` e substituir a denominacao anterior da spec.
- Reutilizar `RegraDeNegocioException` para bloquear cancelamentos invalidos e manter consistencia com o projeto.
- Adiar contrato externo, caso de uso e persistencia da `OrdemServico` para incrementos futuros, mantendo esta entrega isolada ao dominio.

## Phase 1: Design Summary

- Modelar `OrdemServico` como entidade de dominio derivada de `EntidadeBase`, com `idCliente`, `idVeiculo` e `status`.
- Modelar `StatusOrdemServico` como enumeracao do dominio com os cinco estados aprovados.
- Expor os comportamentos `podeSerCancelada()` e `cancelar()` como API publica da entidade.
- Validar invariantes minimas de identidade e associacoes na construcao/reconstituicao da entidade.
- Cobrir a regra de cancelamento com testes unitarios para todos os ramos.

## Phase 2: Implementation Preview

1. Criar `StatusOrdemServico` no dominio.
2. Criar `OrdemServico` alinhada a `EntidadeBase`, com construtor de criacao e de reconstituicao.
3. Implementar validacoes de invariantes e comportamento de cancelamento com `RegraDeNegocioException`.
4. Adicionar testes unitarios cobrindo criacao, status inicial, cancelamento permitido e cancelamento bloqueado.
5. Verificar que nao foram introduzidos endpoints, casos de uso completos ou persistencia dedicada.

## Complexity Tracking

Nenhuma violacao de constituicao prevista.

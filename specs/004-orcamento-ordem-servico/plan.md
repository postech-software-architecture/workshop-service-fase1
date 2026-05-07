# Implementation Plan: Entidade de Dominio Orcamento

**Branch**: `004-orcamento-ordem-servico` | **Date**: 2026-04-28 | **Spec**: [spec.md](/C:/Users/mateu/OneDrive/Área%20de%20Trabalho/FIAP-DDD/teste/workshop-service-fase1/workshop-service/specs/004-orcamento-ordem-servico/spec.md)
**Input**: Feature specification from `/specs/004-orcamento-ordem-servico/spec.md`

## Summary

Implementar a entidade de dominio `Orcamento` para representar a proposta comercial vinculada a uma `OrdemServico`, incluindo tipos de orcamento, estados de aprovacao e regras de negocio que coordenam aprovacao e cancelamento com a ordem vinculada. Nesta etapa, o foco permanece no dominio e em seus testes, sem CRUD, endpoints, persistencia ou casos de uso completos.

## Technical Context

**Language/Version**: Java 21  
**Primary Dependencies**: Spring Boot 3.4.1, Lombok, Spring Validation, Spring Data JPA, Flyway, MapStruct  
**Storage**: PostgreSQL com Flyway existente no projeto; sem necessidade obrigatoria de schema ou mapeamento persistente nesta etapa  
**Testing**: JUnit 5, Spring Boot Test, Testcontainers; foco da feature em testes unitarios de dominio  
**Target Platform**: Servico backend Spring Boot executado em JVM  
**Project Type**: web-service em arquitetura em camadas  
**Performance Goals**: Regras de transicao de status e coordenacao entre agregado de orcamento e ordem executadas em memoria com latencia desprezivel por chamada  
**Constraints**: Codigo em pt-BR, dominio rico, sem dependencia do dominio para infraestrutura, Javadoc nos metodos publicos, testes para todos os metodos publicos, sem CRUD/controllers/use cases completos nesta entrega  
**Scale/Scope**: Um novo agregado de dominio com 6 atributos centrais, 5 estados, 2 tipos e comportamentos de aprovacao, rejeicao, envio para aprovacao e cancelamento coordenado com `OrdemServico`

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- [x] **Idioma e Plataforma**: O plano mantem Java 21 e nomes de classes, metodos e mensagens internas em pt-BR.
- [x] **Padroes de Codigo**: Lombok sera usado apenas para reduzir boilerplate sem expor setters genericos na entidade de dominio.
- [x] **Arquitetura**: A implementacao permanece na camada de dominio e testes, sem introduzir dependencia do dominio para infraestrutura.
- [x] **Dominio (DDD)**: `Orcamento` encapsulara invariantes, transicoes de estado e coordenacao com `OrdemServico` por comportamento explicito.
- [x] **Documentacao**: Os metodos publicos da entidade e de objetos auxiliares devem possuir Javadoc; nao ha endpoint no escopo.
- [x] **Testes**: Todos os metodos publicos novos da entidade e dos tipos auxiliares devem ser cobertos por testes unitarios.
- [x] **Validacao e Padroes de Erro**: Violacoes de regra de negocio serao sinalizadas no dominio; regras HTTP nao se aplicam nesta fase sem interface externa.
- [x] **Banco de Dados**: Nao ha migration nem tabela obrigatoria nesta etapa porque o escopo cobre apenas a modelagem de dominio.

## Project Structure

### Documentation (this feature)

```text
specs/004-orcamento-ordem-servico/
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

**Structure Decision**: Manter a estrutura atual em camadas. Esta feature deve adicionar `Orcamento`, seus enums e eventuais tipos auxiliares em `src/main/java/com/postech/workshop_service/domain/entities/` e concentrar os testes em `src/test/java/com/postech/workshop_service/domain/entities/`, reutilizando `OrdemServico` como colaboradora de dominio.

## Phase 0: Research Summary

- Reutilizar `RegraDeNegocioException` para bloquear transicoes invalidas de status no orcamento e inconsistencias de coordenacao com a ordem de servico.
- Tratar a aprovacao do orcamento original como gatilho de avancar `OrdemServico` para `EM_EXECUCAO`, o que exige ampliar o comportamento de `OrdemServico` para suportar essa transicao explicitamente.
- Manter `ADICAO_SERVICO` sem capacidade de cancelar a ordem vinculada, isolando o impacto do cancelamento ao proprio orcamento complementar.
- Adiar persistencia, contrato externo, caso de uso e migracoes do orcamento para etapas futuras.

## Phase 1: Design Summary

- Modelar `Orcamento` como entidade de dominio derivada de `EntidadeBase`, com `idOrdemServico`, `valor`, `status`, `itens` e `tipo`.
- Modelar `StatusOrcamento` e `TipoOrcamento` como enumeracoes do dominio.
- Representar `itens` como uma colecao de um tipo de dominio proprio para permitir validacao e evolucao sem antecipar rejeicao parcial.
- Integrar `Orcamento` com `OrdemServico` por comportamento de dominio, preservando a regra de cancelamento existente e adicionando o comportamento necessario para avancar a ordem para `EM_EXECUCAO`.
- Cobrir testes unitarios para todas as transicoes permitidas e bloqueadas, inclusive o efeito do orcamento inicial sobre a ordem de servico.

## Phase 2: Implementation Preview

1. Criar os tipos `StatusOrcamento`, `TipoOrcamento` e `ItemOrcamento` no dominio.
2. Criar `Orcamento` alinhado a `EntidadeBase`, com construtores de criacao e reconstituicao.
3. Implementar `enviarParaAprovacao()`, `aprovar(OrdemServico ordemServico)`, `rejeitar()` e `cancelar(OrdemServico ordemServico)` com `RegraDeNegocioException`.
4. Expandir `OrdemServico` com o comportamento necessario para avancar para `EM_EXECUCAO` sem quebrar a regra existente de cancelamento.
5. Adicionar testes unitarios cobrindo criacao, transicoes validas, transicoes invalidas e os efeitos do orcamento do tipo `SERVICO_ORIGINAL` sobre `OrdemServico`.

## Complexity Tracking

Nenhuma violacao de constituicao prevista.

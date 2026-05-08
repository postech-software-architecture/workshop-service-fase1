# Implementation Plan: Estoque Integrado ao Ciclo da Ordem

**Branch**: `009-estoque-integrado` | **Date**: 2026-05-07 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/009-estoque-integrado/spec.md`

## Summary

Integrar o estoque ao ciclo operacional da ordem de servico: aprovar orcamento reserva pecas e insumos, iniciar execucao consome reservas existentes, rejeitar ou cancelar libera reservas ativas, e toda movimentacao operacional fica rastreavel pela OS. A abordagem tecnica preserva a arquitetura atual em camadas, usa o dominio `Estoque` para alterar saldos, adiciona contexto tecnico nas movimentacoes de estoque e usa lock otimista ja existente em `Estoque` para proteger aprovacoes concorrentes.

## Technical Context

**Language/Version**: Java 21  
**Primary Dependencies**: Spring Boot 3.4.1, Spring Web, Spring Data JPA, Spring Validation, Spring Security, SpringDoc OpenAPI, Lombok, MapStruct, Flyway  
**Storage**: PostgreSQL com migrations Flyway  
**Testing**: Maven test lifecycle com JUnit/Spring Boot Test/Testcontainers ja existentes  
**Target Platform**: Backend web service JVM  
**Project Type**: REST web service monolito em camadas  
**Performance Goals**: Fluxos de aprovacao, rejeicao, cancelamento e inicio de execucao devem concluir em uma unica operacao transacional para uma OS comum, sem consultas por texto livre para localizar reservas.  
**Constraints**: Manter regras de negocio na camada de dominio/aplicacao, documentar contratos OpenAPI, retornar erros de regra conforme padrao global do projeto, preservar consistencia transacional de OS, orcamento, estoque e movimentacoes.  
**Scale/Scope**: Feature restrita ao ciclo de estoque de uma ordem de servico e seus orcamentos; nao inclui relatorios gerenciais novos nem interface de consulta dedicada alem dos contratos ja existentes/planejados.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- [x] **Idioma e Plataforma**: Implementacao planejada em pt-BR e Java 21.
- [x] **Padroes de Codigo**: Lombok mantido nas entidades JPA/DTOs; dominio continua com comportamento explicito em vez de setters genericos.
- [x] **Arquitetura**: Controllers permanecem apenas como camada de entrada; use cases orquestram; dominio concentra invariantes; infraestrutura adapta persistencia.
- [x] **Dominio (DDD)**: `Estoque`, `MovimentacaoEstoque`, `Orcamento` e `OrdemServico` continuam como entidades de dominio com regras explicitas.
- [x] **Documentacao**: Metodos publicos novos/alterados exigirao Javadoc; OpenAPI sera atualizado para contratos afetados.
- [x] **Testes**: Plano cobre unitarios de dominio/use cases e integracao de repositorios/controllers para caminhos principais e edge cases.
- [x] **Validacao e Padroes de Erro**: Violacoes de regra seguirao excecoes de negocio existentes e contratos documentados para falhas de estoque.
- [x] **Banco de Dados**: Migration Flyway seguira `V0.YYYYMMDDHHMMSS__descricao_da_acao.sql`, com comentarios SQL, UUIDs e constraints/indices nomeados.

## Project Structure

### Documentation (this feature)

```text
specs/009-estoque-integrado/
|-- plan.md
|-- research.md
|-- data-model.md
|-- quickstart.md
|-- contracts/
|   `-- openapi.yaml
|-- checklists/
|   `-- requirements.md
`-- spec.md
```

### Source Code (repository root)

```text
src/main/java/com/postech/workshop_service/
|-- api/
|   |-- controllers/
|   |   |-- EstoqueController.java
|   |   |-- OrcamentoController.java
|   |   |-- OrdemServicoController.java
|   |   `-- openapi.yaml
|   `-- dtos/
|       |-- MovimentacaoRequest.java
|       `-- MovimentacaoResponse.java
|-- application/
|   `-- usecases/
|       |-- AprovarOrcamentoUseCase.java
|       |-- CancelarOrcamentoUseCase.java
|       |-- IniciarExecucaoUseCase.java
|       `-- RejeitarOrcamentoUseCase.java
|-- domain/
|   |-- entities/
|   |   |-- Estoque.java
|   |   `-- MovimentacaoEstoque.java
|   |-- repositories/
|   |   |-- EstoqueRepository.java
|   |   `-- MovimentacaoEstoqueRepository.java
|   `-- valueobjects/
|       `-- TipoMovimentacao.java
`-- infrastructure/
    `-- persistence/
        |-- entities/
        |   |-- EstoqueJpaEntity.java
        |   `-- MovimentacaoEstoqueJpaEntity.java
        |-- mappers/
        |   |-- EstoqueMapper.java
        |   `-- MovimentacaoEstoqueMapper.java
        `-- repositories/
            |-- EstoqueRepositoryImpl.java
            |-- JpaEstoqueRepository.java
            |-- JpaMovimentacaoEstoqueRepository.java
            `-- MovimentacaoEstoqueRepositoryImpl.java

src/main/resources/db/migration/
`-- V0.YYYYMMDDHHMMSS__vincular_movimentacao_estoque_ordem_orcamento.sql

src/test/java/com/postech/workshop_service/
|-- api/controllers/
|   |-- EstoqueControllerIT.java
|   |-- OrcamentoControllerIT.java
|   `-- OrdemServicoControllerIT.java
|-- application/usecases/
|   |-- AprovarOrcamentoUseCaseTest.java
|   |-- CancelarOrcamentoUseCaseTest.java
|   |-- IniciarExecucaoUseCaseTest.java
|   `-- RejeitarOrcamentoUseCaseTest.java
|-- domain/entities/
|   |-- EstoqueTest.java
|   `-- MovimentacaoEstoqueTest.java
`-- infrastructure/persistence/repositories/
    |-- EstoqueRepositoryImplIT.java
    `-- MovimentacaoEstoqueRepositoryImplIT.java
```

**Structure Decision**: Manter o monolito Spring Boot em camadas ja existente. A feature toca o dominio de estoque e OS, os use cases de ciclo operacional, a persistencia JPA/Flyway e os contratos REST atuais.

## Complexity Tracking

Nenhuma violacao constitucional prevista.

## Phase 0 Research

Research concluida em [research.md](./research.md). As decisoes resolvem rastreabilidade por OS/orcamento, protecao concorrente por lock otimista, consumo idempotente de reservas e separacao entre movimentacoes internas e manuais.

## Phase 1 Design

Design concluido em:

- [data-model.md](./data-model.md)
- [contracts/openapi.yaml](./contracts/openapi.yaml)
- [quickstart.md](./quickstart.md)

## Post-Design Constitution Check

- [x] **Idioma e Plataforma**: Artefatos direcionam nomes de codigo em pt-BR e Java 21.
- [x] **Padroes de Codigo**: Novos campos e mapeamentos preservam Lombok em JPA/DTOs e comportamento no dominio.
- [x] **Arquitetura**: Contratos nao colocam regra em controller; plano distribui regras entre dominio e use cases.
- [x] **Dominio (DDD)**: Reservar, consumir e liberar continuam como comportamentos de `Estoque`/movimentacao.
- [x] **Documentacao**: OpenAPI da feature cobre endpoints afetados e ajustes de schema.
- [x] **Testes**: Quickstart define os grupos minimos de testes unitarios e integracao.
- [x] **Validacao e Padroes de Erro**: Falhas de negocio e concorrencia documentadas como respostas de negocio.
- [x] **Banco de Dados**: Data model exige migration com comentarios, FKs, indices e auditoria preservada.

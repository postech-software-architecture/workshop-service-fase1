# Implementation Plan: Gestao de Pecas, Insumos e Estoques

**Branch**: `003-parts-inventory-management` | **Date**: 2026-04-29 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/003-parts-inventory-management/spec.md`

## Summary

Adicionar um modulo de gestao de pecas e insumos com cadastro, atualizacao, consulta paginada, busca por SKU, controle de movimentacoes de estoque (entrada, saida, ajuste) por localizacao, historico de movimentacoes, alertas de estoque baixo e remocao logica, utilizando optimistic locking para concurrencia e mantendo rastreabilidade completa das operacoes. O modelo suporta multiplas localizacoes de estoque por peca atraves da entidade Estoque.

## Technical Context

**Language/Version**: Java 21  
**Primary Dependencies**: Spring Boot 3.4.1, Spring Web, Spring Data JPA, Spring Validation, Spring Security, SpringDoc OpenAPI, Lombok, MapStruct, Flyway  
**Storage**: PostgreSQL com Flyway para schema versionado  
**Testing**: JUnit 5, Spring Boot Test, Testcontainers (PostgreSQL), JaCoCo  
**Target Platform**: Servico web Spring Boot para back-office interno  
**Project Type**: Web-service monolitico  
**Performance Goals**: Consultas por SKU, identificador e listagem paginada com comportamento consistente para a carga operacional normal da oficina, sem meta formal de benchmark nesta iteracao MVP  
**Constraints**: DDD em camadas, codigo e contratos em pt-BR, validacao estrutural com HTTP 422, regras de negocio com HTTP 400, remocao logica obrigatoria, SKU unico apenas entre pecas ativas, estoque nunca negativo, optimistic locking para concurrencia, ajuste de estoque como valor absoluto  
**Scale/Scope**: Cadastro operacional de oficina com centenas de pecas/insumos, movimentacoes frequentes e consultas de disponibilidade em tempo real

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- [x] **Idioma e Plataforma**: Implementacao planejada em Java 21 e nomenclatura/documentacao em pt-BR.
- [x] **Padroes de Codigo**: Lombok sera usado sem expor mutadores genericos no dominio; invariantes ficarao encapsuladas em entidades e value objects.
- [x] **Arquitetura**: Fluxo controller -> use case -> dominio -> repositorio -> JPA, sem regras de negocio em controllers ou DTOs.
- [x] **Dominio (DDD)**: `PecaInsumo` sera aggregate root com comportamento explicito para movimentacoes e remocao logica; SKU e demais campos sensiveis ficarao com validacao no dominio.
- [x] **Documentacao**: Endpoints documentados em OpenAPI e implementacao prevista com Javadocs para todos os metodos publicos relevantes.
- [x] **Testes**: Planejados testes unitarios para dominio e casos de uso, alem de integracao para controller e persistencia.
- [x] **Validacao e Padroes de Erro**: DTOs cuidarao da validacao estrutural; regras como SKU duplicado, estoque insuficiente e valor invalido retornarao HTTP 400.
- [x] **Banco de Dados**: Novas migrations Flyway seguirao `V0.YYYYMMDDHHMMSS__descricao.sql`, com comentarios SQL, PK UUID, auditoria e FKs nomeadas.

## Project Structure

### Documentation (this feature)

```text
specs/003-parts-inventory-management/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── openapi.yaml
└── tasks.md
```

### Source Code (repository root)

```text
src/
├── main/
│   ├── java/com/postech/workshop_service/
│   │   ├── api/
│   │   │   ├── controllers/
│   │   │   │   └── PecaInsumoController.java
│   │   │   └── dtos/
│   │   │       ├── CadastroPecaRequest.java
│   │   │       ├── AtualizarPecaRequest.java
│   │   │       ├── PecaResponse.java
│   │   │       ├── EstoqueResponse.java
│   │   │       ├── MovimentacaoRequest.java
│   │   │       └── MovimentacaoResponse.java
│   │   ├── application/usecases/
│   │   │   ├── CriarPecaUseCase.java
│   │   │   ├── AtualizarPecaUseCase.java
│   │   │   ├── BuscarPecaPorIdUseCase.java
│   │   │   ├── BuscarPecaPorSkuUseCase.java
│   │   │   ├── ListarPecasUseCase.java
│   │   │   ├── RemoverPecaUseCase.java
│   │   │   ├── CriarEstoqueUseCase.java
│   │   │   ├── RegistrarMovimentacaoUseCase.java
│   │   │   ├── ListarHistoricoMovimentacoesUseCase.java
│   │   │   └── ListarPecasEstoqueBaixoUseCase.java
│   │   ├── domain/
│   │   │   ├── entities/
│   │   │   │   ├── PecaInsumo.java
│   │   │   │   ├── Estoque.java
│   │   │   │   └── MovimentacaoEstoque.java
│   │   │   ├── repositories/
│   │   │   │   ├── PecaInsumoRepository.java
│   │   │   │   ├── EstoqueRepository.java
│   │   │   │   └── MovimentacaoEstoqueRepository.java
│   │   │   └── valueobjects/
│   │   │       ├── TipoMovimentacao.java
│   │   │       └── UnidadeMedida.java
│   │   └── infrastructure/
│   │       ├── config/
│   │       └── persistence/
│   │           ├── entities/
│   │           │   ├── PecaInsumoJpaEntity.java
│   │           │   ├── EstoqueJpaEntity.java
│   │           │   └── MovimentacaoEstoqueJpaEntity.java
│   │           ├── mappers/
│   │           │   ├── PecaInsumoMapper.java
│   │           │   ├── EstoqueMapper.java
│   │           │   └── MovimentacaoEstoqueMapper.java
│   │           └── repositories/
│   │               ├── PecaInsumoJpaRepository.java
│   │               ├── EstoqueJpaRepository.java
│   │               └── MovimentacaoEstoqueJpaRepository.java
│   └── resources/
│       └── db/migration/
│           └── V0.20260429220000__create_table_pecas_estoques_movimentacoes.sql
└── test/
    └── java/com/postech/workshop_service/
        ├── api/controllers/
        ├── application/usecases/
        ├── domain/
        └── infrastructure/persistence/repositories/
```

**Structure Decision**: Manter o monolito Spring Boot existente e acrescentar o modulo de pecas/insumos nas mesmas camadas ja usadas por clientes e veiculos, incluindo migration dedicada e testes unitarios/integracao no mesmo arranjo de pacotes.

## Complexity Tracking

Nenhuma violacao constitucional planejada.

## Phase 0: Research Summary

As decisoes de pesquisa foram consolidadas em [research.md](./research.md), resolvendo as definicoes de estrutura de dados, controle de concorrencia (optimistic locking), estrategia de soft delete, validacao de SKU, e endpoints REST.

## Phase 1: Design Outputs

- Modelo de dados detalhado em [data-model.md](./data-model.md)
- Contrato HTTP planejado em [contracts/openapi.yaml](./contracts/openapi.yaml)
- Passos de implementacao e verificacao em [quickstart.md](./quickstart.md)

## Post-Design Constitution Check

- [x] **Idioma e Plataforma**: Artefatos e contrato mantidos em pt-BR; stack continua Java 21.
- [x] **Padroes de Codigo**: O desenho privilegia comportamento no dominio (`registrarEntrada`, `registrarSaida`, `ajustarEstoque`, `removerLogicamente`) em vez de setters indiscriminados.
- [x] **Arquitetura**: A integracao entre pecas e movimentacoes ocorre por repositorio/caso de uso, sem acoplamento da API a JPA.
- [x] **Dominio (DDD)**: A unicidade do SKU entre pecas ativas, a proibicao de estoque negativo e o controle de concorrencia ficam no dominio/aplicacao, nao no controller.
- [x] **Documentacao**: OpenAPI cobre endpoints, filtros e payloads do MVP; Javadocs continuarao obrigatorios para todos os metodos publicos relevantes.
- [x] **Testes**: O quickstart exige cobertura unitaria e de integracao para todas as operacoes publicas da feature.
- [x] **Validacao e Padroes de Erro**: O contrato distingue 422 (estrutura) de 400 (negocio) e preserva 404 para recursos inexistentes.
- [x] **Banco de Dados**: O modelo inclui tabelas `pecas_insumos`, `estoques` e `movimentacoes_estoque` com auditoria, FKs nomeadas, indices otimizados e suporte a remocao logica. Quantidade total de uma peca e calculada pela soma de seus estoques.

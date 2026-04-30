# Tasks: Gestao de Pecas, Insumos e Estoques

**Input**: Design documents from `/specs/003-parts-inventory-management/`
**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/

**Tests**: Incluidos conforme SC-008 (cobertura minima de 80%)

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

- **Single project**: `src/main/java/`, `src/test/java/` at repository root
- Paths shown below follow the existing project structure from plan.md

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Database schema and base infrastructure for parts/inventory

- [ ] T001 Create Flyway migration for pecas_insumos, estoques and movimentacoes_estoque tables in `src/main/resources/db/migration/V0.20260429220000__create_table_pecas_estoques_movimentacoes.sql`
- [ ] T002 [P] Create TipoMovimentacao enum in `src/main/java/com/postech/workshop_service/domain/valueobjects/TipoMovimentacao.java`
- [ ] T003 [P] Create UnidadeMedida enum in `src/main/java/com/postech/workshop_service/domain/valueobjects/UnidadeMedida.java`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core domain entities and repository interfaces that ALL user stories depend on

**⚠ CRITICAL**: No user story work can begin until this phase is complete

- [ ] T004 Create PecaInsumo domain entity (without quantidadeEstoque) in `src/main/java/com/postech/workshop_service/domain/entities/PecaInsumo.java`
- [ ] T005 [P] Create Estoque domain entity with business methods in `src/main/java/com/postech/workshop_service/domain/entities/Estoque.java`
- [ ] T006 [P] Create MovimentacaoEstoque domain entity in `src/main/java/com/postech/workshop_service/domain/entities/MovimentacaoEstoque.java`
- [ ] T007 [P] Create PecaInsumoRepository interface in `src/main/java/com/postech/workshop_service/domain/repositories/PecaInsumoRepository.java`
- [ ] T008 [P] Create EstoqueRepository interface in `src/main/java/com/postech/workshop_service/domain/repositories/EstoqueRepository.java`
- [ ] T009 [P] Create MovimentacaoEstoqueRepository interface in `src/main/java/com/postech/workshop_service/domain/repositories/MovimentacaoEstoqueRepository.java`
- [ ] T010 Create PecaInsumoJpaEntity with optimistic locking (@Version) in `src/main/java/com/postech/workshop_service/infrastructure/persistence/entities/PecaInsumoJpaEntity.java`
- [ ] T011 [P] Create EstoqueJpaEntity with optimistic locking in `src/main/java/com/postech/workshop_service/infrastructure/persistence/entities/EstoqueJpaEntity.java`
- [ ] T012 [P] Create MovimentacaoEstoqueJpaEntity in `src/main/java/com/postech/workshop_service/infrastructure/persistence/entities/MovimentacaoEstoqueJpaEntity.java`
- [ ] T013 [P] Create PecaInsumoMapper (MapStruct) in `src/main/java/com/postech/workshop_service/infrastructure/persistence/mappers/PecaInsumoMapper.java`
- [ ] T014 [P] Create EstoqueMapper (MapStruct) in `src/main/java/com/postech/workshop_service/infrastructure/persistence/mappers/EstoqueMapper.java`
- [ ] T015 [P] Create MovimentacaoEstoqueMapper (MapStruct) in `src/main/java/com/postech/workshop_service/infrastructure/persistence/mappers/MovimentacaoEstoqueMapper.java`
- [ ] T016 Create PecaInsumoJpaRepository with custom queries in `src/main/java/com/postech/workshop_service/infrastructure/persistence/repositories/PecaInsumoJpaRepository.java`
- [ ] T017 [P] Create EstoqueJpaRepository with custom queries in `src/main/java/com/postech/workshop_service/infrastructure/persistence/repositories/EstoqueJpaRepository.java`
- [ ] T018 [P] Create MovimentacaoEstoqueJpaRepository in `src/main/java/com/postech/workshop_service/infrastructure/persistence/repositories/MovimentacaoEstoqueJpaRepository.java`
- [ ] T019 Implement PecaInsumoRepositoryImpl using JPA repository in `src/main/java/com/postech/workshop_service/infrastructure/persistence/repositories/PecaInsumoRepositoryImpl.java`
- [ ] T020 [P] Implement EstoqueRepositoryImpl in `src/main/java/com/postech/workshop_service/infrastructure/persistence/repositories/EstoqueRepositoryImpl.java`
- [ ] T021 [P] Implement MovimentacaoEstoqueRepositoryImpl in `src/main/java/com/postech/workshop_service/infrastructure/persistence/repositories/MovimentacaoEstoqueRepositoryImpl.java`

**Checkpoint**: Foundation ready - user story implementation can now begin

---

## Phase 3: User Story 1 - Cadastrar e manter pecas e insumos (Priority: P1) 🎯 MVP

**Goal**: Permitir cadastrar pecas e insumos com dados obrigatorios e opcionais, mantendo o catalogo de materiais disponiveis.

**Independent Test**: Cadastrar uma peca com SKU unico, nome, quantidade inicial, valor unitario, estoque minimo e unidade de medida, verificando que os dados sao persistidos corretamente.

### Tests for User Story 1

- [ ] T016 [P] [US1] Create unit tests for PecaInsumo domain entity in `src/test/java/com/postech/workshop_service/domain/entities/PecaInsumoTest.java`
- [ ] T017 [P] [US1] Create integration tests for CriarPecaUseCase in `src/test/java/com/postech/workshop_service/application/usecases/CriarPecaUseCaseTest.java`
- [ ] T018 [P] [US1] Create integration tests for PecaInsumoController (CRUD) in `src/test/java/com/postech/workshop_service/api/controllers/PecaInsumoControllerIntegrationTest.java`

### Implementation for User Story 1

- [ ] T019 [P] [US1] Create CadastroPecaRequest DTO with validations in `src/main/java/com/postech/workshop_service/api/dtos/CadastroPecaRequest.java`
- [ ] T020 [P] [US1] Create AtualizarPecaRequest DTO with validations in `src/main/java/com/postech/workshop_service/api/dtos/AtualizarPecaRequest.java`
- [ ] T021 [P] [US1] Create PecaResponse DTO in `src/main/java/com/postech/workshop_service/api/dtos/PecaResponse.java`
- [ ] T022 [US1] Implement CriarPecaUseCase with SKU uniqueness validation in `src/main/java/com/postech/workshop_service/application/usecases/CriarPecaUseCase.java`
- [ ] T023 [US1] Implement AtualizarPecaUseCase with optimistic locking in `src/main/java/com/postech/workshop_service/application/usecases/AtualizarPecaUseCase.java`
- [ ] T024 [US1] Implement BuscarPecaPorIdUseCase in `src/main/java/com/postech/workshop_service/application/usecases/BuscarPecaPorIdUseCase.java`
- [ ] T025 [US1] Implement BuscarPecaPorSkuUseCase in `src/main/java/com/postech/workshop_service/application/usecases/BuscarPecaPorSkuUseCase.java`
- [ ] T026 [US1] Implement ListarPecasUseCase with pagination and filters in `src/main/java/com/postech/workshop_service/application/usecases/ListarPecasUseCase.java`
- [ ] T027 [US1] Create PecaInsumoController with CRUD endpoints in `src/main/java/com/postech/workshop_service/api/controllers/PecaInsumoController.java`
- [ ] T028 [US1] Add OpenAPI documentation for pecas endpoints in `src/main/java/com/postech/workshop_service/api/controllers/openapi.yaml`

**Checkpoint**: User Story 1 completo - CRUD de pecas funcional e testado independentemente

---

## Phase 4: User Story 1.5 - Criar estoques para pecas (Priority: P1) 🎯 MVP

**Goal**: Permitir criar estoques (localizacoes) para pecas cadastradas, possibilitando multiplas localizacoes por peca.

**Independent Test**: Criar um estoque para uma peca existente com localizacao e quantidade inicial, verificando que a quantidade total da peca e calculada corretamente.

### Tests for User Story 1.5

- [ ] T038 [P] [US1.5] Create unit tests for Estoque domain entity in `src/test/java/com/postech/workshop_service/domain/entities/EstoqueTest.java`
- [ ] T039 [P] [US1.5] Create integration tests for CriarEstoqueUseCase in `src/test/java/com/postech/workshop_service/application/usecases/CriarEstoqueUseCaseTest.java`

### Implementation for User Story 1.5

- [ ] T040 [P] [US1.5] Create CriarEstoqueRequest DTO with validations in `src/main/java/com/postech/workshop_service/api/dtos/CriarEstoqueRequest.java`
- [ ] T041 [P] [US1.5] Create EstoqueResponse DTO in `src/main/java/com/postech/workshop_service/api/dtos/EstoqueResponse.java`
- [ ] T042 [US1.5] Implement CriarEstoqueUseCase in `src/main/java/com/postech/workshop_service/application/usecases/CriarEstoqueUseCase.java`
- [ ] T043 [US1.5] Add estoques endpoints to PecaInsumoController in `src/main/java/com/postech/workshop_service/api/controllers/PecaInsumoController.java`
- [ ] T044 [US1.5] Update OpenAPI documentation for estoques endpoints in `src/main/java/com/postech/workshop_service/api/controllers/openapi.yaml`

**Checkpoint**: User Story 1.5 completo - Estoque por localizacao funcional

---

## Phase 5: User Story 2 - Controlar movimentacoes de estoque (Priority: P1) 🎯 MVP

**Goal**: Registrar entradas, saidas e ajustes de estoque mantendo controle preciso da quantidade disponivel por localizacao.

**Independent Test**: Realizar uma entrada de estoque, uma saida e um ajuste em um estoque especifico, verificando que as quantidades sao atualizadas corretamente e o historico registra cada operacao.

### Tests for User Story 2

- [ ] T045 [P] [US2] Create unit tests for stock movement methods in Estoque in `src/test/java/com/postech/workshop_service/domain/entities/EstoqueTest.java`
- [ ] T046 [P] [US2] Create integration tests for RegistrarMovimentacaoUseCase in `src/test/java/com/postech/workshop_service/application/usecases/RegistrarMovimentacaoUseCaseTest.java`

### Implementation for User Story 2

- [ ] T047 [P] [US2] Create MovimentacaoRequest DTO with validations in `src/main/java/com/postech/workshop_service/api/dtos/MovimentacaoRequest.java`
- [ ] T048 [P] [US2] Create MovimentacaoResponse DTO in `src/main/java/com/postech/workshop_service/api/dtos/MovimentacaoResponse.java`
- [ ] T049 [US2] Implement RegistrarMovimentacaoUseCase with stock validation in `src/main/java/com/postech/workshop_service/application/usecases/RegistrarMovimentacaoUseCase.java`
- [ ] T050 [US2] Add movimentacao endpoint to PecaInsumoController in `src/main/java/com/postech/workshop_service/api/controllers/PecaInsumoController.java`
- [ ] T051 [US2] Update OpenAPI documentation for movimentacoes endpoints in `src/main/java/com/postech/workshop_service/api/controllers/openapi.yaml`

**Checkpoint**: User Story 2 completo - Movimentacoes de estoque funcionais e testadas independentemente

---

## Phase 6: User Story 3 - Consultar pecas e verificar disponibilidade (Priority: P2)

**Goal**: Consultar pecas por SKU, nome ou categoria e verificar disponibilidade em estoque (soma de todas as localizacoes).

**Independent Test**: Cadastrar varias pecas com estoques em multiplas localizacoes e verificar que a busca por SKU retorna a peca correta com quantidade total calculada.

### Tests for User Story 3

- [ ] T052 [P] [US3] Create integration tests for search endpoints in `src/test/java/com/postech/workshop_service/api/controllers/PecaInsumoControllerIntegrationTest.java`

### Implementation for User Story 3

- [ ] T053 [US3] Enhance ListarPecasUseCase with categoria and nome filters in `src/main/java/com/postech/workshop_service/application/usecases/ListarPecasUseCase.java`
- [ ] T054 [US3] Add query methods to PecaInsumoJpaRepository for filters in `src/main/java/com/postech/workshop_service/infrastructure/persistence/repositories/PecaInsumoJpaRepository.java`
- [ ] T055 [US3] Add filter parameters to list endpoint in PecaInsumoController in `src/main/java/com/postech/workshop_service/api/controllers/PecaInsumoController.java`
- [ ] T056 [US3] Update PecaResponse to include quantidadeTotal (calculated) in `src/main/java/com/postech/workshop_service/api/dtos/PecaResponse.java`

**Checkpoint**: User Story 3 completo - Consultas com filtros funcionais

---

## Phase 7: User Story 4 - Receber alertas de estoque baixo (Priority: P2)

**Goal**: Consultar lista de pecas com estoque total abaixo do minimo para providenciar reposicao.

**Independent Test**: Cadastrar pecas com estoque minimo definido, reduzir o estoque total abaixo do limite e verificar que a consulta de itens com estoque baixo retorna as pecas corretas.

### Tests for User Story 4

- [ ] T057 [P] [US4] Create integration tests for estoque baixo endpoint in `src/test/java/com/postech/workshop_service/api/controllers/PecaInsumoControllerIntegrationTest.java`

### Implementation for User Story 4

- [ ] T058 [P] [US4] Create PecaEstoqueBaixoResponse DTO in `src/main/java/com/postech/workshop_service/api/dtos/PecaEstoqueBaixoResponse.java`
- [ ] T059 [US4] Implement ListarPecasEstoqueBaixoUseCase in `src/main/java/com/postech/workshop_service/application/usecases/ListarPecasEstoqueBaixoUseCase.java`
- [ ] T060 [US4] Add estoque-baixo query method to PecaInsumoJpaRepository (using view or subquery) in `src/main/java/com/postech/workshop_service/infrastructure/persistence/repositories/PecaInsumoJpaRepository.java`
- [ ] T061 [US4] Add estoque-baixo endpoint to PecaInsumoController in `src/main/java/com/postech/workshop_service/api/controllers/PecaInsumoController.java`
- [ ] T062 [US4] Update OpenAPI documentation for estoque-baixo endpoint in `src/main/java/com/postech/workshop_service/api/controllers/openapi.yaml`

**Checkpoint**: User Story 4 completo - Alertas de estoque baixo funcionais

---

## Phase 8: User Story 5 - Consultar historico de movimentacoes (Priority: P3)

**Goal**: Consultar o historico completo de movimentacoes de um estoque especifico para rastrear entradas, saidas e ajustes.

**Independent Test**: Realizar algumas movimentacoes em um estoque e verificar que o historico lista todas as operacoes com data, tipo, quantidade e motivo.

### Tests for User Story 5

- [ ] T063 [P] [US5] Create integration tests for historico endpoint in `src/test/java/com/postech/workshop_service/api/controllers/PecaInsumoControllerIntegrationTest.java`

### Implementation for User Story 5

- [ ] T064 [US5] Implement ListarHistoricoMovimentacoesUseCase with filters in `src/main/java/com/postech/workshop_service/application/usecases/ListarHistoricoMovimentacoesUseCase.java`
- [ ] T065 [US5] Add historico query methods to MovimentacaoEstoqueJpaRepository in `src/main/java/com/postech/workshop_service/infrastructure/persistence/repositories/MovimentacaoEstoqueJpaRepository.java`
- [ ] T066 [US5] Add historico endpoint to PecaInsumoController in `src/main/java/com/postech/workshop_service/api/controllers/PecaInsumoController.java`
- [ ] T067 [US5] Update OpenAPI documentation for historico endpoint in `src/main/java/com/postech/workshop_service/api/controllers/openapi.yaml`

**Checkpoint**: User Story 5 completo - Historico de movimentacoes funcional

---

## Phase 9: User Story 6 - Remover peca do catalogo (Priority: P3)

**Goal**: Desativar pecas descontinuadas preservando o historico de movimentacoes.

**Independent Test**: Tentar remover uma peca sem estoques (sucesso) e uma com estoques/movimentacoes (soft delete), verificando que a peca desaparece das consultas ativas mas o historico permanece acessivel.

### Tests for User Story 6

- [ ] T068 [P] [US6] Create integration tests for soft delete endpoint in `src/test/java/com/postech/workshop_service/api/controllers/PecaInsumoControllerIntegrationTest.java`

### Implementation for User Story 6

- [ ] T069 [US6] Implement RemoverPecaUseCase with soft delete in `src/main/java/com/postech/workshop_service/application/usecases/RemoverPecaUseCase.java`
- [ ] T070 [US6] Add delete endpoint to PecaInsumoController in `src/main/java/com/postech/workshop_service/api/controllers/PecaInsumoController.java`

**Checkpoint**: User Story 6 completo - Remocao logica funcional

---

## Phase 10: Polish & Cross-Cutting Concerns

**Purpose**: Final improvements and validation

- [ ] T071 Add Javadoc to all public methods in domain entities
- [ ] T072 [P] Add Javadoc to all public methods in use cases
- [ ] T073 [P] Add Javadoc to all public methods in controller
- [ ] T074 Run JaCoCo coverage report and verify >= 80% coverage
- [ ] T075 Run quickstart.md validation scenarios
- [ ] T076 Final code review and cleanup

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Stories (Phase 3-9)**: All depend on Foundational phase completion
  - US1 (P1): Peca CRUD
  - US1.5 (P1): Estoque creation - depends on US1
  - US2 (P1): Movimentacoes - depends on US1.5
  - US3 (P2): Consultas - depends on US1
  - US4 (P2): Alertas - depends on US1 + US1.5
  - US5 (P3): Historico - depends on US2
  - US6 (P3): Remocao - depends on US1
- **Polish (Phase 10)**: Depends on all user stories being complete

### User Story Dependencies

- **US1 (P1)**: Can start after Foundational - No dependencies on other stories
- **US1.5 (P1)**: Depends on US1 (needs PecaInsumo to exist)
- **US2 (P1)**: Depends on US1.5 (needs Estoque to exist)
- **US3 (P2)**: Can start after Foundational - Uses endpoints from US1
- **US4 (P2)**: Depends on US1 + US1.5 (needs estoques with quantities)
- **US5 (P3)**: Depends on US2 (needs MovimentacaoEstoque)
- **US6 (P3)**: Can start after Foundational - Uses PecaInsumo from US1

### Within Each User Story

- Tests MUST be written and FAIL before implementation
- DTOs before use cases
- Use cases before controller endpoints
- Core implementation before integration

### Parallel Opportunities

- T002, T003: Value objects can run in parallel
- T005, T006: Estoque and MovimentacaoEstoque entities can run in parallel
- T007, T008, T009: Repository interfaces can run in parallel
- T010, T011, T012: JPA entities can run in parallel
- T013, T014, T015: Mappers can run in parallel
- T016, T017, T018: JPA repositories can run in parallel
- T019, T020, T021: Repository implementations can run in parallel
- All tests within each user story can run in parallel
- All DTOs within each user story can run in parallel

---

## Parallel Example: User Story 1

```bash
# Launch all tests for User Story 1 together:
Task: "Create unit tests for PecaInsumo domain entity"
Task: "Create integration tests for CriarPecaUseCase"
Task: "Create integration tests for PecaInsumoController"

# Launch all DTOs for User Story 1 together:
Task: "Create CadastroPecaRequest DTO"
Task: "Create AtualizarPecaRequest DTO"
Task: "Create PecaResponse DTO"
```

---

## Implementation Strategy

### MVP First (User Stories 1 + 1.5 + 2)

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational (CRITICAL - blocks all stories)
3. Complete Phase 3: User Story 1 (CRUD de pecas)
4. Complete Phase 4: User Story 1.5 (Criar estoques)
5. Complete Phase 5: User Story 2 (Movimentacoes de estoque)
6. **STOP and VALIDATE**: Test US1, US1.5 and US2 independently
7. Deploy/demo if ready

### Incremental Delivery

1. Complete Setup + Foundational → Foundation ready
2. Add User Story 1 → Test independently → Deploy/Demo (MVP parte 1!)
3. Add User Story 1.5 → Test independently → Deploy/Demo
4. Add User Story 2 → Test independently → Deploy/Demo (MVP completo!)
5. Add User Stories 3-4 → Test independently → Deploy/Demo
6. Add User Stories 5-6 → Test independently → Deploy/Demo
7. Each story adds value without breaking previous stories

---

## Notes

- [P] tasks = different files, no dependencies
- [Story] label maps task to specific user story for traceability
- Each user story should be independently completable and testable
- Verify tests fail before implementing
- Commit after each task or logical group
- Stop at any checkpoint to validate story independently
- Optimistic locking with @Version for concurrency control
- SKU uniqueness only among active parts (soft delete allows reuse)
- Quantidade total de uma peca = soma de todos os seus estoques ativos
- Tabelas no plural: pecas_insumos, estoques, movimentacoes_estoque

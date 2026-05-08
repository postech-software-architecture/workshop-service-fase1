# Tasks: Estoque Integrado ao Ciclo da Ordem

**Input**: Design documents from `/specs/009-estoque-integrado/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/openapi.yaml, quickstart.md
**Tests**: Required by the feature specification and quickstart validation.
**Organization**: Tasks are grouped by user story so each story remains independently implementable and testable.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel because it touches different files or depends only on completed earlier phases.
- **[Story]**: User story label, present only in story phases.
- Every task includes an exact file path.

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Confirm the feature documentation and current contracts are aligned before implementation.

- [ ] T001 Review feature contract delta in specs/009-estoque-integrado/contracts/openapi.yaml against src/main/java/com/postech/workshop_service/api/controllers/openapi.yaml
- [ ] T002 Review current stock/order/budget implementation paths in specs/009-estoque-integrado/plan.md before editing Java files

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Add shared persistence and domain capabilities required by all stories.

**CRITICAL**: No user story implementation should start until this phase is complete.

- [ ] T003 Create Flyway migration adding ordem_servico_id and orcamento_id to movimentacoes_estoque in src/main/resources/db/migration/V0.YYYYMMDDHHMMSS__vincular_movimentacao_estoque_ordem_orcamento.sql
- [ ] T004 Add ordemServicoId and orcamentoId fields to persistence entity in src/main/java/com/postech/workshop_service/infrastructure/persistence/entities/MovimentacaoEstoqueJpaEntity.java
- [ ] T005 Add ordemServicoId and orcamentoId fields plus constructors/accessors to domain entity in src/main/java/com/postech/workshop_service/domain/entities/MovimentacaoEstoque.java
- [ ] T006 Update mapping of movement context fields in src/main/java/com/postech/workshop_service/infrastructure/persistence/mappers/MovimentacaoEstoqueMapper.java
- [ ] T007 Add repository contract methods for movements by OS and active reservations in src/main/java/com/postech/workshop_service/domain/repositories/MovimentacaoEstoqueRepository.java
- [ ] T008 Add Spring Data queries for movements by OS and reservation lookup in src/main/java/com/postech/workshop_service/infrastructure/persistence/repositories/JpaMovimentacaoEstoqueRepository.java
- [ ] T009 Implement new movement repository methods in src/main/java/com/postech/workshop_service/infrastructure/persistence/repositories/MovimentacaoEstoqueRepositoryImpl.java
- [ ] T010 Add stock selection helper for highest available balance reservation in src/main/java/com/postech/workshop_service/domain/repositories/EstoqueRepository.java
- [ ] T011 Implement stock listing/selection support needed for reservation allocation in src/main/java/com/postech/workshop_service/infrastructure/persistence/repositories/EstoqueRepositoryImpl.java
- [ ] T012 Add or update domain tests for movement context fields in src/test/java/com/postech/workshop_service/domain/entities/MovimentacaoEstoqueTest.java
- [ ] T013 Add repository integration tests for movement lookup by OS and active reservations in src/test/java/com/postech/workshop_service/infrastructure/persistence/repositories/MovimentacaoEstoqueRepositoryImplIT.java

**Checkpoint**: Database, entity, mapper and repository support are ready for all user stories.

---

## Phase 3: User Story 1 - Reservar estoque ao aprovar orcamento (Priority: P1) MVP

**Goal**: Approving a budget reserves required stock atomically, blocks insufficient stock, prevents duplicate reservations and handles concurrent approvals.

**Independent Test**: Approve a pending budget with sufficient stock and verify reservations are created, balances change, status changes persist and insufficient/concurrent approvals return business failure without partial state.

### Tests for User Story 1

- [ ] T014 [P] [US1] Add approval-reserves-stock and insufficient-stock tests in src/test/java/com/postech/workshop_service/application/usecases/AprovarOrcamentoUseCaseTest.java
- [ ] T015 [P] [US1] Add multi-location highest-balance allocation test in src/test/java/com/postech/workshop_service/application/usecases/AprovarOrcamentoUseCaseTest.java
- [ ] T016 [P] [US1] Add duplicate-approval reservation prevention test in src/test/java/com/postech/workshop_service/application/usecases/AprovarOrcamentoUseCaseTest.java
- [ ] T017 [P] [US1] Add concurrent approval integration test for limited stock in src/test/java/com/postech/workshop_service/api/controllers/OrcamentoControllerIT.java

### Implementation for User Story 1

- [ ] T018 [US1] Inject EstoqueRepository and MovimentacaoEstoqueRepository into src/main/java/com/postech/workshop_service/application/usecases/AprovarOrcamentoUseCase.java
- [ ] T019 [US1] Locate stock-controlled composition items and validate total available stock before approval in src/main/java/com/postech/workshop_service/application/usecases/AprovarOrcamentoUseCase.java
- [ ] T020 [US1] Reserve stock from highest available locations first and split reservations when needed in src/main/java/com/postech/workshop_service/application/usecases/AprovarOrcamentoUseCase.java
- [ ] T021 [US1] Persist OS, budget, stock and reservation movements in one transaction in src/main/java/com/postech/workshop_service/application/usecases/AprovarOrcamentoUseCase.java
- [ ] T022 [US1] Convert insufficient stock and optimistic locking conflicts to HTTP 422-compatible business errors in src/main/java/com/postech/workshop_service/application/usecases/AprovarOrcamentoUseCase.java
- [ ] T023 [US1] Update approval response documentation for stock reservation failures in src/main/java/com/postech/workshop_service/api/controllers/OrcamentoController.java
- [ ] T024 [US1] Update approval contract delta in specs/009-estoque-integrado/contracts/openapi.yaml if implementation changes response wording

**Checkpoint**: User Story 1 is independently testable as the MVP.

---

## Phase 4: User Story 2 - Consumir reservas ao iniciar execucao (Priority: P2)

**Goal**: Starting execution consumes existing active reservations exactly once and does not create new reservations.

**Independent Test**: Start execution on an approved OS with active reservations and verify matching stock exits are created once; repeat start does not duplicate consumption.

### Tests for User Story 2

- [ ] T025 [P] [US2] Add active-reservation consumption tests in src/test/java/com/postech/workshop_service/application/usecases/IniciarExecucaoUseCaseTest.java
- [ ] T026 [P] [US2] Add duplicate execution start does not duplicate stock exit test in src/test/java/com/postech/workshop_service/application/usecases/IniciarExecucaoUseCaseTest.java
- [ ] T027 [P] [US2] Add controller integration test for iniciar-execucao consuming reservations in src/test/java/com/postech/workshop_service/api/controllers/OrdemServicoControllerIT.java

### Implementation for User Story 2

- [ ] T028 [US2] Inject EstoqueRepository and MovimentacaoEstoqueRepository into src/main/java/com/postech/workshop_service/application/usecases/IniciarExecucaoUseCase.java
- [ ] T029 [US2] Load active reservations by OS and validate availability for execution consumption in src/main/java/com/postech/workshop_service/application/usecases/IniciarExecucaoUseCase.java
- [ ] T030 [US2] Convert each active reservation into one SAIDA movement linked to the OS in src/main/java/com/postech/workshop_service/application/usecases/IniciarExecucaoUseCase.java
- [ ] T031 [US2] Guard against duplicate consumption when execution start is retried in src/main/java/com/postech/workshop_service/application/usecases/IniciarExecucaoUseCase.java
- [ ] T032 [US2] Update execution endpoint documentation for reservation consumption failures in src/main/java/com/postech/workshop_service/api/controllers/OrdemServicoController.java

**Checkpoint**: User Stories 1 and 2 work independently and together.

---

## Phase 5: User Story 3 - Liberar reservas em rejeicao ou cancelamento (Priority: P3)

**Goal**: Rejecting or cancelling a budget releases only active reservations for the OS and never releases consumed stock.

**Independent Test**: Reject or cancel an approved budget before execution and verify active reservations return to stock; reject/cancel after consumption does not release consumed quantities.

### Tests for User Story 3

- [ ] T033 [P] [US3] Add rejection releases active reservations by OS test in src/test/java/com/postech/workshop_service/application/usecases/RejeitarOrcamentoUseCaseTest.java
- [ ] T034 [P] [US3] Add cancellation releases active reservations by OS test in src/test/java/com/postech/workshop_service/application/usecases/CancelarOrcamentoUseCaseTest.java
- [ ] T035 [P] [US3] Add consumed-reservation is not released tests in src/test/java/com/postech/workshop_service/application/usecases/RejeitarOrcamentoUseCaseTest.java
- [ ] T036 [P] [US3] Add cancel/reject controller integration coverage in src/test/java/com/postech/workshop_service/api/controllers/OrcamentoControllerIT.java

### Implementation for User Story 3

- [ ] T037 [US3] Replace motivo-text reservation lookup with repository lookup by OS in src/main/java/com/postech/workshop_service/application/usecases/RejeitarOrcamentoUseCase.java
- [ ] T038 [US3] Replace motivo-text reservation lookup with repository lookup by OS in src/main/java/com/postech/workshop_service/application/usecases/CancelarOrcamentoUseCase.java
- [ ] T039 [US3] Calculate active reservation quantities before release in src/main/java/com/postech/workshop_service/application/usecases/RejeitarOrcamentoUseCase.java
- [ ] T040 [US3] Calculate active reservation quantities before release in src/main/java/com/postech/workshop_service/application/usecases/CancelarOrcamentoUseCase.java
- [ ] T041 [US3] Persist LIBERACAO movements linked to OS and orcamento in src/main/java/com/postech/workshop_service/application/usecases/RejeitarOrcamentoUseCase.java
- [ ] T042 [US3] Persist LIBERACAO movements linked to OS and orcamento in src/main/java/com/postech/workshop_service/application/usecases/CancelarOrcamentoUseCase.java
- [ ] T043 [US3] Update reject/cancel endpoint documentation for reservation release behavior in src/main/java/com/postech/workshop_service/api/controllers/OrcamentoController.java

**Checkpoint**: User Stories 1 through 3 preserve stock across approval, execution start, rejection and cancellation.

---

## Phase 6: User Story 4 - Auditar movimentacoes do ciclo operacional (Priority: P4)

**Goal**: Users can query stock movements linked to a service order and see only that order's reservation, exit and release movements.

**Independent Test**: Query movements for one OS after a full cycle and verify unrelated OS movements are excluded.

### Tests for User Story 4

- [ ] T044 [P] [US4] Add use case test for listing stock movements by OS in src/test/java/com/postech/workshop_service/application/usecases/ListarMovimentacoesEstoquePorOrdemServicoUseCaseTest.java
- [ ] T045 [P] [US4] Add controller integration test for GET movements by OS in src/test/java/com/postech/workshop_service/api/controllers/EstoqueControllerIT.java

### Implementation for User Story 4

- [ ] T046 [US4] Create ListarMovimentacoesEstoquePorOrdemServicoUseCase in src/main/java/com/postech/workshop_service/application/usecases/ListarMovimentacoesEstoquePorOrdemServicoUseCase.java
- [ ] T047 [US4] Add ordemServicoId and orcamentoId to movement response DTO in src/main/java/com/postech/workshop_service/api/dtos/MovimentacaoResponse.java
- [ ] T048 [US4] Inject and expose GET /api/v1/estoques/movimentacoes/ordem-servico/{id} in src/main/java/com/postech/workshop_service/api/controllers/EstoqueController.java
- [ ] T049 [US4] Document movement-by-OS endpoint in src/main/java/com/postech/workshop_service/api/controllers/openapi.yaml
- [ ] T050 [US4] Update contract delta after implementation details settle in specs/009-estoque-integrado/contracts/openapi.yaml

**Checkpoint**: Users can audit stock movements by OS independently.

---

## Phase 7: Manual Movement Contract & Cross-Story Constraints

**Purpose**: Enforce clarified manual movement and HTTP 422 behavior across the feature.

- [ ] T051 [P] Add manual movement rejects RESERVA, LIBERACAO and AJUSTE tests in src/test/java/com/postech/workshop_service/application/usecases/RegistrarMovimentacaoUseCaseTest.java
- [ ] T052 [P] Add manual movement controller tests for allowed ENTRADA/SAIDA and rejected AJUSTE in src/test/java/com/postech/workshop_service/api/controllers/EstoqueControllerIT.java
- [ ] T053 Update manual movement validation to allow only ENTRADA and SAIDA in src/main/java/com/postech/workshop_service/application/usecases/RegistrarMovimentacaoUseCase.java
- [ ] T054 Update request schema enum/documentation to exclude AJUSTE, RESERVA and LIBERACAO in src/main/java/com/postech/workshop_service/api/dtos/MovimentacaoRequest.java
- [ ] T055 Update OpenAPI manual movement schema to allow only ENTRADA and SAIDA in src/main/java/com/postech/workshop_service/api/controllers/openapi.yaml

---

## Phase 8: Polish & Cross-Cutting Concerns

**Purpose**: Final validation, documentation and consistency checks.

- [ ] T056 [P] Add or update Javadocs for public methods changed in src/main/java/com/postech/workshop_service/application/usecases/AprovarOrcamentoUseCase.java
- [ ] T057 [P] Add or update Javadocs for public methods changed in src/main/java/com/postech/workshop_service/application/usecases/IniciarExecucaoUseCase.java
- [ ] T058 [P] Add or update Javadocs for public repository methods in src/main/java/com/postech/workshop_service/domain/repositories/MovimentacaoEstoqueRepository.java
- [ ] T059 Run focused unit tests from quickstart in specs/009-estoque-integrado/quickstart.md
- [ ] T060 Run repository/controller integration tests from quickstart in specs/009-estoque-integrado/quickstart.md
- [ ] T061 Run full regression suite with mvn test from pom.xml
- [ ] T062 Update tasks completion evidence or notes in specs/009-estoque-integrado/tasks.md after validation

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1 Setup**: No dependencies.
- **Phase 2 Foundational**: Depends on Phase 1 and blocks all user stories.
- **Phase 3 US1**: Depends on Phase 2 and is the MVP.
- **Phase 4 US2**: Depends on Phase 2, but full business validation benefits from US1 reservations.
- **Phase 5 US3**: Depends on Phase 2, but full business validation benefits from US1 reservations and US2 consumption scenarios.
- **Phase 6 US4**: Depends on Phase 2 and can be implemented after repository query support exists.
- **Phase 7 Manual Movement Contract**: Depends on Phase 2 and can run after or alongside user stories.
- **Phase 8 Polish**: Depends on desired stories and cross-story constraints being complete.

### User Story Dependencies

- **US1 (P1)**: Start after foundational persistence/repository work; no dependency on later stories.
- **US2 (P2)**: Requires active reservations to validate end-to-end, so implement after US1 for normal flow.
- **US3 (P3)**: Requires reservation records from US1; consumed-reservation edge case benefits from US2.
- **US4 (P4)**: Requires movement context fields from Phase 2; can proceed in parallel with US1-US3 after repository support.

### Within Each User Story

- Write or update tests first and verify they fail.
- Update domain/repository contracts before use cases.
- Update use cases before controllers and OpenAPI.
- Validate each story at its checkpoint before moving to the next priority.

---

## Parallel Opportunities

- Foundational tests T012 and T013 can run in parallel after T003-T009 design is understood.
- US1 tests T014-T017 can be written in parallel because they target separate scenarios and test methods.
- US2 tests T025-T027 can be written in parallel.
- US3 tests T033-T036 can be written in parallel across reject/cancel paths.
- US4 tests T044-T045 can be written in parallel.
- Javadocs T056-T058 can be completed in parallel after implementation stabilizes.

## Parallel Example: User Story 1

```text
Task: "T014 [P] [US1] Add approval-reserves-stock and insufficient-stock tests in src/test/java/com/postech/workshop_service/application/usecases/AprovarOrcamentoUseCaseTest.java"
Task: "T015 [P] [US1] Add multi-location highest-balance allocation test in src/test/java/com/postech/workshop_service/application/usecases/AprovarOrcamentoUseCaseTest.java"
Task: "T016 [P] [US1] Add duplicate-approval reservation prevention test in src/test/java/com/postech/workshop_service/application/usecases/AprovarOrcamentoUseCaseTest.java"
Task: "T017 [P] [US1] Add concurrent approval integration test for limited stock in src/test/java/com/postech/workshop_service/api/controllers/OrcamentoControllerIT.java"
```

## Parallel Example: User Story 4

```text
Task: "T044 [P] [US4] Add use case test for listing stock movements by OS in src/test/java/com/postech/workshop_service/application/usecases/ListarMovimentacoesEstoquePorOrdemServicoUseCaseTest.java"
Task: "T045 [P] [US4] Add controller integration test for GET movements by OS in src/test/java/com/postech/workshop_service/api/controllers/EstoqueControllerIT.java"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1 and Phase 2.
2. Complete Phase 3 tasks T014-T024.
3. Validate approval with sufficient stock, insufficient stock, duplicate approval and concurrent approval.
4. Stop and demo reservation at approval before adding consumption/release.

### Incremental Delivery

1. Foundation: movement context and repository queries.
2. US1: reservation at approval.
3. US2: consumption at execution start.
4. US3: release at reject/cancel.
5. US4: movement audit by OS.
6. Cross-story: manual movement restrictions and full regression.

### Suggested MVP Scope

US1 only: approving a budget reserves stock atomically and blocks insufficient or concurrent approvals.

## Notes

- Keep code names and messages in pt-BR, following AGENTS.md.
- Keep business rules in domain/application layers, not controllers.
- Preserve transaction boundaries around OS, budget, stock and movement updates.
- Keep unrelated dirty worktree changes out of this feature.

# Tasks: Ciclo de Execucao da Ordem de Servico

**Input**: Design documents from `/specs/008-os-execution-cycle/`
**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/

**Tests**: A especificacao exige testes para transicoes validas, bloqueios, autorizacao, consulta de historico e fluxo completo.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Preparar os artefatos de schema e contratos compartilhados da feature

- [ ] T001 Revisar e alinhar os artefatos da feature em specs/008-os-execution-cycle/spec.md, specs/008-os-execution-cycle/plan.md, specs/008-os-execution-cycle/data-model.md e specs/008-os-execution-cycle/contracts/README.md
- [ ] T002 Criar migration dos timestamps do ciclo da OS em src/main/resources/db/migration/V0.20260507100000__add_execution_cycle_to_ordens_servico.sql
- [ ] T003 Criar migration da tabela de historico de status em src/main/resources/db/migration/V0.20260507101000__create_table_historico_status_os.sql

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Dominio, persistencia e suporte compartilhado que bloqueiam todas as user stories

**CRITICAL**: No user story work can begin until this phase is complete

- [ ] T004 Atualizar os estados do ciclo da OS em src/main/java/com/postech/workshop_service/domain/entities/StatusOrdemServico.java
- [ ] T005 Atualizar o agregado da OS com timestamps e transicoes em src/main/java/com/postech/workshop_service/domain/entities/OrdemServico.java
- [ ] T006 [P] Atualizar os testes de maquina de estados da OS em src/test/java/com/postech/workshop_service/domain/entities/OrdemServicoTest.java
- [ ] T007 [P] Criar a entidade de dominio do historico em src/main/java/com/postech/workshop_service/domain/entities/HistoricoStatusOrdemServico.java
- [ ] T008 [P] Criar o contrato de repositorio de historico em src/main/java/com/postech/workshop_service/domain/repositories/HistoricoStatusOrdemServicoRepository.java
- [ ] T009 [P] Criar a entidade JPA de historico em src/main/java/com/postech/workshop_service/infrastructure/persistence/entities/HistoricoStatusOrdemServicoJpaEntity.java
- [ ] T010 [P] Criar o repositorio Spring Data de historico em src/main/java/com/postech/workshop_service/infrastructure/persistence/repositories/JpaHistoricoStatusOrdemServicoRepository.java
- [ ] T011 [P] Criar o mapper de historico em src/main/java/com/postech/workshop_service/infrastructure/persistence/mappers/HistoricoStatusOrdemServicoMapper.java
- [ ] T012 Implementar o adaptador de persistencia de historico em src/main/java/com/postech/workshop_service/infrastructure/persistence/repositories/HistoricoStatusOrdemServicoRepositoryImpl.java
- [ ] T013 Atualizar a entidade JPA e mapper da OS com timestamps em src/main/java/com/postech/workshop_service/infrastructure/persistence/entities/OrdemServicoJpaEntity.java e src/main/java/com/postech/workshop_service/infrastructure/persistence/mappers/OrdemServicoMapper.java
- [ ] T014 [P] Criar DTO de resposta de historico em src/main/java/com/postech/workshop_service/api/dtos/HistoricoStatusOrdemServicoResponse.java
- [ ] T015 Criar componente para obter responsavel autenticado em src/main/java/com/postech/workshop_service/application/usecases/BuscarResponsavelTransicaoUseCase.java
- [ ] T016 Criar componente de registro de historico em src/main/java/com/postech/workshop_service/application/usecases/RegistrarHistoricoStatusOrdemServicoUseCase.java
- [ ] T017 [P] Criar testes do registro de historico em src/test/java/com/postech/workshop_service/application/usecases/RegistrarHistoricoStatusOrdemServicoUseCaseTest.java
- [ ] T018 [P] Criar testes do repositorio de historico em src/test/java/com/postech/workshop_service/infrastructure/persistence/repositories/HistoricoStatusOrdemServicoRepositoryImplIT.java

**Checkpoint**: Foundation ready - user story implementation can now begin

---

## Phase 3: User Story 1 - Iniciar execucao da ordem aprovada (Priority: P1) MVP

**Goal**: Permitir que mecanico ou administrador inicie a execucao de uma OS em `AGUARDANDO_EXECUCAO`, registrando timestamp e historico.

**Independent Test**: Com uma OS aguardando execucao, iniciar a execucao deve mudar para `EM_EXECUCAO`, preencher `dataInicioExecucao` e criar historico com responsavel.

### Tests for User Story 1

- [ ] T019 [P] [US1] Criar testes do caso de uso de inicio de execucao em src/test/java/com/postech/workshop_service/application/usecases/IniciarExecucaoUseCaseTest.java
- [ ] T020 [P] [US1] Expandir testes de dominio para inicio de execucao valido e bloqueios em src/test/java/com/postech/workshop_service/domain/entities/OrdemServicoTest.java
- [ ] T021 [P] [US1] Criar testes de controller para PATCH iniciar execucao e autorizacao em src/test/java/com/postech/workshop_service/api/controllers/OrdemServicoControllerIT.java

### Implementation for User Story 1

- [ ] T022 [US1] Implementar o caso de uso de inicio de execucao em src/main/java/com/postech/workshop_service/application/usecases/IniciarExecucaoUseCase.java
- [ ] T023 [US1] Atualizar OrdemServicoController com endpoint de inicio de execucao em src/main/java/com/postech/workshop_service/api/controllers/OrdemServicoController.java
- [ ] T024 [US1] Atualizar OrdemServicoResponse com dataInicioExecucao em src/main/java/com/postech/workshop_service/api/dtos/OrdemServicoResponse.java
- [ ] T025 [US1] Atualizar OpenAPI do endpoint iniciar execucao em src/main/java/com/postech/workshop_service/api/controllers/openapi.yaml

**Checkpoint**: User Story 1 funcional e testavel independentemente

---

## Phase 4: User Story 2 - Finalizar execucao tecnica (Priority: P1)

**Goal**: Permitir que mecanico ou administrador finalize uma OS em `EM_EXECUCAO`, registrando timestamp e historico.

**Independent Test**: Com uma OS em execucao, finalizar deve mudar para `FINALIZADA`, preencher `dataFinalizacao` e criar historico com responsavel.

### Tests for User Story 2

- [ ] T026 [P] [US2] Criar testes do caso de uso de finalizacao em src/test/java/com/postech/workshop_service/application/usecases/FinalizarExecucaoUseCaseTest.java
- [ ] T027 [P] [US2] Expandir testes de dominio para finalizacao valida e bloqueios em src/test/java/com/postech/workshop_service/domain/entities/OrdemServicoTest.java
- [ ] T028 [P] [US2] Criar testes de controller para PATCH finalizar e autorizacao em src/test/java/com/postech/workshop_service/api/controllers/OrdemServicoControllerIT.java

### Implementation for User Story 2

- [ ] T029 [US2] Implementar o caso de uso de finalizacao em src/main/java/com/postech/workshop_service/application/usecases/FinalizarExecucaoUseCase.java
- [ ] T030 [US2] Atualizar OrdemServicoController com endpoint de finalizacao em src/main/java/com/postech/workshop_service/api/controllers/OrdemServicoController.java
- [ ] T031 [US2] Atualizar OrdemServicoResponse com dataFinalizacao em src/main/java/com/postech/workshop_service/api/dtos/OrdemServicoResponse.java
- [ ] T032 [US2] Atualizar OpenAPI do endpoint finalizar em src/main/java/com/postech/workshop_service/api/controllers/openapi.yaml

**Checkpoint**: User Story 2 funcional e testavel independentemente

---

## Phase 5: User Story 3 - Registrar entrega do veiculo (Priority: P2)

**Goal**: Permitir que atendente ou administrador registre a entrega de uma OS `FINALIZADA`, encerrando operacionalmente o atendimento.

**Independent Test**: Com uma OS finalizada, entregar deve mudar para `ENTREGUE`, preencher `dataEntrega` e criar historico com responsavel.

### Tests for User Story 3

- [ ] T033 [P] [US3] Criar testes do caso de uso de entrega em src/test/java/com/postech/workshop_service/application/usecases/EntregarVeiculoUseCaseTest.java
- [ ] T034 [P] [US3] Expandir testes de dominio para entrega valida e bloqueios em src/test/java/com/postech/workshop_service/domain/entities/OrdemServicoTest.java
- [ ] T035 [P] [US3] Criar testes de controller para PATCH entregar e autorizacao em src/test/java/com/postech/workshop_service/api/controllers/OrdemServicoControllerIT.java

### Implementation for User Story 3

- [ ] T036 [US3] Implementar o caso de uso de entrega em src/main/java/com/postech/workshop_service/application/usecases/EntregarVeiculoUseCase.java
- [ ] T037 [US3] Atualizar OrdemServicoController com endpoint de entrega em src/main/java/com/postech/workshop_service/api/controllers/OrdemServicoController.java
- [ ] T038 [US3] Atualizar OrdemServicoResponse com dataEntrega em src/main/java/com/postech/workshop_service/api/dtos/OrdemServicoResponse.java
- [ ] T039 [US3] Atualizar OpenAPI do endpoint entregar em src/main/java/com/postech/workshop_service/api/controllers/openapi.yaml

**Checkpoint**: User Story 3 funcional e testavel independentemente

---

## Phase 6: User Story 4 - Auditar historico de status da ordem (Priority: P2)

**Goal**: Permitir que administrador, mecanico ou atendente consulte a linha do tempo de status da OS em ordem cronologica.

**Independent Test**: Executar fluxo completo da OS, consultar historico e verificar transicoes em ordem cronologica com status anterior, status novo, data e responsavel.

### Tests for User Story 4

- [ ] T040 [P] [US4] Criar testes do caso de uso de consulta de historico em src/test/java/com/postech/workshop_service/application/usecases/ConsultarHistoricoOrdemServicoUseCaseTest.java
- [ ] T041 [P] [US4] Criar testes de controller para GET historico-status e autorizacao em src/test/java/com/postech/workshop_service/api/controllers/OrdemServicoControllerIT.java
- [ ] T042 [P] [US4] Criar teste de fluxo completo com historico cronologico em src/test/java/com/postech/workshop_service/api/controllers/OrdemServicoControllerIT.java
- [ ] T043 [P] [US4] Criar teste de ausencia de backfill retroativo em src/test/java/com/postech/workshop_service/infrastructure/persistence/repositories/HistoricoStatusOrdemServicoRepositoryImplIT.java

### Implementation for User Story 4

- [ ] T044 [US4] Implementar consulta por OS no contrato de historico em src/main/java/com/postech/workshop_service/domain/repositories/HistoricoStatusOrdemServicoRepository.java
- [ ] T045 [US4] Implementar consulta cronologica no repositorio JPA de historico em src/main/java/com/postech/workshop_service/infrastructure/persistence/repositories/JpaHistoricoStatusOrdemServicoRepository.java e src/main/java/com/postech/workshop_service/infrastructure/persistence/repositories/HistoricoStatusOrdemServicoRepositoryImpl.java
- [ ] T046 [US4] Implementar o caso de uso de consulta de historico em src/main/java/com/postech/workshop_service/application/usecases/ConsultarHistoricoOrdemServicoUseCase.java
- [ ] T047 [US4] Atualizar fluxos existentes para registrar historico em src/main/java/com/postech/workshop_service/application/usecases/EncerrarComposicaoTecnicaUseCase.java, src/main/java/com/postech/workshop_service/application/usecases/AprovarOrcamentoUseCase.java, src/main/java/com/postech/workshop_service/application/usecases/RejeitarOrcamentoUseCase.java e src/main/java/com/postech/workshop_service/application/usecases/CancelarOrcamentoUseCase.java
- [ ] T048 [US4] Atualizar OrdemServicoController com endpoint de consulta de historico em src/main/java/com/postech/workshop_service/api/controllers/OrdemServicoController.java
- [ ] T049 [US4] Atualizar OpenAPI com endpoint e schema de historico em src/main/java/com/postech/workshop_service/api/controllers/openapi.yaml

**Checkpoint**: User Story 4 funcional e testavel independentemente

---

## Phase 7: Polish & Cross-Cutting Concerns

**Purpose**: Consolidar consistencia, documentacao e validacao final da feature

- [ ] T050 [P] Atualizar quickstart da feature com comandos finais em specs/008-os-execution-cycle/quickstart.md
- [ ] T051 [P] Atualizar contratos da feature se os nomes finais de DTOs/endpoints mudarem em specs/008-os-execution-cycle/contracts/README.md
- [ ] T052 Executar formatacao Spring Java Format para os arquivos alterados com mvn spring-javaformat:apply
- [ ] T053 Executar testes focados da feature com mvn "-Dtest=OrdemServicoTest,IniciarExecucaoUseCaseTest,FinalizarExecucaoUseCaseTest,EntregarVeiculoUseCaseTest,ConsultarHistoricoOrdemServicoUseCaseTest,OrdemServicoControllerIT,HistoricoStatusOrdemServicoRepositoryImplIT" test
- [ ] T054 Executar suite completa com mvn test

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Story 1 (Phase 3)**: Depends on Foundational phase completion
- **User Story 2 (Phase 4)**: Depends on Foundational phase completion and uses the state introduced by US1
- **User Story 3 (Phase 5)**: Depends on Foundational phase completion and uses the state produced by US2
- **User Story 4 (Phase 6)**: Depends on historico foundation and is most valuable after US1-US3
- **Polish (Phase 7)**: Depends on all desired user stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: Can start after Foundational - MVP of post-approval cycle
- **User Story 2 (P1)**: Can start after Foundational, but full business flow follows US1
- **User Story 3 (P2)**: Depends on US2 for natural flow from `FINALIZADA` to `ENTREGUE`
- **User Story 4 (P2)**: Can implement read path after Foundational, but full validation depends on US1-US3

### Within Each User Story

- Tests MUST be written and fail before implementation
- Domain behavior before use case integration
- Use cases before controller endpoints
- DTO/OpenAPI updates after endpoint behavior is defined
- Story complete before moving to production validation

### Parallel Opportunities

- T006, T007, T008, T009, T010, T011, T014, T017 and T018 can run in parallel after T004/T005 are understood
- T019, T020 and T021 can run in parallel for US1
- T026, T027 and T028 can run in parallel for US2
- T033, T034 and T035 can run in parallel for US3
- T040, T041, T042 and T043 can run in parallel for US4 after foundational historico exists
- T050 and T051 can run in parallel during polish

---

## Parallel Example: User Story 1

```bash
# Launch all tests for User Story 1 together:
Task: "Criar testes do caso de uso de inicio de execucao em src/test/java/com/postech/workshop_service/application/usecases/IniciarExecucaoUseCaseTest.java"
Task: "Expandir testes de dominio para inicio de execucao valido e bloqueios em src/test/java/com/postech/workshop_service/domain/entities/OrdemServicoTest.java"
Task: "Criar testes de controller para PATCH iniciar execucao e autorizacao em src/test/java/com/postech/workshop_service/api/controllers/OrdemServicoControllerIT.java"
```

## Parallel Example: User Story 4

```bash
# Launch read-path tests together:
Task: "Criar testes do caso de uso de consulta de historico em src/test/java/com/postech/workshop_service/application/usecases/ConsultarHistoricoOrdemServicoUseCaseTest.java"
Task: "Criar testes de controller para GET historico-status e autorizacao em src/test/java/com/postech/workshop_service/api/controllers/OrdemServicoControllerIT.java"
Task: "Criar teste de ausencia de backfill retroativo em src/test/java/com/postech/workshop_service/infrastructure/persistence/repositories/HistoricoStatusOrdemServicoRepositoryImplIT.java"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational
3. Complete Phase 3: User Story 1
4. STOP and validate `IniciarExecucaoUseCaseTest`, `OrdemServicoTest` and the relevant `OrdemServicoControllerIT` cases
5. Demonstrate OS moving from `AGUARDANDO_EXECUCAO` to `EM_EXECUCAO`

### Incremental Delivery

1. Complete Setup + Foundational -> history-capable OS model
2. Add User Story 1 -> execution start works
3. Add User Story 2 -> technical completion works
4. Add User Story 3 -> vehicle delivery works
5. Add User Story 4 -> audit/history query works
6. Run Phase 7 validation

### Parallel Team Strategy

With multiple developers:

1. Team completes Setup + Foundational together
2. Once Foundational is done:
   - Developer A: US1 and shared controller wiring
   - Developer B: US2 and domain transition tests
   - Developer C: US4 repository/read path
3. Integrate US3 after US2 transition behavior is stable

---

## Notes

- [P] tasks = different files, no dependency on incomplete tasks
- [US1], [US2], [US3] and [US4] map directly to user stories in spec.md
- The feature intentionally excludes stock reservation/withdrawal and KPI calculation
- No retroactive history backfill should be implemented
- Keep business transition rules in the domain, not in controllers

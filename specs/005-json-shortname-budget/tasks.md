# Tasks: Encerramento de Composicao Tecnica e Fluxo de Orcamento

**Input**: Design documents from `/specs/005-json-shortname-budget/`
**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/

**Tests**: A especificacao e o pedido do usuario exigem testes unitarios para os casos de uso e atualizacao dos testes de dominio impactados.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Preparar os artefatos base da feature para persistencia, mapeamento e validacao automatizada

- [ ] T001 Revisar e alinhar o contexto da feature em specs/005-json-shortname-budget/plan.md, specs/005-json-shortname-budget/data-model.md e specs/005-json-shortname-budget/quickstart.md antes da implementacao
- [ ] T002 Criar a migration base da ordem de servico em src/main/resources/db/migration/V0.20260429100000__create_table_ordens_servico.sql
- [ ] T003 [P] Criar a migration base do orcamento em src/main/resources/db/migration/V0.20260429101000__create_table_orcamentos.sql

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Infraestrutura e dominio compartilhados que bloqueiam todas as user stories

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [ ] T004 Atualizar os enums de estados em src/main/java/com/postech/workshop_service/domain/entities/StatusOrdemServico.java e src/main/java/com/postech/workshop_service/domain/entities/StatusOrcamento.java para refletir o fluxo do MVP
- [ ] T005 [P] Criar o enum de classificacao dos itens tecnicos em src/main/java/com/postech/workshop_service/domain/entities/TipoItemComposicaoTecnica.java
- [ ] T006 [P] Criar o objeto de dominio de item de composicao em src/main/java/com/postech/workshop_service/domain/entities/ItemComposicaoTecnica.java
- [ ] T007 Evoluir o agregado da ordem para suportar itens e transicoes do novo fluxo em src/main/java/com/postech/workshop_service/domain/entities/OrdemServico.java
- [ ] T008 Evoluir o agregado do orcamento para fotografia de itens e transicoes alinhadas ao MVP em src/main/java/com/postech/workshop_service/domain/entities/Orcamento.java e src/main/java/com/postech/workshop_service/domain/entities/ItemOrcamento.java
- [ ] T009 [P] Criar os contratos de repositorio do dominio em src/main/java/com/postech/workshop_service/domain/repositories/OrdemServicoRepository.java e src/main/java/com/postech/workshop_service/domain/repositories/OrcamentoRepository.java
- [ ] T010 [P] Criar os contratos de notificacao em src/main/java/com/postech/workshop_service/application/usecases/ClienteNotificationService.java e src/main/java/com/postech/workshop_service/application/usecases/MecanicoNotificationService.java
- [ ] T011 [P] Criar as implementacoes de notificacao por log em src/main/java/com/postech/workshop_service/application/usecases/LogClienteNotificationService.java e src/main/java/com/postech/workshop_service/application/usecases/LogMecanicoNotificationService.java
- [ ] T012 [P] Criar as entidades JPA de ordem e itens da ordem em src/main/java/com/postech/workshop_service/infrastructure/persistence/entities/OrdemServicoJpaEntity.java e src/main/java/com/postech/workshop_service/infrastructure/persistence/entities/ItemComposicaoTecnicaJpaEntity.java
- [ ] T013 [P] Criar as entidades JPA de orcamento e itens do orcamento em src/main/java/com/postech/workshop_service/infrastructure/persistence/entities/OrcamentoJpaEntity.java e src/main/java/com/postech/workshop_service/infrastructure/persistence/entities/ItemOrcamentoJpaEntity.java
- [ ] T014 [P] Criar os repositorios Spring Data em src/main/java/com/postech/workshop_service/infrastructure/persistence/repositories/JpaOrdemServicoRepository.java e src/main/java/com/postech/workshop_service/infrastructure/persistence/repositories/JpaOrcamentoRepository.java
- [ ] T015 [P] Criar os mapeadores MapStruct da feature em src/main/java/com/postech/workshop_service/infrastructure/persistence/mappers/OrdemServicoMapper.java e src/main/java/com/postech/workshop_service/infrastructure/persistence/mappers/OrcamentoMapper.java
- [ ] T016 Implementar as adaptacoes de persistencia do dominio em src/main/java/com/postech/workshop_service/infrastructure/persistence/repositories/OrdemServicoRepositoryImpl.java e src/main/java/com/postech/workshop_service/infrastructure/persistence/repositories/OrcamentoRepositoryImpl.java
- [ ] T017 Atualizar os testes de dominio compartilhados para o novo modelo em src/test/java/com/postech/workshop_service/domain/entities/OrdemServicoTest.java e src/test/java/com/postech/workshop_service/domain/entities/OrcamentoTest.java

**Checkpoint**: Foundation ready - user story implementation can now begin in parallel

---

## Phase 3: User Story 1 - Encerrar composicao tecnica e emitir proposta (Priority: P1) 🎯 MVP

**Goal**: Permitir que o mecanico encerre a composicao tecnica da ordem, gere um orcamento pendente, copie os itens da OS e notifique o cliente

**Independent Test**: Executar o caso de uso com uma ordem em composicao contendo ao menos um item e verificar a geracao do orcamento `PENDENTE_APROVACAO`, a transicao da ordem para `AGUARDANDO_RESPOSTA_CLIENTE`, a copia dos itens e a notificacao do cliente

### Tests for User Story 1

- [ ] T018 [P] [US1] Criar testes do caso de uso de encerramento da composicao em src/test/java/com/postech/workshop_service/application/usecases/EncerrarComposicaoTecnicaUseCaseTest.java
- [ ] T019 [P] [US1] Expandir os testes de dominio da ordem para validar bloqueio sem itens e encerramento valido em src/test/java/com/postech/workshop_service/domain/entities/OrdemServicoTest.java
- [ ] T020 [P] [US1] Expandir os testes de dominio do orcamento para validar envio para aprovacao e preservacao da fotografia em src/test/java/com/postech/workshop_service/domain/entities/OrcamentoTest.java

### Implementation for User Story 1

- [ ] T021 [US1] Implementar o caso de uso de encerramento da composicao em src/main/java/com/postech/workshop_service/application/usecases/EncerrarComposicaoTecnicaUseCase.java
- [ ] T022 [US1] Adicionar a consulta de orcamento pendente por ordem em src/main/java/com/postech/workshop_service/domain/repositories/OrcamentoRepository.java e src/main/java/com/postech/workshop_service/infrastructure/persistence/repositories/OrcamentoRepositoryImpl.java
- [ ] T023 [US1] Implementar a restricao de unicidade de orcamento pendente no repositorio Spring Data em src/main/java/com/postech/workshop_service/infrastructure/persistence/repositories/JpaOrcamentoRepository.java
- [ ] T024 [US1] Finalizar o mapeamento de itens da ordem e fotografia do orcamento em src/main/java/com/postech/workshop_service/infrastructure/persistence/mappers/OrdemServicoMapper.java e src/main/java/com/postech/workshop_service/infrastructure/persistence/mappers/OrcamentoMapper.java

**Checkpoint**: At this point, User Story 1 should be fully functional and testable independently

---

## Phase 4: User Story 2 - Aprovar ou rejeitar proposta do cliente (Priority: P2)

**Goal**: Permitir que o atendente registre a aprovacao ou rejeicao do orcamento pendente e sincronize corretamente os estados da ordem e do orcamento

**Independent Test**: Executar os casos de uso de aprovacao e rejeicao com um orcamento `PENDENTE_APROVACAO` vinculado a uma ordem em `AGUARDANDO_RESPOSTA_CLIENTE` e validar as transicoes finais, os bloqueios por estado invalido e a notificacao ao mecanico

### Tests for User Story 2

- [ ] T025 [P] [US2] Criar testes do caso de uso de aprovacao em src/test/java/com/postech/workshop_service/application/usecases/AprovarOrcamentoUseCaseTest.java
- [ ] T026 [P] [US2] Criar testes do caso de uso de rejeicao em src/test/java/com/postech/workshop_service/application/usecases/RejeitarOrcamentoUseCaseTest.java
- [ ] T027 [P] [US2] Ajustar os testes de dominio da ordem para retorno a composicao e ida para aguardando execucao em src/test/java/com/postech/workshop_service/domain/entities/OrdemServicoTest.java
- [ ] T028 [P] [US2] Ajustar os testes de dominio do orcamento para aprovacao e rejeicao apenas a partir de pendente em src/test/java/com/postech/workshop_service/domain/entities/OrcamentoTest.java

### Implementation for User Story 2

- [ ] T029 [US2] Implementar o caso de uso de aprovacao do orcamento em src/main/java/com/postech/workshop_service/application/usecases/AprovarOrcamentoUseCase.java
- [ ] T030 [US2] Implementar o caso de uso de rejeicao do orcamento em src/main/java/com/postech/workshop_service/application/usecases/RejeitarOrcamentoUseCase.java
- [ ] T031 [US2] Ajustar o dominio da ordem e do orcamento para suportar aprovacao e rejeicao do novo fluxo em src/main/java/com/postech/workshop_service/domain/entities/OrdemServico.java e src/main/java/com/postech/workshop_service/domain/entities/Orcamento.java

**Checkpoint**: At this point, User Stories 1 AND 2 should both work independently

---

## Phase 5: User Story 3 - Cancelar proposta e encerrar atendimento (Priority: P3)

**Goal**: Permitir que o atendente cancele um orcamento pendente e encerre a ordem de servico de forma consistente

**Independent Test**: Executar o caso de uso de cancelamento com um orcamento `PENDENTE_APROVACAO` vinculado a uma ordem em `AGUARDANDO_RESPOSTA_CLIENTE` e verificar o orcamento `CANCELADO`, a ordem `CANCELADA`, os bloqueios por estado invalido e a notificacao ao mecanico

### Tests for User Story 3

- [ ] T032 [P] [US3] Criar testes do caso de uso de cancelamento em src/test/java/com/postech/workshop_service/application/usecases/CancelarOrcamentoUseCaseTest.java
- [ ] T033 [P] [US3] Ajustar os testes de dominio da ordem para cancelamento apenas em aguardando resposta do cliente em src/test/java/com/postech/workshop_service/domain/entities/OrdemServicoTest.java
- [ ] T034 [P] [US3] Ajustar os testes de dominio do orcamento para cancelamento apenas a partir de pendente em src/test/java/com/postech/workshop_service/domain/entities/OrcamentoTest.java

### Implementation for User Story 3

- [ ] T035 [US3] Implementar o caso de uso de cancelamento do orcamento em src/main/java/com/postech/workshop_service/application/usecases/CancelarOrcamentoUseCase.java
- [ ] T036 [US3] Ajustar o comportamento de cancelamento no dominio em src/main/java/com/postech/workshop_service/domain/entities/OrdemServico.java e src/main/java/com/postech/workshop_service/domain/entities/Orcamento.java

**Checkpoint**: All user stories should now be independently functional

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Consolidar consistencia, cobertura e validacao final da feature

- [ ] T037 [P] Atualizar a documentacao de contratos internos da feature em specs/005-json-shortname-budget/contracts/README.md
- [ ] T038 Validar o fluxo completo descrito em specs/005-json-shortname-budget/quickstart.md e ajustar o texto se necessario
- [ ] T039 Executar a suite de testes da feature e registrar os resultados esperados com mvn test a partir de specs/005-json-shortname-budget/quickstart.md

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Stories (Phase 3+)**: All depend on Foundational phase completion
- **Polish (Phase 6)**: Depends on all desired user stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: Can start after Foundational (Phase 2) - define o MVP da feature
- **User Story 2 (P2)**: Depends on US1 because requer um orcamento pendente gerado pelo fluxo de encerramento da composicao
- **User Story 3 (P3)**: Depends on US1 because requer um orcamento pendente gerado pelo fluxo de encerramento da composicao

### Within Each User Story

- Tests MUST be written and fail before implementation
- Domain adjustments required by the story should precede final use case integration
- Repository/query changes required by the story should precede persistence-dependent assertions
- Story complete before moving to production validation

### Parallel Opportunities

- T003 can run in parallel with T002 after migration naming is defined
- T005, T006, T009, T010, T011, T012, T013, T014 and T015 can run in parallel within Phase 2
- T018, T019 and T020 can run in parallel for US1
- T025, T026, T027 and T028 can run in parallel for US2
- T032, T033 and T034 can run in parallel for US3

---

## Parallel Example: User Story 1

```bash
# Launch all tests for User Story 1 together:
Task: "Criar testes do caso de uso de encerramento da composicao em src/test/java/com/postech/workshop_service/application/usecases/EncerrarComposicaoTecnicaUseCaseTest.java"
Task: "Expandir os testes de dominio da ordem para validar bloqueio sem itens e encerramento valido em src/test/java/com/postech/workshop_service/domain/entities/OrdemServicoTest.java"
Task: "Expandir os testes de dominio do orcamento para validar envio para aprovacao e preservacao da fotografia em src/test/java/com/postech/workshop_service/domain/entities/OrcamentoTest.java"

# Launch shared infrastructure work in parallel after enum alignment:
Task: "Criar os contratos de repositorio do dominio em src/main/java/com/postech/workshop_service/domain/repositories/OrdemServicoRepository.java e src/main/java/com/postech/workshop_service/domain/repositories/OrcamentoRepository.java"
Task: "Criar os contratos de notificacao em src/main/java/com/postech/workshop_service/application/usecases/ClienteNotificationService.java e src/main/java/com/postech/workshop_service/application/usecases/MecanicoNotificationService.java"
Task: "Criar as entidades JPA de ordem e itens da ordem em src/main/java/com/postech/workshop_service/infrastructure/persistence/entities/OrdemServicoJpaEntity.java e src/main/java/com/postech/workshop_service/infrastructure/persistence/entities/ItemComposicaoTecnicaJpaEntity.java"
Task: "Criar as entidades JPA de orcamento e itens do orcamento em src/main/java/com/postech/workshop_service/infrastructure/persistence/entities/OrcamentoJpaEntity.java e src/main/java/com/postech/workshop_service/infrastructure/persistence/entities/ItemOrcamentoJpaEntity.java"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational
3. Complete Phase 3: User Story 1
4. **STOP and VALIDATE**: Executar os testes de `EncerrarComposicaoTecnicaUseCase` e os testes de dominio impactados
5. Validar que a OS gera exatamente um orcamento `PENDENTE_APROVACAO` com fotografia imutavel dos itens

### Incremental Delivery

1. Complete Setup + Foundational → foundation ready
2. Add User Story 1 → test independently → MVP funcional
3. Add User Story 2 → test independently → aprovacao e rejeicao funcionais
4. Add User Story 3 → test independently → cancelamento funcional
5. Execute Phase 6 para consolidacao final

### Parallel Team Strategy

With multiple developers:

1. Team completes Setup + Foundational together
2. Once Foundational is done:
   - Developer A: testes e implementacao de US1
   - Developer B: testes e implementacao de US2
   - Developer C: testes e implementacao de US3
3. Integrate story-specific repository and domain adjustments with priority on US1 first

---

## Notes

- [P] tasks = different files, no dependencies
- [US1], [US2] e [US3] mapeiam diretamente para as user stories da spec
- Cada user story fica independentemente validavel a partir de seus testes de caso de uso
- A feature nao inclui endpoint, PDF, pagamento, aprovacao parcial nem estoque
- O fluxo depende de persistencia nova para ordem e orcamento; nao pular a Fase 2

# Tasks: Entidade Base de Ordem de Servico

**Input**: Design documents from `/specs/003-ordem-servico-base/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/, quickstart.md

**Tests**: Testes unitarios sao obrigatorios nesta feature porque a especificacao e a constituicao exigem cobertura para todos os metodos publicos da entidade de dominio.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Preparar os artefatos da feature para a implementacao de dominio sem alterar camadas fora do escopo

- [X] T001 Revisar e alinhar os artefatos de design da feature em `specs/003-ordem-servico-base/spec.md`, `specs/003-ordem-servico-base/plan.md`, `specs/003-ordem-servico-base/data-model.md` e `specs/003-ordem-servico-base/quickstart.md`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Estruturas de dominio que bloqueiam todas as user stories

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [X] T002 [P] Criar o enum `StatusOrdemServico` em `src/main/java/com/postech/workshop_service/domain/entities/StatusOrdemServico.java`
- [X] T003 Criar a estrutura inicial da entidade `OrdemServico` herdando de `EntidadeBase` em `src/main/java/com/postech/workshop_service/domain/entities/OrdemServico.java`
- [X] T004 [P] Criar a classe de testes unitarios da entidade em `src/test/java/com/postech/workshop_service/domain/entities/OrdemServicoTest.java`

**Checkpoint**: Foundation ready - user story implementation can now begin in parallel

---

## Phase 3: User Story 1 - Definir uma ordem de servico rastreavel (Priority: P1) 🎯 MVP

**Goal**: Disponibilizar a entidade base `OrdemServico` com identidade propria e vinculos obrigatorios para cliente e veiculo

**Independent Test**: Validar por teste unitario que uma nova `OrdemServico` contem `id`, `idCliente`, `idVeiculo` e nasce com status inicial previsto sem depender de controller, use case ou persistencia

### Tests for User Story 1

- [X] T005 [US1] Implementar testes de criacao e invariantes obrigatorias em `src/test/java/com/postech/workshop_service/domain/entities/OrdemServicoTest.java`

### Implementation for User Story 1

- [X] T006 [US1] Implementar construtor de criacao com `idCliente` e `idVeiculo` obrigatorios em `src/main/java/com/postech/workshop_service/domain/entities/OrdemServico.java`
- [X] T007 [US1] Implementar construtor de reconstituicao com campos de auditoria herdados de `EntidadeBase` em `src/main/java/com/postech/workshop_service/domain/entities/OrdemServico.java`
- [X] T008 [US1] Adicionar Javadoc e validacoes de nulos para identidade e vinculos da entidade em `src/main/java/com/postech/workshop_service/domain/entities/OrdemServico.java`

**Checkpoint**: At this point, User Story 1 should be fully functional and testable independently

---

## Phase 4: User Story 2 - Controlar estados iniciais da ordem (Priority: P2)

**Goal**: Garantir o conjunto inicial de status validos e o nascimento da entidade sempre com status `RECEBIDA`

**Independent Test**: Validar por teste unitario que novas ordens sempre iniciam com `RECEBIDA` e que a entidade reconhece apenas os estados definidos em `StatusOrdemServico`

### Tests for User Story 2

- [X] T009 [US2] Implementar testes para status inicial e cobertura dos status suportados em `src/test/java/com/postech/workshop_service/domain/entities/OrdemServicoTest.java`

### Implementation for User Story 2

- [X] T010 [US2] Definir os valores `RECEBIDA`, `AGUARDANDO_APROVACAO_ORCAMENTO`, `EM_EXECUCAO`, `CANCELADA` e `FINALIZADA` em `src/main/java/com/postech/workshop_service/domain/entities/StatusOrdemServico.java`
- [X] T011 [US2] Garantir atribuicao automatica de `StatusOrdemServico.RECEBIDA` na criacao da entidade em `src/main/java/com/postech/workshop_service/domain/entities/OrdemServico.java`
- [X] T012 [US2] Ajustar a reconstituicao da entidade para aceitar apenas status nao nulo e consistente com o enum em `src/main/java/com/postech/workshop_service/domain/entities/OrdemServico.java`

**Checkpoint**: At this point, User Stories 1 AND 2 should both work independently

---

## Phase 5: User Story 3 - Aplicar regra de cancelamento (Priority: P3)

**Goal**: Encapsular a regra de cancelamento no dominio com consulta de cancelabilidade e bloqueio por erro de negocio

**Independent Test**: Validar por teste unitario que `podeSerCancelada()` retorna `true` apenas para `RECEBIDA` e `AGUARDANDO_APROVACAO_ORCAMENTO`, e que `cancelar()` altera para `CANCELADA` ou lanca `RegraDeNegocioException` sem mudar o estado

### Tests for User Story 3

- [X] T013 [US3] Implementar testes de `podeSerCancelada()` para estados permitidos e bloqueados em `src/test/java/com/postech/workshop_service/domain/entities/OrdemServicoTest.java`
- [X] T014 [US3] Implementar testes de `cancelar()` cobrindo sucesso, excecao e preservacao de estado em `src/test/java/com/postech/workshop_service/domain/entities/OrdemServicoTest.java`

### Implementation for User Story 3

- [X] T015 [US3] Implementar o metodo `podeSerCancelada()` em `src/main/java/com/postech/workshop_service/domain/entities/OrdemServico.java`
- [X] T016 [US3] Implementar o metodo `cancelar()` com uso de `RegraDeNegocioException` em `src/main/java/com/postech/workshop_service/domain/entities/OrdemServico.java`
- [X] T017 [US3] Atualizar `dataUltimaAtualizacao` ao cancelar com sucesso e adicionar Javadoc dos comportamentos publicos em `src/main/java/com/postech/workshop_service/domain/entities/OrdemServico.java`

**Checkpoint**: All user stories should now be independently functional

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Validacao final da entrega restrita ao dominio

- [X] T018 [P] Executar a suite de testes com `mvn test` a partir de `pom.xml`
- [X] T019 Revisar se nenhum controller, endpoint, repository, migration ou use case completo foi criado fora de `src/main/java/com/postech/workshop_service/domain/entities/` e `src/test/java/com/postech/workshop_service/domain/entities/`
- [X] T020 [P] Atualizar a documentacao de execucao da feature se necessario em `specs/003-ordem-servico-base/quickstart.md`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Stories (Phase 3+)**: All depend on Foundational phase completion
- **Polish (Phase 6)**: Depends on all desired user stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: Comeca apos a fase Foundational e estabelece a estrutura minima da entidade
- **User Story 2 (P2)**: Depende de US1 porque complementa a entidade com o conjunto inicial de status e o status de nascimento
- **User Story 3 (P3)**: Depende de US2 porque a regra de cancelamento opera sobre o conjunto final de status definido

### Within Each User Story

- Testes devem ser escritos antes ou em conjunto com a implementacao e precisam validar os metodos publicos da etapa
- Estrutura da entidade antes das regras de estado
- Regras de estado antes do comportamento de cancelamento
- Implementacao principal antes da validacao final com `mvn test`

### Parallel Opportunities

- `T002` e `T004` podem ocorrer em paralelo apos `T001`
- `T018` e `T020` podem ocorrer em paralelo apos a conclusao de `T019`

---

## Parallel Example: User Story 3

```bash
# Preparar a cobertura da regra de cancelamento em paralelo:
Task: "Implementar testes de podeSerCancelada() em src/test/java/com/postech/workshop_service/domain/entities/OrdemServicoTest.java"
Task: "Implementar testes de cancelar() em src/test/java/com/postech/workshop_service/domain/entities/OrdemServicoTest.java"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Completar Phase 1: Setup
2. Completar Phase 2: Foundational
3. Completar Phase 3: User Story 1
4. Validar a criacao da entidade e seus vinculos obrigatorios por testes unitarios

### Incremental Delivery

1. Foundation pronta com enum, entidade e classe de testes
2. Entregar US1 com identidade e vinculos obrigatorios
3. Entregar US2 com status iniciais e nascimento em `RECEBIDA`
4. Entregar US3 com cancelamento encapsulado no dominio
5. Executar validacao final e confirmar que o escopo permaneceu restrito ao dominio

### Parallel Team Strategy

1. Uma pessoa prepara `StatusOrdemServico` enquanto outra prepara a classe de testes apos a fundacao
2. Depois da fundacao, uma pessoa pode consolidar US1 e outra pode adiantar a massa de testes de US2/US3
3. A integracao final fica concentrada em `OrdemServico.java` e `OrdemServicoTest.java`

---

## Notes

- [P] tasks = different files, no dependencies
- [Story] label maps task to specific user story for traceability
- Cada user story permanece validavel sem controller, endpoint ou persistencia
- O MVP recomendado e a **User Story 1**
- Todas as tasks seguem o formato checklist com ID, labels e file path

# Tasks: Entidade de Dominio Orcamento

**Input**: Design documents from `/specs/004-orcamento-ordem-servico/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/, quickstart.md

**Tests**: Testes unitarios sao obrigatorios nesta feature porque a especificacao e a constituicao exigem cobertura para todos os metodos publicos e transicoes de dominio.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Preparar os artefatos da feature para a implementacao do agregado de dominio sem expandir o escopo para camadas externas

- [X] T001 Revisar e alinhar os artefatos de design da feature em `specs/004-orcamento-ordem-servico/spec.md`, `specs/004-orcamento-ordem-servico/plan.md`, `specs/004-orcamento-ordem-servico/data-model.md` e `specs/004-orcamento-ordem-servico/quickstart.md`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Estruturas de dominio compartilhadas que bloqueiam todas as user stories do orcamento

**CRITICAL**: No user story work can begin until this phase is complete

- [X] T002 [P] Criar o enum `StatusOrcamento` em `src/main/java/com/postech/workshop_service/domain/entities/StatusOrcamento.java`
- [X] T003 [P] Criar o enum `TipoOrcamento` em `src/main/java/com/postech/workshop_service/domain/entities/TipoOrcamento.java`
- [X] T004 [P] Criar o tipo de dominio `ItemOrcamento` em `src/main/java/com/postech/workshop_service/domain/entities/ItemOrcamento.java`
- [X] T005 Criar a estrutura inicial da entidade `Orcamento` herdando de `EntidadeBase` em `src/main/java/com/postech/workshop_service/domain/entities/Orcamento.java`
- [X] T006 [P] Criar a classe de testes unitarios `OrcamentoTest` em `src/test/java/com/postech/workshop_service/domain/entities/OrcamentoTest.java`
- [X] T007 Ajustar a base de `OrdemServico` para suportar entrada explicita em `EM_EXECUCAO` em `src/main/java/com/postech/workshop_service/domain/entities/OrdemServico.java`
- [X] T008 [P] Expandir `OrdemServicoTest` para cobrir a nova transicao para `EM_EXECUCAO` em `src/test/java/com/postech/workshop_service/domain/entities/OrdemServicoTest.java`

**Checkpoint**: Foundation ready - user story implementation can now begin in parallel

---

## Phase 3: User Story 1 - Registrar um orcamento rastreavel (Priority: P1) MVP

**Goal**: Disponibilizar a entidade `Orcamento` com identidade propria, ordem vinculada, valor, itens obrigatorios, tipo e status inicial valido

**Independent Test**: Validar por teste unitario que um novo `Orcamento` possui `id`, `idOrdemServico`, `valor`, pelo menos um item, `tipo` e nasce com status `CRIADO` sem depender de controller, repository ou persistencia

### Tests for User Story 1

- [X] T009 [US1] Implementar testes de criacao e invariantes obrigatorias de `Orcamento` em `src/test/java/com/postech/workshop_service/domain/entities/OrcamentoTest.java`

### Implementation for User Story 1

- [X] T010 [US1] Implementar os campos obrigatorios, construtor de criacao e inicializacao com status `CRIADO` em `src/main/java/com/postech/workshop_service/domain/entities/Orcamento.java`
- [X] T011 [US1] Implementar construtor de reconstituicao com campos herdados de `EntidadeBase` em `src/main/java/com/postech/workshop_service/domain/entities/Orcamento.java`
- [X] T012 [US1] Implementar validacoes de nulos, regra de minimo de 1 item e Javadoc dos comportamentos publicos em `src/main/java/com/postech/workshop_service/domain/entities/Orcamento.java` e `src/main/java/com/postech/workshop_service/domain/entities/ItemOrcamento.java`

**Checkpoint**: At this point, User Story 1 should be fully functional and testable independently

---

## Phase 4: User Story 2 - Submeter e decidir um orcamento (Priority: P2)

**Goal**: Encapsular no dominio o envio para aprovacao, a aprovacao e a rejeicao do orcamento, incluindo o efeito do servico original sobre a ordem

**Independent Test**: Validar por teste unitario que apenas `CRIADO` pode ser enviado para aprovacao, apenas `PENDENTE_APROVACAO` pode ser aprovado ou rejeitado e que a aprovacao do `SERVICO_ORIGINAL` coloca a `OrdemServico` em `EM_EXECUCAO`

### Tests for User Story 2

- [X] T013 [US2] Implementar testes de `enviarParaAprovacao()` cobrindo sucesso e bloqueio por status em `src/test/java/com/postech/workshop_service/domain/entities/OrcamentoTest.java`
- [X] T014 [US2] Implementar testes de `aprovar(OrdemServico)` e `rejeitar()` cobrindo transicoes validas e invalidas em `src/test/java/com/postech/workshop_service/domain/entities/OrcamentoTest.java`

### Implementation for User Story 2

- [X] T015 [US2] Definir os valores `CRIADO`, `PENDENTE_APROVACAO`, `APROVADO`, `REJEITADO` e `CANCELADO` em `src/main/java/com/postech/workshop_service/domain/entities/StatusOrcamento.java`
- [X] T016 [US2] Definir os valores `SERVICO_ORIGINAL` e `ADICAO_SERVICO` em `src/main/java/com/postech/workshop_service/domain/entities/TipoOrcamento.java`
- [X] T017 [US2] Implementar `enviarParaAprovacao()`, `aprovar(OrdemServico)` e `rejeitar()` com `RegraDeNegocioException` em `src/main/java/com/postech/workshop_service/domain/entities/Orcamento.java`
- [X] T018 [US2] Implementar o comportamento de `OrdemServico` para entrar em `EM_EXECUCAO` e integrá-lo na aprovacao do `SERVICO_ORIGINAL` em `src/main/java/com/postech/workshop_service/domain/entities/OrdemServico.java` e `src/main/java/com/postech/workshop_service/domain/entities/Orcamento.java`

**Checkpoint**: At this point, User Stories 1 AND 2 should both work independently

---

## Phase 5: User Story 3 - Coordenar o cancelamento do orcamento com a ordem (Priority: P3)

**Goal**: Aplicar a regra de cancelamento do orcamento respeitando status, tipo e possibilidade atual de cancelamento da `OrdemServico`

**Independent Test**: Validar por teste unitario que apenas `CRIADO`, `PENDENTE_APROVACAO` e `APROVADO` podem cancelar o orcamento, que somente `SERVICO_ORIGINAL` tenta cancelar a ordem e que uma ordem ja em `EM_EXECUCAO` permanece inalterada

### Tests for User Story 3

- [X] T019 [US3] Implementar testes de `cancelar(OrdemServico)` para cancelamento permitido do `SERVICO_ORIGINAL` e bloqueio por status do orcamento em `src/test/java/com/postech/workshop_service/domain/entities/OrcamentoTest.java`
- [X] T020 [US3] Implementar testes de `cancelar(OrdemServico)` cobrindo `ADICAO_SERVICO`, ordem nao cancelavel e preservacao de estado em `src/test/java/com/postech/workshop_service/domain/entities/OrcamentoTest.java`

### Implementation for User Story 3

- [X] T021 [US3] Implementar `cancelar(OrdemServico)` com `RegraDeNegocioException`, transicao para `CANCELADO` e atualizacao de `dataUltimaAtualizacao` em `src/main/java/com/postech/workshop_service/domain/entities/Orcamento.java`
- [X] T022 [US3] Integrar o cancelamento do `SERVICO_ORIGINAL` com `podeSerCancelada()` e `cancelar()` da `OrdemServico`, sem alterar a ordem para `ADICAO_SERVICO`, em `src/main/java/com/postech/workshop_service/domain/entities/Orcamento.java`
- [X] T023 [US3] Ajustar a cobertura de `OrdemServico` para garantir que a transicao para `EM_EXECUCAO` impeça cancelamento posterior via orcamento em `src/test/java/com/postech/workshop_service/domain/entities/OrdemServicoTest.java`

**Checkpoint**: All user stories should now be independently functional

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Validacao final da entrega restrita ao dominio

- [X] T024 [P] Executar a suite de testes com `mvn test` a partir de `pom.xml`
- [X] T025 Revisar se nenhum controller, endpoint, repository, migration ou use case completo do orcamento foi criado fora de `src/main/java/com/postech/workshop_service/domain/entities/` e `src/test/java/com/postech/workshop_service/domain/entities/`
- [X] T026 [P] Atualizar a documentacao de execucao da feature se necessario em `specs/004-orcamento-ordem-servico/quickstart.md`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Stories (Phase 3+)**: All depend on Foundational phase completion
- **Polish (Phase 6)**: Depends on all desired user stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: Comeca apos a fase Foundational e estabelece a estrutura minima do agregado `Orcamento`
- **User Story 2 (P2)**: Depende de US1 porque as transicoes de aprovacao e rejeicao operam sobre a entidade e seus enums completos
- **User Story 3 (P3)**: Depende de US2 porque o cancelamento do orcamento precisa respeitar os estados finais do fluxo de aprovacao e a transicao da `OrdemServico` para `EM_EXECUCAO`

### Within Each User Story

- Testes devem ser escritos antes ou em conjunto com a implementacao e precisam validar os metodos publicos da etapa
- Tipos e estrutura base antes das regras de transicao
- Transicoes de aprovacao antes da coordenacao de cancelamento com a ordem
- Implementacao principal antes da validacao final com `mvn test`

### Parallel Opportunities

- `T002`, `T003`, `T004` e `T006` podem ocorrer em paralelo apos `T001`
- `T007` e `T008` podem ocorrer em paralelo com a preparacao do agregado `Orcamento` ao fim da fundacao
- `T019` e `T020` podem ocorrer em paralelo dentro da cobertura de cancelamento
- `T024` e `T026` podem ocorrer em paralelo apos a conclusao de `T025`

---

## Parallel Example: User Story 3

```bash
# Preparar a cobertura da regra de cancelamento em paralelo:
Task: "Implementar testes de cancelar(OrdemServico) para SERVICO_ORIGINAL em src/test/java/com/postech/workshop_service/domain/entities/OrcamentoTest.java"
Task: "Implementar testes de cancelar(OrdemServico) para ADICAO_SERVICO e ordem nao cancelavel em src/test/java/com/postech/workshop_service/domain/entities/OrcamentoTest.java"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Completar Phase 1: Setup
2. Completar Phase 2: Foundational
3. Completar Phase 3: User Story 1
4. Validar a criacao do agregado e suas invariantes obrigatorias por testes unitarios

### Incremental Delivery

1. Foundation pronta com enums, item de orcamento, entidade base e ajustes em `OrdemServico`
2. Entregar US1 com estrutura minima, tipo, valor, itens e status inicial `CRIADO`
3. Entregar US2 com envio para aprovacao, aprovacao, rejeicao e avancar da ordem para `EM_EXECUCAO`
4. Entregar US3 com cancelamento coordenado entre `Orcamento` e `OrdemServico`
5. Executar validacao final e confirmar que o escopo permaneceu restrito ao dominio

### Parallel Team Strategy

1. Uma pessoa pode preparar `StatusOrcamento`, `TipoOrcamento` e `ItemOrcamento` enquanto outra cria a base de testes
2. Depois da fundacao, uma pessoa pode consolidar a criacao do agregado e outra adiantar a cobertura das transicoes de aprovacao
3. A integracao final fica concentrada em `Orcamento.java`, `OrdemServico.java` e seus testes

---

## Notes

- [P] tasks = different files, no dependencies
- [Story] label maps task to specific user story for traceability
- Cada user story permanece validavel sem controller, endpoint, repository ou persistencia dedicada
- O MVP recomendado e a **User Story 1**
- Todas as tasks seguem o formato checklist com ID, labels e file path

# Tasks: Atualizacao da Documentacao OpenAPI e README

**Input**: Design documents from `/specs/010-update-openapi-readme/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/openapi-documentation-contract.md, quickstart.md

**Tests**: No new automated tests were explicitly requested. Validation tasks use the existing controller tests, OpenAPI review, README review, and Maven test command.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (US1, US2, US3)
- Include exact file paths in descriptions

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Establish the documentation baseline and avoid editing from stale assumptions.

- [ ] T001 Confirm the active branch and pending changes before editing using repository status for `specs/010-update-openapi-readme/tasks.md`
- [ ] T002 [P] Review the endpoint inventory contract in `specs/010-update-openapi-readme/contracts/openapi-documentation-contract.md`
- [ ] T003 [P] Review the current OpenAPI structure, tags, shared schemas, and response conventions in `src/main/java/com/postech/workshop_service/api/controllers/openapi.yaml`
- [ ] T004 [P] Review current README OpenAPI and execution guidance in `README.md`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Build the shared source-of-truth inventory needed before any user story updates begin.

**CRITICAL**: No user story work can begin until this phase is complete.

- [ ] T005 Extract current controller routes, methods, parameters, request bodies, and authorization annotations from `src/main/java/com/postech/workshop_service/api/controllers/AuthController.java`
- [ ] T006 [P] Extract current controller routes, methods, parameters, request bodies, and authorization annotations from `src/main/java/com/postech/workshop_service/api/controllers/ClienteController.java`
- [ ] T007 [P] Extract current controller routes, methods, parameters, request bodies, and authorization annotations from `src/main/java/com/postech/workshop_service/api/controllers/VeiculoController.java`
- [ ] T008 [P] Extract current controller routes, methods, parameters, request bodies, and authorization annotations from `src/main/java/com/postech/workshop_service/api/controllers/ServicoController.java`
- [ ] T009 [P] Extract current controller routes, methods, parameters, request bodies, and authorization annotations from `src/main/java/com/postech/workshop_service/api/controllers/PecaInsumoController.java`
- [ ] T010 [P] Extract current controller routes, methods, parameters, request bodies, and authorization annotations from `src/main/java/com/postech/workshop_service/api/controllers/EstoqueController.java`
- [ ] T011 [P] Extract current controller routes, methods, parameters, request bodies, and authorization annotations from `src/main/java/com/postech/workshop_service/api/controllers/OrdemServicoController.java`
- [ ] T012 [P] Extract current controller routes, methods, parameters, request bodies, and authorization annotations from `src/main/java/com/postech/workshop_service/api/controllers/OrcamentoController.java`
- [ ] T013 [P] Extract current controller routes, methods, parameters, request bodies, and authorization annotations from `src/main/java/com/postech/workshop_service/api/controllers/MetricaController.java`
- [ ] T014 Extract error response status codes and response body conventions from `src/main/java/com/postech/workshop_service/api/controllers/GlobalExceptionHandler.java`
- [ ] T015 Cross-check controller inventory against existing integration coverage in `src/test/java/com/postech/workshop_service/api/controllers/ControllerCoverageTest.java`

**Checkpoint**: Endpoint inventory and shared error conventions are known and ready for story work.

---

## Phase 3: User Story 1 - Consultar contrato completo das APIs (Priority: P1) MVP

**Goal**: Consumers can consult a complete OpenAPI contract matching all public controllers.

**Independent Test**: Compare `openapi.yaml` against the controller inventory and confirm every public operation has method, path, purpose, inputs, outputs, and applicable error responses.

### Implementation for User Story 1

- [ ] T016 [US1] Add or correct authentication operations, request schemas, response schemas, security exceptions, and required examples in `src/main/java/com/postech/workshop_service/api/controllers/openapi.yaml`
- [ ] T017 [US1] Add or correct customer operations, parameters, request schemas, response schemas, errors, and required examples in `src/main/java/com/postech/workshop_service/api/controllers/openapi.yaml`
- [ ] T018 [US1] Add or correct vehicle operations, parameters, request schemas, response schemas, errors, and required examples in `src/main/java/com/postech/workshop_service/api/controllers/openapi.yaml`
- [ ] T019 [US1] Add or correct service catalog operations, parameters, request schemas, response schemas, errors, and required examples in `src/main/java/com/postech/workshop_service/api/controllers/openapi.yaml`
- [ ] T020 [US1] Add or correct parts, inventory, and stock movement operations, parameters, schemas, errors, and required examples in `src/main/java/com/postech/workshop_service/api/controllers/openapi.yaml`
- [ ] T021 [US1] Add or correct service order lifecycle operations, parameters, request schemas, response schemas, errors, and required examples in `src/main/java/com/postech/workshop_service/api/controllers/openapi.yaml`
- [ ] T022 [US1] Add or correct budget approval, rejection, cancellation, and listing operations with schemas, errors, and required examples in `src/main/java/com/postech/workshop_service/api/controllers/openapi.yaml`
- [ ] T023 [US1] Add or correct metrics operations, query parameters, response schemas, authorization responses, and examples when required in `src/main/java/com/postech/workshop_service/api/controllers/openapi.yaml`
- [ ] T024 [US1] Ensure shared schemas for requests, responses, paginated payloads, validation errors, business errors, and authentication errors are present and consistently referenced in `src/main/java/com/postech/workshop_service/api/controllers/openapi.yaml`
- [ ] T025 [US1] Verify the endpoint inventory from `specs/010-update-openapi-readme/contracts/openapi-documentation-contract.md` is fully represented in `src/main/java/com/postech/workshop_service/api/controllers/openapi.yaml`

**Checkpoint**: User Story 1 is complete when every public endpoint is represented in OpenAPI and can be reviewed without reading controller source.

---

## Phase 4: User Story 2 - Validar consistencia e padrao da documentacao (Priority: P2)

**Goal**: Documentation uses consistent tags, names, schemas, examples, and error conventions across domains.

**Independent Test**: Review all OpenAPI groups and confirm equivalent operations use consistent descriptions, response codes, schema names, security rules, and examples.

### Implementation for User Story 2

- [ ] T026 [US2] Normalize tag names, summaries, descriptions, and operation ordering across all path groups in `src/main/java/com/postech/workshop_service/api/controllers/openapi.yaml`
- [ ] T027 [US2] Normalize path, query, and request body parameter descriptions across all operations in `src/main/java/com/postech/workshop_service/api/controllers/openapi.yaml`
- [ ] T028 [US2] Normalize response descriptions for 200, 201, 204, 400, 401, 403, 404, 409, and 422 where applicable in `src/main/java/com/postech/workshop_service/api/controllers/openapi.yaml`
- [ ] T029 [US2] Apply the clarified example rule from `specs/010-update-openapi-readme/spec.md` to all request-body operations, complex responses, and critical business flows in `src/main/java/com/postech/workshop_service/api/controllers/openapi.yaml`
- [ ] T030 [US2] Remove obsolete, duplicated, or contradictory schema definitions from `src/main/java/com/postech/workshop_service/api/controllers/openapi.yaml`
- [ ] T031 [US2] Validate that authorization notes for ADMINISTRADOR, CLIENTE, and public authentication flows match controller behavior in `src/main/java/com/postech/workshop_service/api/controllers/openapi.yaml`

**Checkpoint**: User Story 2 is complete when equivalent operations read as one coherent contract and no obvious naming or error-format drift remains.

---

## Phase 5: User Story 3 - Entender o uso da API pelo README (Priority: P3)

**Goal**: A new project contributor can find, access, and use the API documentation from the README.

**Independent Test**: Starting from `README.md`, confirm a new contributor can identify how to run the app, where Swagger UI is available, where the versioned OpenAPI YAML lives, and which API domains are covered.

### Implementation for User Story 3

- [ ] T032 [US3] Update the OpenAPI documentation section with Swagger UI URL and the versioned YAML source path in `README.md`
- [ ] T033 [US3] Add concise API domain coverage guidance without duplicating the full endpoint contract in `README.md`
- [ ] T034 [US3] Ensure JWT setup, application startup, and documentation access instructions are consistent and non-conflicting in `README.md`
- [ ] T035 [US3] Remove or update stale MVP observations that contradict the current documented API scope in `README.md`

**Checkpoint**: User Story 3 is complete when README guidance supports API discovery in under 10 minutes without duplicating the OpenAPI contract.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Final validation across OpenAPI, README, and existing project behavior.

- [ ] T036 [P] Validate YAML structure and indentation by reviewing `src/main/java/com/postech/workshop_service/api/controllers/openapi.yaml`
- [ ] T037 [P] Validate README links and relative paths for API documentation in `README.md`
- [ ] T038 Run the existing Maven test suite using `pom.xml`
- [ ] T039 Compare the final implementation against quickstart completion criteria in `specs/010-update-openapi-readme/quickstart.md`
- [ ] T040 Record any validation limitation or intentionally deferred follow-up in `specs/010-update-openapi-readme/tasks.md`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies; can start immediately.
- **Foundational (Phase 2)**: Depends on Setup completion; blocks all user stories.
- **User Story 1 (Phase 3)**: Depends on Foundational; recommended MVP.
- **User Story 2 (Phase 4)**: Depends on User Story 1 because consistency review needs the complete OpenAPI contract.
- **User Story 3 (Phase 5)**: Depends on User Story 1 and can run in parallel with User Story 2 after endpoint coverage is stable.
- **Polish (Phase 6)**: Depends on all desired user stories being complete.

### User Story Dependencies

- **User Story 1 (P1)**: No dependency on other stories after Foundational.
- **User Story 2 (P2)**: Depends on User Story 1 for complete documentation surface.
- **User Story 3 (P3)**: Depends on User Story 1 for accurate README references; independent from User Story 2 except for final wording consistency.

### Within Each User Story

- Inventory before OpenAPI edits.
- OpenAPI endpoint coverage before consistency normalization.
- OpenAPI coverage before README summary updates.
- Final validation after all selected stories are complete.

### Parallel Opportunities

- T002, T003, and T004 can run in parallel.
- T006 through T013 can run in parallel because each reads a different controller.
- T036 and T037 can run in parallel after OpenAPI and README edits.
- Different OpenAPI path groups in US1 can be reviewed in parallel only if edits are coordinated because they share `openapi.yaml`.

---

## Parallel Example: Foundational Inventory

```text
Task: "Extract current controller routes from src/main/java/com/postech/workshop_service/api/controllers/ClienteController.java"
Task: "Extract current controller routes from src/main/java/com/postech/workshop_service/api/controllers/VeiculoController.java"
Task: "Extract current controller routes from src/main/java/com/postech/workshop_service/api/controllers/ServicoController.java"
Task: "Extract current controller routes from src/main/java/com/postech/workshop_service/api/controllers/OrdemServicoController.java"
```

---

## Parallel Example: Final Validation

```text
Task: "Validate YAML structure and indentation by reviewing src/main/java/com/postech/workshop_service/api/controllers/openapi.yaml"
Task: "Validate README links and relative paths for API documentation in README.md"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup.
2. Complete Phase 2: Foundational inventory.
3. Complete Phase 3: User Story 1 OpenAPI endpoint coverage.
4. Stop and validate endpoint inventory against `openapi.yaml`.

### Incremental Delivery

1. Complete Setup and Foundational inventory.
2. Deliver User Story 1 as MVP: complete endpoint coverage.
3. Deliver User Story 2: consistency, examples, schemas, and errors.
4. Deliver User Story 3: README discovery and usage guidance.
5. Run Polish validation and record any limitation.

### Notes

- Keep this feature documentary only; do not change controller behavior, use cases, entities, migrations, or authorization rules.
- Preserve the existing OpenAPI style unless it conflicts with the feature requirements.
- Commit after each logical group when repository policy allows it.

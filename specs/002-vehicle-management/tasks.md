---
description: "Task list for Gestao de Veiculos de Clientes implementation"
---

# Tasks: Gestao de Veiculos de Clientes

**Input**: Design documents from `/specs/002-vehicle-management/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/openapi.yaml, quickstart.md

**Tests**: A especificacao e a constituicao exigem testes unitarios e de integracao para os metodos publicos e para cada jornada principal da feature.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (`US1`, `US2`, `US3`)
- Include exact file paths in descriptions

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Preparar a base documental e o esqueleto minimo para o modulo de veiculos

- [X] T001 Atualizar `README.md` com a visao da feature de veiculos, requisitos de execucao e referencia aos endpoints planejados
- [X] T002 [P] Revisar `specs/002-vehicle-management/contracts/openapi.yaml` para alinhar exemplos finais de payload e codigos HTTP aos requisitos FR-001..FR-017
- [X] T003 [P] Registrar comandos de build e teste em `AGENTS.md` para a feature com `./mvnw test` e convencoes do modulo de veiculos

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Infraestrutura e modelagem base que bloqueiam todas as historias

**⚠️ CRITICAL**: Nenhuma historia deve comecar antes da conclusao desta fase

- [X] T004 Criar migration Flyway de veiculos e vinculos em `src/main/resources/db/migration/V0.20260426140000__create_table_veiculos.sql`
- [X] T005 [P] Criar value object `Placa` em `src/main/java/com/postech/workshop_service/domain/valueobjects/Placa.java`
- [X] T006 [P] Criar value object `Chassi` em `src/main/java/com/postech/workshop_service/domain/valueobjects/Chassi.java`
- [X] T007 [P] Criar value object `Renavam` em `src/main/java/com/postech/workshop_service/domain/valueobjects/Renavam.java`
- [X] T008 [P] Criar enum `TipoCombustivel` em `src/main/java/com/postech/workshop_service/domain/valueobjects/TipoCombustivel.java`
- [X] T009 Criar entidade de dominio `Veiculo` com regras de vinculo minimo e soft delete em `src/main/java/com/postech/workshop_service/domain/entities/Veiculo.java`
- [X] T010 Criar contrato `VeiculoRepository` em `src/main/java/com/postech/workshop_service/domain/repositories/VeiculoRepository.java`
- [X] T011 [P] Criar entidades JPA `VeiculoJpaEntity`, `VeiculoClienteJpaEntity` e `VeiculoClienteId` em `src/main/java/com/postech/workshop_service/infrastructure/persistence/entities/VeiculoJpaEntity.java`, `src/main/java/com/postech/workshop_service/infrastructure/persistence/entities/VeiculoClienteJpaEntity.java` e `src/main/java/com/postech/workshop_service/infrastructure/persistence/entities/VeiculoClienteId.java`
- [X] T012 [P] Criar repositrio Spring Data `JpaVeiculoRepository` em `src/main/java/com/postech/workshop_service/infrastructure/persistence/repositories/JpaVeiculoRepository.java`
- [X] T013 [P] Criar mapper `VeiculoMapper` em `src/main/java/com/postech/workshop_service/infrastructure/persistence/mappers/VeiculoMapper.java`
- [X] T014 Implementar adaptador `VeiculoRepositoryImpl` em `src/main/java/com/postech/workshop_service/infrastructure/persistence/repositories/VeiculoRepositoryImpl.java`
- [X] T015 Ajustar `src/main/java/com/postech/workshop_service/infrastructure/config/SecurityConfig.java` para manter compatibilidade futura com restricao de acesso aos endpoints `/api/v1/veiculos/**` sem exigir autenticacao no MVP
- [X] T016 Ajustar `src/main/java/com/postech/workshop_service/api/controllers/GlobalExceptionHandler.java` para mapear erros de veiculo e diferenciar respostas HTTP 400, 404 e 422 com mensagens claras

**Checkpoint**: Fundacao pronta para implementar as historias em incrementos independentes

---

## Phase 3: User Story 1 - Cadastrar e manter veiculo do cliente (Priority: P1) 🎯 MVP

**Goal**: Permitir cadastrar e atualizar veiculos vinculados a um ou mais clientes existentes, com validacoes de dominio e preservacao dos vinculos

**Independent Test**: Cadastrar um veiculo com dados obrigatorios validos e dois clientes existentes, atualizar campos opcionais e a lista de clientes, e verificar bloqueio de placa invalida, ano invalido e lista vazia de clientes

### Tests for User Story 1

- [X] T017 [P] [US1] Criar testes unitarios da entidade `Veiculo` em `src/test/java/com/postech/workshop_service/domain/entities/VeiculoTest.java`
- [X] T018 [P] [US1] Criar testes unitarios dos value objects `Placa`, `Chassi` e `Renavam` em `src/test/java/com/postech/workshop_service/domain/valueobjects/PlacaTest.java`, `src/test/java/com/postech/workshop_service/domain/valueobjects/ChassiTest.java` e `src/test/java/com/postech/workshop_service/domain/valueobjects/RenavamTest.java`
- [X] T019 [P] [US1] Criar testes unitarios dos casos de uso de criacao e atualizacao em `src/test/java/com/postech/workshop_service/application/usecases/CriarVeiculoUseCaseTest.java` e `src/test/java/com/postech/workshop_service/application/usecases/AtualizarVeiculoUseCaseTest.java`
- [X] T020 [P] [US1] Criar teste de integracao do repositorio de veiculos para cadastro e atualizacao em `src/test/java/com/postech/workshop_service/infrastructure/persistence/repositories/VeiculoRepositoryImplIT.java`
- [X] T021 [P] [US1] Criar teste de integracao do controller para `POST /veiculos` e `PUT /veiculos/{id}` em `src/test/java/com/postech/workshop_service/api/controllers/VeiculoControllerIT.java`

### Implementation for User Story 1

- [X] T022 [US1] Implementar `CriarVeiculoUseCase` em `src/main/java/com/postech/workshop_service/application/usecases/CriarVeiculoUseCase.java`
- [X] T023 [US1] Implementar `AtualizarVeiculoUseCase` em `src/main/java/com/postech/workshop_service/application/usecases/AtualizarVeiculoUseCase.java`
- [X] T024 [P] [US1] Criar DTOs `CadastroVeiculoRequest` e `AtualizarVeiculoRequest` em `src/main/java/com/postech/workshop_service/api/dtos/CadastroVeiculoRequest.java` e `src/main/java/com/postech/workshop_service/api/dtos/AtualizarVeiculoRequest.java`
- [X] T025 [P] [US1] Criar DTOs de resposta `ClienteVinculadoResponse` e `VeiculoResponse` em `src/main/java/com/postech/workshop_service/api/dtos/ClienteVinculadoResponse.java` e `src/main/java/com/postech/workshop_service/api/dtos/VeiculoResponse.java`
- [X] T026 [US1] Implementar `VeiculoController` com endpoints de cadastro e atualizacao em `src/main/java/com/postech/workshop_service/api/controllers/VeiculoController.java`
- [X] T027 [US1] Adicionar Javadocs e anotacoes OpenAPI de criacao e atualizacao em `src/main/java/com/postech/workshop_service/api/controllers/VeiculoController.java`
- [X] T028 [US1] Adicionar Javadocs aos metodos publicos de `src/main/java/com/postech/workshop_service/application/usecases/CriarVeiculoUseCase.java`, `src/main/java/com/postech/workshop_service/application/usecases/AtualizarVeiculoUseCase.java` e `src/main/java/com/postech/workshop_service/domain/entities/Veiculo.java`

**Checkpoint**: User Story 1 funcional e testavel isoladamente

---

## Phase 4: User Story 2 - Consultar veiculos com rapidez (Priority: P2)

**Goal**: Permitir localizar veiculos por ID, placa e cliente, alem de listar registros paginados com filtros consistentes

**Independent Test**: Criar veiculos para clientes diferentes e verificar busca por ID, busca por placa com variacoes de formatacao, consulta por cliente compartilhado e listagem paginada com filtros

### Tests for User Story 2

- [X] T029 [P] [US2] Criar testes unitarios dos casos de uso de consulta em `src/test/java/com/postech/workshop_service/application/usecases/BuscarVeiculoPorIdUseCaseTest.java`, `src/test/java/com/postech/workshop_service/application/usecases/BuscarVeiculoPorPlacaUseCaseTest.java`, `src/test/java/com/postech/workshop_service/application/usecases/ListarVeiculosUseCaseTest.java` e `src/test/java/com/postech/workshop_service/application/usecases/ListarVeiculosPorClienteUseCaseTest.java`
- [X] T030 [P] [US2] Expandir teste de integracao do repositorio para consultas por placa, cliente e pagina em `src/test/java/com/postech/workshop_service/infrastructure/persistence/repositories/VeiculoRepositoryImplIT.java`
- [X] T031 [P] [US2] Expandir teste de integracao do controller para `GET /veiculos`, `GET /veiculos/{id}`, `GET /veiculos/placa/{placa}` e `GET /veiculos/cliente/{clienteId}` em `src/test/java/com/postech/workshop_service/api/controllers/VeiculoControllerIT.java`

### Implementation for User Story 2

- [X] T032 [US2] Implementar `BuscarVeiculoPorIdUseCase` em `src/main/java/com/postech/workshop_service/application/usecases/BuscarVeiculoPorIdUseCase.java`
- [X] T033 [P] [US2] Implementar `BuscarVeiculoPorPlacaUseCase` e `ListarVeiculosPorClienteUseCase` em `src/main/java/com/postech/workshop_service/application/usecases/BuscarVeiculoPorPlacaUseCase.java` e `src/main/java/com/postech/workshop_service/application/usecases/ListarVeiculosPorClienteUseCase.java`
- [X] T034 [US2] Implementar `ListarVeiculosUseCase` em `src/main/java/com/postech/workshop_service/application/usecases/ListarVeiculosUseCase.java`
- [X] T035 [P] [US2] Criar DTO `PaginaVeiculosResponse` em `src/main/java/com/postech/workshop_service/api/dtos/PaginaVeiculosResponse.java`
- [X] T036 [US2] Expandir `VeiculoRepository` e `VeiculoRepositoryImpl` com consultas por ID, placa normalizada, cliente e paginacao em `src/main/java/com/postech/workshop_service/domain/repositories/VeiculoRepository.java` e `src/main/java/com/postech/workshop_service/infrastructure/persistence/repositories/VeiculoRepositoryImpl.java`
- [X] T037 [US2] Expandir `JpaVeiculoRepository` com queries de placa, cliente e filtros de ativos em `src/main/java/com/postech/workshop_service/infrastructure/persistence/repositories/JpaVeiculoRepository.java`
- [X] T038 [US2] Expandir `VeiculoController` com endpoints de consulta e listagem paginada em `src/main/java/com/postech/workshop_service/api/controllers/VeiculoController.java`
- [X] T039 [US2] Atualizar documentacao OpenAPI das consultas e filtros em `src/main/java/com/postech/workshop_service/api/controllers/VeiculoController.java` e `specs/002-vehicle-management/contracts/openapi.yaml`
- [X] T040 [US2] Adicionar Javadocs aos metodos publicos de `src/main/java/com/postech/workshop_service/application/usecases/BuscarVeiculoPorIdUseCase.java`, `src/main/java/com/postech/workshop_service/application/usecases/BuscarVeiculoPorPlacaUseCase.java`, `src/main/java/com/postech/workshop_service/application/usecases/ListarVeiculosUseCase.java`, `src/main/java/com/postech/workshop_service/application/usecases/ListarVeiculosPorClienteUseCase.java` e `src/main/java/com/postech/workshop_service/infrastructure/persistence/repositories/VeiculoRepositoryImpl.java`

**Checkpoint**: User Stories 1 e 2 operacionais e testaveis de forma independente

---

## Phase 5: User Story 3 - Remover veiculo sem perder historico (Priority: P3)

**Goal**: Permitir remocao logica idempotente, excluindo o veiculo das consultas operacionais padrao e preservando rastreabilidade

**Independent Test**: Remover logicamente um veiculo existente, confirmar resposta consistente em remocao repetida, validar que o registro nao aparece nas consultas ativas e que ainda pode ser encontrado quando a consulta incluir inativos

### Tests for User Story 3

- [X] T041 [P] [US3] Criar teste unitario do caso de uso de remocao logica em `src/test/java/com/postech/workshop_service/application/usecases/RemoverVeiculoUseCaseTest.java`
- [X] T042 [P] [US3] Expandir teste de integracao do repositorio para filtrar inativos e reutilizacao de placa apos remocao em `src/test/java/com/postech/workshop_service/infrastructure/persistence/repositories/VeiculoRepositoryImplIT.java`
- [X] T043 [P] [US3] Expandir teste de integracao do controller para `DELETE /veiculos/{id}` e consultas com `incluirInativos` em `src/test/java/com/postech/workshop_service/api/controllers/VeiculoControllerIT.java`
- [X] T044 [P] [US3] Criar teste de integracao para garantir preservacao de referencias historicas do veiculo removido em `src/test/java/com/postech/workshop_service/infrastructure/persistence/repositories/VeiculoRepositoryImplIT.java`

### Implementation for User Story 3

- [X] T045 [US3] Implementar `RemoverVeiculoUseCase` em `src/main/java/com/postech/workshop_service/application/usecases/RemoverVeiculoUseCase.java`
- [X] T046 [US3] Ajustar `Veiculo` para remocao logica idempotente e preservacao de data de remocao em `src/main/java/com/postech/workshop_service/domain/entities/Veiculo.java`
- [X] T047 [US3] Ajustar `VeiculoRepository` e `VeiculoRepositoryImpl` para soft delete, filtros de ativos e reutilizacao de placa apos inativacao em `src/main/java/com/postech/workshop_service/domain/repositories/VeiculoRepository.java` e `src/main/java/com/postech/workshop_service/infrastructure/persistence/repositories/VeiculoRepositoryImpl.java`
- [X] T048 [US3] Expandir `VeiculoController` com endpoint de remocao logica e suporte a `incluirInativos` nas consultas em `src/main/java/com/postech/workshop_service/api/controllers/VeiculoController.java`
- [X] T049 [US3] Atualizar anotacoes OpenAPI da remocao logica e das consultas historicas em `src/main/java/com/postech/workshop_service/api/controllers/VeiculoController.java` e `specs/002-vehicle-management/contracts/openapi.yaml`
- [X] T050 [US3] Adicionar Javadocs aos metodos publicos de `src/main/java/com/postech/workshop_service/application/usecases/RemoverVeiculoUseCase.java`

**Checkpoint**: Todas as historias da feature estao funcionais e independentes

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Consolidar qualidade, documentacao e verificacao final

- [X] T051 [P] Ajustar sanitizacao de entradas textuais de veiculo e clientes vinculados em `src/main/java/com/postech/workshop_service/domain/entities/Veiculo.java`, `src/main/java/com/postech/workshop_service/domain/valueobjects/Placa.java`, `src/main/java/com/postech/workshop_service/domain/valueobjects/Chassi.java` e `src/main/java/com/postech/workshop_service/domain/valueobjects/Renavam.java`
- [X] T052 [P] Revisar exemplos e descricao da feature em `specs/002-vehicle-management/quickstart.md` e `specs/002-vehicle-management/contracts/openapi.yaml` conforme implementacao final
- [X] T053 Executar e corrigir a suite `./mvnw test` a partir dos testes em `src/test/java/com/postech/workshop_service/`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: pode iniciar imediatamente
- **Foundational (Phase 2)**: depende da fase de Setup e bloqueia todas as historias
- **User Story 1 (Phase 3)**: depende da conclusao da fase Foundational
- **User Story 2 (Phase 4)**: depende da fase Foundational e reutiliza a base entregue por US1, mas continua testavel de forma independente
- **User Story 3 (Phase 5)**: depende da fase Foundational e da existencia do agregado persistido; pode aproveitar consultas da US2 para validacao
- **Polish (Phase 6)**: depende das historias desejadas concluidas

### User Story Dependencies

- **US1 (P1)**: primeira entrega MVP, sem dependencia de outras historias
- **US2 (P2)**: depende da estrutura de persistencia e dos DTOs/respostas da US1 para expor consultas completas
- **US3 (P3)**: depende da persistencia e do controller ja existentes para aplicar remocao logica e filtros historicos

### Within Each User Story

- Testes devem ser escritos e falhar antes da implementacao principal
- Casos de uso antes de endpoints
- DTOs/modelos de resposta antes do acabamento da API
- Ajustes de documentacao OpenAPI apos os endpoints funcionarem

### Parallel Opportunities

- T002 e T003 podem rodar em paralelo na fase Setup
- T005, T006, T007, T008, T011, T012 e T013 podem rodar em paralelo na fase Foundational
- Na US1, T017, T018, T019, T020 e T021 podem ser preparados em paralelo
- Na US2, T029, T030 e T031 podem rodar em paralelo; T033 e T035 tambem
- Na US3, T041, T042, T043 e T044 podem rodar em paralelo

---

## Parallel Example: User Story 1

```bash
# Testes da US1 em paralelo
Task: "Criar testes unitarios da entidade Veiculo em src/test/java/com/postech/workshop_service/domain/entities/VeiculoTest.java"
Task: "Criar testes unitarios dos value objects em src/test/java/com/postech/workshop_service/domain/valueobjects/PlacaTest.java, ChassiTest.java e RenavamTest.java"
Task: "Criar testes unitarios dos casos de uso em src/test/java/com/postech/workshop_service/application/usecases/CriarVeiculoUseCaseTest.java e AtualizarVeiculoUseCaseTest.java"

# DTOs da US1 em paralelo
Task: "Criar DTOs CadastroVeiculoRequest e AtualizarVeiculoRequest em src/main/java/com/postech/workshop_service/api/dtos/"
Task: "Criar DTOs ClienteVinculadoResponse e VeiculoResponse em src/main/java/com/postech/workshop_service/api/dtos/"
```

---

## Parallel Example: User Story 2

```bash
# Testes da US2 em paralelo
Task: "Criar testes unitarios dos casos de uso de consulta em src/test/java/com/postech/workshop_service/application/usecases/"
Task: "Expandir teste de integracao do repositorio para consultas em src/test/java/com/postech/workshop_service/infrastructure/persistence/repositories/VeiculoRepositoryImplIT.java"
Task: "Expandir teste de integracao do controller para GETs de veiculos em src/test/java/com/postech/workshop_service/api/controllers/VeiculoControllerIT.java"

# Implementacoes paralelas da US2
Task: "Implementar BuscarVeiculoPorPlacaUseCase e ListarVeiculosPorClienteUseCase em src/main/java/com/postech/workshop_service/application/usecases/"
Task: "Criar PaginaVeiculosResponse em src/main/java/com/postech/workshop_service/api/dtos/PaginaVeiculosResponse.java"
```

---

## Parallel Example: User Story 3

```bash
# Testes da US3 em paralelo
Task: "Criar teste unitario de remocao logica em src/test/java/com/postech/workshop_service/application/usecases/RemoverVeiculoUseCaseTest.java"
Task: "Expandir testes de integracao para soft delete no repositorio e controller em src/test/java/com/postech/workshop_service/infrastructure/persistence/repositories/VeiculoRepositoryImplIT.java e src/test/java/com/postech/workshop_service/api/controllers/VeiculoControllerIT.java"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Concluir Setup
2. Concluir Foundational
3. Entregar US1 completa
4. Validar cadastro e atualizacao com clientes multiplos
5. Demonstrar MVP

### Incremental Delivery

1. Base tecnica pronta com schema, dominio, persistencia e seguranca
2. Entregar US1 e validar criacao/atualizacao
3. Entregar US2 e validar consultas independentes
4. Entregar US3 e validar remocao logica e busca historica
5. Encerrar com polimento, documentacao e suite de testes verde

### Parallel Team Strategy

1. Time fecha as tasks T004-T016 em conjunto
2. Depois da fundacao:
   - Pessoa A: US1
   - Pessoa B: US2
   - Pessoa C: US3
3. Consolidar na fase de Polish com execucao completa dos testes

---

## Notes

- Todas as tasks seguem o formato `- [ ] Txxx ... caminho/do/arquivo`
- As historias permanecem rastreaveis pelos labels `US1`, `US2` e `US3`
- Os caminhos usam a estrutura real do projeto Spring Boot atual
- O script de prerequisitos `.specify/scripts/bash/check-prerequisites.sh --json` falhou neste ambiente Windows, entao a geracao foi feita diretamente a partir dos artefatos ja existentes em `specs/002-vehicle-management/`


# Tasks: Controle de Acesso Autenticado

**Input**: Design documents from `/specs/006-jwt-auth-rbac/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/auth-api.yaml

**Tests**: Testes sao obrigatorios nesta feature porque a spec exige cobertura de login, refresh, logout e acesso negado por role.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

- Projeto unico Spring Boot em `src/main/java`, `src/main/resources` e `src/test/java`
- Controllers em `src/main/java/com/postech/workshop_service/api/controllers`
- Casos de uso em `src/main/java/com/postech/workshop_service/application/usecases`
- Dominio em `src/main/java/com/postech/workshop_service/domain`
- Persistencia em `src/main/java/com/postech/workshop_service/infrastructure/persistence`

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Preparar dependencias, configuracao e contratos base da feature

- [ ] T001 Adicionar dependencias JWT e ajustes de build em `pom.xml`
- [ ] T002 Configurar propriedades de seguranca JWT e expiracao em `src/main/resources/application.yml`
- [ ] T003 [P] Atualizar o contrato de autenticacao conforme as clarificacoes em `specs/006-jwt-auth-rbac/contracts/auth-api.yaml`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Infraestrutura central de autenticacao/autorizacao que bloqueia todas as historias

**CRITICAL**: Nenhuma historia pode comecar antes desta fase

- [ ] T004 Criar migration de usuarios, roles e refresh tokens em `src/main/resources/db/migration/V0.20260501190000__create_table_usuarios_roles_refresh_tokens.sql`
- [ ] T005 [P] Criar enum de roles em `src/main/java/com/postech/workshop_service/domain/enums/Role.java`
- [ ] T006 [P] Criar entidade de dominio `Usuario` em `src/main/java/com/postech/workshop_service/domain/entities/Usuario.java`
- [ ] T007 [P] Criar entidade de dominio `RefreshToken` em `src/main/java/com/postech/workshop_service/domain/entities/RefreshToken.java`
- [ ] T008 [P] Criar interfaces de repositorio em `src/main/java/com/postech/workshop_service/domain/repositories/UsuarioRepository.java` e `src/main/java/com/postech/workshop_service/domain/repositories/RefreshTokenRepository.java`
- [ ] T009 [P] Criar entidades JPA em `src/main/java/com/postech/workshop_service/infrastructure/persistence/entities/UsuarioJpaEntity.java` e `src/main/java/com/postech/workshop_service/infrastructure/persistence/entities/RefreshTokenJpaEntity.java`
- [ ] T010 [P] Criar repositorios Spring Data em `src/main/java/com/postech/workshop_service/infrastructure/persistence/repositories/JpaUsuarioRepository.java` e `src/main/java/com/postech/workshop_service/infrastructure/persistence/repositories/JpaRefreshTokenRepository.java`
- [ ] T011 [P] Criar mapeadores e implementacoes de repositorio em `src/main/java/com/postech/workshop_service/infrastructure/persistence/mappers/UsuarioMapper.java`, `src/main/java/com/postech/workshop_service/infrastructure/persistence/mappers/RefreshTokenMapper.java`, `src/main/java/com/postech/workshop_service/infrastructure/persistence/repositories/UsuarioRepositoryImpl.java` e `src/main/java/com/postech/workshop_service/infrastructure/persistence/repositories/RefreshTokenRepositoryImpl.java`
- [ ] T012 Implementar servico de tokens JWT e principal autenticado em `src/main/java/com/postech/workshop_service/infrastructure/security/JwtTokenService.java`, `src/main/java/com/postech/workshop_service/infrastructure/security/UsuarioAutenticadoPrincipal.java` e `src/main/java/com/postech/workshop_service/infrastructure/security/DetalhesUsuarioServiceImpl.java`
- [ ] T013 Implementar filtro JWT, handlers 401/403 e endurecer a configuracao global em `src/main/java/com/postech/workshop_service/infrastructure/security/JwtAuthenticationFilter.java`, `src/main/java/com/postech/workshop_service/infrastructure/security/JwtAuthenticationEntryPoint.java`, `src/main/java/com/postech/workshop_service/infrastructure/security/JwtAccessDeniedHandler.java` e `src/main/java/com/postech/workshop_service/infrastructure/config/SecurityConfig.java`

**Checkpoint**: Fundacao pronta - login JWT, persistencia de refresh token e pipeline de seguranca habilitados

---

## Phase 3: User Story 1 - Entrar em area protegida (Priority: P1) MVP

**Goal**: Permitir login com `username` ou `email`, emitir access token/refresh token e retornar a identidade do usuario autenticado

**Independent Test**: Validar login bem-sucedido, login invalido, `GET /api/auth/me` autenticado e `401` para rota protegida sem token

### Tests for User Story 1

- [ ] T014 [P] [US1] Criar teste de integracao para `POST /api/auth/login` e `GET /api/auth/me` em `src/test/java/com/postech/workshop_service/api/controllers/AuthControllerIT.java`
- [ ] T015 [P] [US1] Criar testes unitarios do caso de uso de login em `src/test/java/com/postech/workshop_service/application/usecases/RealizarLoginUseCaseTest.java`
- [ ] T016 [P] [US1] Criar testes unitarios da consulta ao usuario autenticado em `src/test/java/com/postech/workshop_service/application/usecases/BuscarUsuarioAutenticadoUseCaseTest.java`

### Implementation for User Story 1

- [ ] T017 [P] [US1] Criar DTOs de autenticacao em `src/main/java/com/postech/workshop_service/api/dtos/LoginRequest.java`, `src/main/java/com/postech/workshop_service/api/dtos/AuthTokensResponse.java` e `src/main/java/com/postech/workshop_service/api/dtos/UsuarioAutenticadoResponse.java`
- [ ] T018 [P] [US1] Criar excecoes de autenticacao em `src/main/java/com/postech/workshop_service/application/exceptions/CredenciaisInvalidasException.java` e `src/main/java/com/postech/workshop_service/application/exceptions/ContaInativaException.java`
- [ ] T019 [US1] Implementar caso de uso de login em `src/main/java/com/postech/workshop_service/application/usecases/RealizarLoginUseCase.java`
- [ ] T020 [US1] Implementar caso de uso de consulta do usuario autenticado em `src/main/java/com/postech/workshop_service/application/usecases/BuscarUsuarioAutenticadoUseCase.java`
- [ ] T021 [US1] Implementar endpoints `/api/auth/login` e `/api/auth/me` com Javadoc/OpenAPI em `src/main/java/com/postech/workshop_service/api/controllers/AuthController.java`
- [ ] T022 [US1] Integrar excecoes de login ao tratamento HTTP em `src/main/java/com/postech/workshop_service/api/controllers/GlobalExceptionHandler.java`

**Checkpoint**: User Story 1 funcional e validavel de forma independente

---

## Phase 4: User Story 2 - Renovar ou encerrar sessao (Priority: P2)

**Goal**: Permitir refresh rotativo por sessao e logout que revoga apenas a sessao indicada pelo refresh token

**Independent Test**: Validar refresh bem-sucedido com rotacao, rejeicao de refresh expirado/revogado, logout da sessao alvo e coexistencia de multiplas sessoes

### Tests for User Story 2

- [ ] T023 [P] [US2] Criar teste de integracao para `/api/auth/refresh` e `/api/auth/logout` em `src/test/java/com/postech/workshop_service/api/controllers/AuthRefreshLogoutIT.java`
- [ ] T024 [P] [US2] Criar testes unitarios do refresh rotativo em `src/test/java/com/postech/workshop_service/application/usecases/RenovarSessaoUseCaseTest.java`
- [ ] T025 [P] [US2] Criar testes unitarios do logout por sessao em `src/test/java/com/postech/workshop_service/application/usecases/EncerrarSessaoUseCaseTest.java`

### Implementation for User Story 2

- [ ] T026 [P] [US2] Criar DTOs e excecao de refresh/logout em `src/main/java/com/postech/workshop_service/api/dtos/RefreshTokenRequest.java`, `src/main/java/com/postech/workshop_service/api/dtos/LogoutRequest.java` e `src/main/java/com/postech/workshop_service/application/exceptions/TokenInvalidoException.java`
- [ ] T027 [US2] Implementar caso de uso de renovacao de sessao em `src/main/java/com/postech/workshop_service/application/usecases/RenovarSessaoUseCase.java`
- [ ] T028 [US2] Implementar caso de uso de encerramento de sessao em `src/main/java/com/postech/workshop_service/application/usecases/EncerrarSessaoUseCase.java`
- [ ] T029 [US2] Atualizar `AuthController` com `/api/auth/refresh` e `/api/auth/logout` em `src/main/java/com/postech/workshop_service/api/controllers/AuthController.java`
- [ ] T030 [US2] Ajustar `JwtTokenService` e `RefreshTokenRepositoryImpl` para rotacao e multiplas sessoes em `src/main/java/com/postech/workshop_service/infrastructure/security/JwtTokenService.java` e `src/main/java/com/postech/workshop_service/infrastructure/persistence/repositories/RefreshTokenRepositoryImpl.java`

**Checkpoint**: User Stories 1 e 2 funcionam isoladamente e em conjunto

---

## Phase 5: User Story 3 - Restringir acoes por perfil (Priority: P3)

**Goal**: Exigir JWT nas rotas protegidas, aplicar `@PreAuthorize` por papel e restringir dados proprios do papel `CLIENTE`

**Independent Test**: Validar `403` por role inadequada, acesso livre apenas a rotas publicas e escopo de dados proprios para `CLIENTE`

### Tests for User Story 3

- [ ] T031 [P] [US3] Criar teste de integracao de `401/403` e rotas publicas em `src/test/java/com/postech/workshop_service/api/controllers/SecurityAccessIT.java`
- [ ] T032 [P] [US3] Criar teste de integracao de escopo de dados proprios do cliente em `src/test/java/com/postech/workshop_service/api/controllers/ClienteSecurityIT.java`
- [ ] T033 [P] [US3] Criar teste unitario do vinculo 1:1 cliente-conta em `src/test/java/com/postech/workshop_service/domain/entities/UsuarioTest.java`

### Implementation for User Story 3

- [ ] T034 [US3] Aplicar autorizacao por role em `src/main/java/com/postech/workshop_service/api/controllers/ClienteController.java` e `src/main/java/com/postech/workshop_service/api/controllers/VeiculoController.java`
- [ ] T035 [US3] Aplicar autorizacao por role em `src/main/java/com/postech/workshop_service/api/controllers/ServicoController.java`, `src/main/java/com/postech/workshop_service/api/controllers/PecaInsumoController.java` e `src/main/java/com/postech/workshop_service/api/controllers/EstoqueController.java`
- [ ] T036 [US3] Implementar regra de ownership do papel `CLIENTE` em `src/main/java/com/postech/workshop_service/application/usecases/BuscarUsuarioAutenticadoUseCase.java`, `src/main/java/com/postech/workshop_service/domain/entities/Usuario.java` e `src/main/java/com/postech/workshop_service/infrastructure/persistence/entities/UsuarioJpaEntity.java`
- [ ] T037 [US3] Expor o endpoint publico de rastreamento sem autenticacao e restringir o restante em `src/main/java/com/postech/workshop_service/infrastructure/config/SecurityConfig.java`

**Checkpoint**: Todas as historias estao independentes e funcionalmente completas

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Fechamentos que afetam multiplas historias

- [ ] T038 [P] Sincronizar `quickstart.md` com login por `username/email`, logout por sessao e multiplas sessoes em `specs/006-jwt-auth-rbac/quickstart.md`
- [ ] T039 [P] Sincronizar `data-model.md` com o vinculo 1:1 do papel `CLIENTE` em `specs/006-jwt-auth-rbac/data-model.md`
- [ ] T040 Validar a suite da feature e ajustar documentacao final em `pom.xml` e `specs/006-jwt-auth-rbac/contracts/auth-api.yaml`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1 (Setup)**: sem dependencias
- **Phase 2 (Foundational)**: depende da Phase 1 e bloqueia todas as historias
- **Phase 3 (US1)**: depende da Phase 2
- **Phase 4 (US2)**: depende da Phase 2 e integra com os artefatos de US1
- **Phase 5 (US3)**: depende da Phase 2 e consome autenticacao pronta de US1/US2
- **Phase 6 (Polish)**: depende das historias desejadas concluidas

### User Story Dependencies

- **US1 (P1)**: pode iniciar apos a fundacao; e o MVP da feature
- **US2 (P2)**: depende da autenticacao base, mas pode ser validada independentemente apos US1
- **US3 (P3)**: depende da autenticacao base, mas deve ser testavel sem depender do refresh/logout

### Within Each User Story

- Testes devem ser escritos primeiro e falhar antes da implementacao
- DTOs/entidades antes dos casos de uso
- Casos de uso antes dos controllers
- Configuracao e handlers antes da validacao final

### Parallel Opportunities

- T003 pode rodar em paralelo com T001-T002
- T005-T011 podem rodar em paralelo dentro da fundacao, desde que respeitem dependencias de leitura do modelo
- T014-T018 podem ser paralelizados em US1
- T023-T026 podem ser paralelizados em US2
- T031-T033 podem ser paralelizados em US3
- T038-T039 podem ser executados em paralelo na fase final

---

## Parallel Example: User Story 1

```bash
Task: "Criar teste de integracao para POST /api/auth/login e GET /api/auth/me em src/test/java/com/postech/workshop_service/api/controllers/AuthControllerIT.java"
Task: "Criar testes unitarios do caso de uso de login em src/test/java/com/postech/workshop_service/application/usecases/RealizarLoginUseCaseTest.java"
Task: "Criar DTOs de autenticacao em src/main/java/com/postech/workshop_service/api/dtos/LoginRequest.java, src/main/java/com/postech/workshop_service/api/dtos/AuthTokensResponse.java e src/main/java/com/postech/workshop_service/api/dtos/UsuarioAutenticadoResponse.java"
```

---

## Parallel Example: User Story 2

```bash
Task: "Criar teste de integracao para /api/auth/refresh e /api/auth/logout em src/test/java/com/postech/workshop_service/api/controllers/AuthRefreshLogoutIT.java"
Task: "Criar testes unitarios do refresh rotativo em src/test/java/com/postech/workshop_service/application/usecases/RenovarSessaoUseCaseTest.java"
Task: "Criar DTOs e excecao de refresh/logout em src/main/java/com/postech/workshop_service/api/dtos/RefreshTokenRequest.java, src/main/java/com/postech/workshop_service/api/dtos/LogoutRequest.java e src/main/java/com/postech/workshop_service/application/exceptions/TokenInvalidoException.java"
```

---

## Parallel Example: User Story 3

```bash
Task: "Criar teste de integracao de 401/403 e rotas publicas em src/test/java/com/postech/workshop_service/api/controllers/SecurityAccessIT.java"
Task: "Criar teste de integracao de escopo de dados proprios do cliente em src/test/java/com/postech/workshop_service/api/controllers/ClienteSecurityIT.java"
Task: "Aplicar autorizacao por role em src/main/java/com/postech/workshop_service/api/controllers/ServicoController.java, src/main/java/com/postech/workshop_service/api/controllers/PecaInsumoController.java e src/main/java/com/postech/workshop_service/api/controllers/EstoqueController.java"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Completar Setup
2. Completar Foundational
3. Completar US1
4. Validar login, `me` e `401` sem token

### Incremental Delivery

1. Fundacao pronta
2. Entregar US1 como MVP autenticado
3. Adicionar US2 com refresh/logout por sessao
4. Adicionar US3 com RBAC e ownership do cliente
5. Fechar com polish e sincronizacao documental

### Parallel Team Strategy

1. Uma pessoa fecha T001-T004
2. Outra pessoa pode assumir T005-T011 em paralelo por fatias de dominio/persistencia
3. Depois da fundacao:
   - Pessoa A: US1
   - Pessoa B: US2
   - Pessoa C: US3

---

## Notes

- Todas as tasks seguem o formato `- [ ] Txxx ...`
- Historias foram separadas para permitir validacao independente
- O `plan.md` foi gerado antes das clarificacoes; por isso T038-T039 sincronizam a documentacao de design com as decisoes finais da spec

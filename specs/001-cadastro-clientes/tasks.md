---
description: "Task list for CRUD Completo de Clientes implementation"
---

# Tasks: CRUD Completo de Clientes

**Input**: Design documents from `/specs/001-cadastro-clientes/`
**Prerequisites**: plan.md, spec.md, data-model.md

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel
- **[Story]**: US1 (Cadastro), US2 (Read/Update/Delete)

## Phase 5: CRUD Expandido (US2)

**Goal**: Implementar as operações de leitura, atualização e exclusão, além de suportar novos campos e validações rigorosas.

### Foundations for CRUD
- [ ] T024 [P] [US2] Implementar algoritmos de validação real de CPF/CNPJ em `domain/valueobjects/Documento.java`
- [ ] T025 [P] [US2] Criar Value Object `Endereco` em `domain/valueobjects/Endereco.java`
- [ ] T026 [P] [US2] Atualizar entidade `Cliente` com campos opcionais e métodos de alteração em `domain/entities/Cliente.java`
- [ ] T027 [P] [US2] Atualizar `ClienteJpaEntity` com os novos campos e @Embedded Endereco em `infrastructure/persistence/ClienteJpaEntity.java`
- [ ] T028 [US2] Criar migration Flyway para novos campos em `src/main/resources/db/migration/V0.20260426113000__add_fields_to_clientes.sql`

### Use Cases (Application)
- [ ] T029 [US2] Implementar `AtualizarClienteUseCase` em `application/usecases/AtualizarClienteUseCase.java`
- [ ] T030 [US2] Implementar `BuscarClienteUseCase` (lista paginada, por id, por documento) em `application/usecases/BuscarClienteUseCase.java`
- [ ] T031 [US2] Implementar `RemoverClienteUseCase` em `application/usecases/RemoverClienteUseCase.java`

### API & Integration
- [ ] T032 [US2] Atualizar `ClienteController` com endpoints GET (list/id/doc), PUT e DELETE em `api/controllers/ClienteController.java`
- [ ] T033 [US2] Criar DTO `AtualizarClienteRequest` e atualizar `CadastroClienteRequest` e `ClienteResponse`
- [ ] T034 [US2] Adicionar suporte a validações de Email e Telefone via anotações ou VO em `api/dtos`

### Validation & Quality
- [ ] T035 [US2] Atualizar testes de integração `ClienteControllerIT` cobrindo todos os novos endpoints
- [ ] T036 [US2] Validar cobertura de testes >= 80% usando Jacoco e ajustar se necessário
- [ ] T037 [US2] Garantir que o Swagger reflita todos os novos campos e endpoints

## Phase 6: Segurança e Polimento
- [ ] T038 Configurar `SecurityConfig` para exigir JWT nos endpoints (conforme requisito de APIs administrativas)
- [ ] T039 Garantir sanitização de dados em todos os inputs da API

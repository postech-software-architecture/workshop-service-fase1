# Implementation Plan: CRUD Completo de Clientes

**Branch**: `001-cadastro-clientes` | **Date**: 2026-04-26 | **Spec**: [spec.md](./spec.md)
**Input**: Requisitos completos de CRUD, validações e segurança.

## Summary

Expandir o módulo de clientes para suportar todas as operações CRUD, incluindo busca paginada, busca por documento, atualização e remoção. Adicionar suporte a campos opcionais (Endereço, Datas, Observações) e garantir segurança via JWT e validações robustas.

## Technical Context

**Language/Version**: Java 21
**Primary Dependencies**: Spring Boot 3.4.1, Lombok, Spring Security (JWT), Spring Data JPA, Flyway, Springdoc OpenAPI
**Storage**: PostgreSQL
**Testing**: JUnit 5, Testcontainers, Mockito, JaCoCo (80% coverage)
**Constraints**: Arquitetura DDD, Migrations com V0.YYYYMMDDHHMMSS__ padronizado.

## Constitution Check

- [x] **Idioma e Plataforma**: pt-BR / Java 21.
- [x] **Padrões de Código**: Lombok (Exceto em Value Objects imutáveis onde Records ou construtores manuais são melhores).
- [x] **Arquitetura**: Layered / DDD.
- [x] **Documentação**: Javadoc + OpenAPI.
- [x] **Banco de Dados**: Flyway com comentários e campos de auditoria.

## Proposed Changes

### Domain Layer

- **[MODIFY] [Documento.java](file:///c:/Users/mateu/OneDrive/Área%20de%20Trabalho/FIAP-DDD/teste/workshop-service-fase1/workshop-service/src/main/java/com/postech/workshop_service/domain/valueobjects/Documento.java)**: Implementar validação matemática real de CPF e CNPJ.
- **[NEW] [Endereco.java](file:///c:/Users/mateu/OneDrive/Área%20de%20Trabalho/FIAP-DDD/teste/workshop-service-fase1/workshop-service/src/main/java/com/postech/workshop_service/domain/valueobjects/Endereco.java)**: Value Object para endereço.
- **[MODIFY] [Cliente.java](file:///c:/Users/mateu/OneDrive/Área%20de%20Trabalho/FIAP-DDD/teste/workshop-service-fase1/workshop-service/src/main/java/com/postech/workshop_service/domain/entities/Cliente.java)**: Adicionar novos campos e métodos de atualização.

### Application Layer

- **[NEW] [AtualizarClienteUseCase.java](file:///c:/Users/mateu/OneDrive/Área%20de%20Trabalho/FIAP-DDD/teste/workshop-service-fase1/workshop-service/src/main/java/com/postech/workshop_service/application/usecases/AtualizarClienteUseCase.java)**
- **[NEW] [BuscarClienteUseCase.java](file:///c:/Users/mateu/OneDrive/Área%20de%20Trabalho/FIAP-DDD/teste/workshop-service-fase1/workshop-service/src/main/java/com/postech/workshop_service/application/usecases/BuscarClienteUseCase.java)**: Inclui listagem paginada e busca por documento.
- **[NEW] [RemoverClienteUseCase.java](file:///c:/Users/mateu/OneDrive/Área%20de%20Trabalho/FIAP-DDD/teste/workshop-service-fase1/workshop-service/src/main/java/com/postech/workshop_service/application/usecases/RemoverClienteUseCase.java)**

### API Layer

- **[MODIFY] [ClienteController.java](file:///c:/Users/mateu/OneDrive/Área%20de%20Trabalho/FIAP-DDD/teste/workshop-service-fase1/workshop-service/src/main/java/com/postech/workshop_service/api/controllers/ClienteController.java)**: Adicionar endpoints GET, PUT, DELETE.
- **[MODIFY] [CadastroClienteRequest.java](file:///c:/Users/mateu/OneDrive/Área%20de%20Trabalho/FIAP-DDD/teste/workshop-service-fase1/workshop-service/src/main/java/com/postech/workshop_service/api/dtos/CadastroClienteRequest.java)**: Incluir campos opcionais.
- **[NEW] [AtualizarClienteRequest.java](file:///c:/Users/mateu/OneDrive/Área%20de%20Trabalho/FIAP-DDD/teste/workshop-service-fase1/workshop-service/src/main/java/com/postech/workshop_service/api/dtos/AtualizarClienteRequest.java)**

### Infrastructure Layer

- **[MODIFY] [ClienteJpaEntity.java](file:///c:/Users/mateu/OneDrive/Área%20de%20Trabalho/FIAP-DDD/teste/workshop-service-fase1/workshop-service/src/main/java/com/postech/workshop_service/infrastructure/persistence/ClienteJpaEntity.java)**: Mapear novos campos e `Endereco` embedded.
- **[NEW] [SecurityConfig.java](file:///c:/Users/mateu/OneDrive/Área%20de%20Trabalho/FIAP-DDD/teste/workshop-service-fase1/workshop-service/src/main/java/com/postech/workshop_service/infrastructure/config/SecurityConfig.java)**: Configuração básica de Security (mesmo que permita tudo por enquanto para focar no CRUD, deve estar preparada).
- **[NEW] [V0.20260426113000__add_fields_to_clientes.sql](file:///c:/Users/mateu/OneDrive/Área%20de%20Trabalho/FIAP-DDD/teste/workshop-service-fase1/workshop-service/src/main/resources/db/migration/V0.20260426113000__add_fields_to_clientes.sql)**

## Verification Plan

### Automated Tests
- Testes unitários para `Documento` (validando CPFs/CNPJs reais e fakes).
- Testes unitários para `Cliente` (validando invariantes).
- Testes de integração `ClienteControllerIT` cobrindo o fluxo completo (C -> R -> U -> D).
- Execução: `./mvnw test` e `./mvnw jacoco:report`.

### Manual Verification
- Acesso ao `/swagger-ui.html` para testar os endpoints interativamente.

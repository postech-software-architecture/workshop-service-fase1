# Feature Specification: CRUD Completo de Clientes

**Feature Branch**: `001-cadastro-clientes`  
**Created**: 2026-04-24  
**Last Updated**: 2026-04-26  
**Status**: In Progress  
**Input**: Descrição detalhada do CRUD completo de clientes, incluindo validações robustas, campos opcionais e requisitos técnicos de segurança e performance.

## Clarifications

- **Autenticação**: APIs administrativas (Listagem, Update, Delete) exigem Autenticação JWT. O cadastro inicial (POST) pode ser público ou autenticado dependendo da política de registro (assumiremos autenticado para "APIs administrativas" conforme solicitado).
- **Documento**: Campo único que aceita CPF ou CNPJ. Validação rigorosa de dígitos verificadores é mandatória.
- **Relacionamentos**: Preparar a estrutura para futuros relacionamentos com Veículos e Ordens de Serviço.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Gerenciamento Completo de Clientes (CRUD) (Priority: P1)

Como gestor da oficina, desejo cadastrar, consultar, atualizar e remover clientes para manter a base de dados atualizada e operacional.

**Acceptance Scenarios**:

1. **Criação com Sucesso**: Dado dados válidos (Nome, CPF/CNPJ válido, Email/Telefone), quando salvo, o cliente é persistido com sucesso (HTTP 201).
2. **Validação de Documento**: Dado um CPF ou CNPJ com dígitos verificadores inválidos, o sistema deve rejeitar (HTTP 400 ou 422).
3. **Prevenção de Duplicidade**: Não permitir dois clientes com o mesmo CPF/CNPJ (HTTP 400).
4. **Consulta Paginada**: A listagem de clientes deve suportar paginação para performance.
5. **Busca por Documento**: Deve ser possível localizar um cliente rapidamente informando apenas o CPF ou CNPJ.
6. **Atualização Segura**: Alterar dados (exceto o documento de identificação, que deve ser imutável após a criação para manter integridade) deve refletir imediatamente.
7. **Remoção**: Excluir um cliente remove seu registro do sistema (HTTP 204).

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: Implementar Create (POST), Read (GET list/id/doc), Update (PUT), Delete (DELETE).
- **FR-002**: Validação rigorosa de CPF/CNPJ (formato e algoritmos de dígitos).
- **FR-003**: Validação de formato para Email e Telefone.
- **FR-004**: Suporte a campos opcionais: Endereço completo, Data de Nascimento/Fundação, Observações.
- **FR-005**: Paginação obrigatória no endpoint de listagem.
- **FR-006**: Proteção contra duplicação de documento.
- **FR-007**: Sanitização de todos os inputs para evitar XSS/Injection.

### Technical Requirements

- **TR-001**: API RESTful seguindo padrões HTTP apropriados.
- **TR-002**: Autenticação JWT para endpoints administrativos.
- **TR-003**: Documentação OpenAPI (Swagger) completa.
- **TR-004**: Cobertura de testes unitários e de integração >= 80%.
- **TR-005**: Tratamento global de exceções com ErrorResponse padronizado.

### Key Entities

- **Cliente**: Aggregate Root contendo Nome, Documento (VO), Contato (Email, Telefone), Endereço (VO), Metadados (Data Nascimento, Observações) e Auditoria.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% dos documentos no banco são válidos e únicos.
- **SC-002**: Cobertura de testes reportada pelo Jacoco/Maven >= 80%.
- **SC-003**: Documentação Swagger acessível em `/swagger-ui.html`.
- **SC-004**: Respostas HTTP condizem com o resultado (201 para criação, 204 para delete, 404 para não encontrado, etc).

## Assumptions

- O campo de documento (CPF/CNPJ) é a chave de negócio e não muda após o cadastro.
- A autenticação JWT será implementada via Spring Security.
- A paginação padrão será de 20 itens por página.
- O endereço será tratado como um Value Object opcional.

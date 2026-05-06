# Workshop Service - Fase 1

Serviço desenvolvido em Spring Boot para gestão de uma oficina mecânica, com foco na organização do atendimento, rastreabilidade das operações e consistência das regras de domínio.

Este projeto aplica conceitos de Domain-Driven Design (DDD) para estruturar regras de negócio, entidades e fluxos operacionais de forma clara e evolutiva.

## Objetivo
Centralizar o gerenciamento de clientes e veículos, garantindo:
- Consistência dos dados
- Rastreabilidade das operações
- Base sólida para evolução do domínio (ordens de serviço, orçamento, execução)

## Modelagem de Domínio
O sistema é estruturado com base em DDD, incluindo:
- Linguagem Ubíqua definida para alinhar negócio e desenvolvimento
- Separação clara entre Core Domain e Supporting Domain
- Organização por camadas: domain, application, infrastructure e api
- Organização dos eventos esperados (comandos, eventos e políticas)

Documentação de domínio:
- [Dicionário de Linguagem Ubíqua](docs/linguagem-ubiqua/dicionario_linguagem_ubiqua_completo.pdf)
- [Diagramas Domain Storytelling](/docs/DDD_storytelling)
- [Event Storming](em construção)


## Tecnologias

- Java 21
- Spring Boot 3.4.1
- Spring Web
- Spring Data JPA
- Spring Validation
- Spring Security
- SpringDoc OpenAPI
- PostgreSQL
- Flyway
- Lombok
- MapStruct
- JUnit 5
- Mockito
- Testcontainers
- JaCoCo

## Estrutura
O projeto segue um modelo em camadas inspirado em Clean Architecture:

```text
src/main/java/com/postech/workshop_service/
├── api/
│   ├── controllers/
│   └── dtos/
├── application/
│   └── usecases/
├── domain/
│   ├── entities/
│   ├── repositories/
│   └── valueobjects/
└── infrastructure/
    ├── config/
    └── persistence/
        ├── entities/
        ├── mappers/
        └── repositories/
```

## Funcionalidades Atuais

- Cadastro, consulta, atualização e remoção de clientes
- Cadastro, consulta, atualização e remoção logica de veículos
- Vínculo de um veículo com múltiplos clientes equivalentes
- Busca de veículos por ID, placa e cliente
- Listagem paginada de veículos com filtro de inativos
- Reutilização de placa após remoção logica do cadastro anterior

## Segurança
- Autenticação baseada em JWT
- Configuração obrigatória de segredo (JWT_SECRET)
- Estrutura preparada para RBAC (Role-Based Access Control)

Detalhes: [Fluxo de autenticação e autorização JWT](docs/autenticacao-jwt-rbac/README.md)

## Execução Local

### Pré-requisitos

- Java 21
- Maven 3.9+
- Docker Desktop ativo

### Banco de dados

```bash
docker compose up -d
```

## Rodando com Docker

Copie `.env.example` para `.env` se quiser ajustar credenciais locais.

```bash
docker compose up -d
```

A aplicacao fica disponivel em `http://localhost:8080`.

Health check:

```bash
curl http://localhost:8080/actuator/health
```

### Testes

```bash
mvn test
```

### Aplicação

Antes de iniciar a API, defina a variavel de ambiente `JWT_SECRET` com um segredo de pelo menos 32 caracteres.

Exemplo no PowerShell:

```powershell
$env:JWT_SECRET="defina-um-segredo-com-pelo-menos-32-caracteres"
mvn spring-boot:run
```

Sem essa variavel a aplicação falha no startup por segurança.

Exemplo alternativo passando pela linha de comando:

```powershell
mvn spring-boot:run "-Dspring-boot.run.arguments=--JWT_SECRET=defina-um-segredo-com-pelo-menos-32-caracteres"
```

```bash
mvn spring-boot:run
```

### Documentação OpenAPI

- `http://localhost:8080/swagger-ui/index.html`

## Observações do MVP

- Os endpoints de veículos estão liberados no MVP e preparados para futura restrição de acesso.
- A remoção de veículos e lógica, preservando rastreabilidade e referências históricas.


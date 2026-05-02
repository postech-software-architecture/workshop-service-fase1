# Workshop Service - Fase 1

Servico Spring Boot para gestao de cadastros base de uma oficina, com foco atual em clientes e veiculos vinculados a um ou mais clientes.

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

- Cadastro, consulta, atualizacao e remocao de clientes
- Cadastro, consulta, atualizacao e remocao logica de veiculos
- Vinculo de um veiculo com multiplos clientes equivalentes
- Busca de veiculos por ID, placa e cliente
- Listagem paginada de veiculos com filtro de inativos
- Reutilizacao de placa apos remocao logica do cadastro anterior

## Execucao Local

### Pre-requisitos

- Java 21
- Maven 3.9+
- Docker Desktop ativo

### Banco de dados

```bash
docker compose up -d
```

### Testes

```bash
mvn test
```

### Aplicacao

```bash
mvn spring-boot:run
```

### Documentacao OpenAPI

- `http://localhost:8080/swagger-ui/index.html`

## Observacoes do MVP

- Os endpoints de veiculos estao liberados no MVP e preparados para futura restricao de acesso.
- A remocao de veiculos e logica, preservando rastreabilidade e referencias historicas.

## Documentacao

- Fluxo detalhado de autenticacao e autorizacao JWT: [docs/autenticacao-jwt-rbac/README.md](docs/autenticacao-jwt-rbac/README.md)

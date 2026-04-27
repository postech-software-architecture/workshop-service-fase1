# Workshop Service - Fase 1

Este projeto é um microserviço desenvolvido como parte da Fase 1 da Pós-Graduação em Software Architecture (FIAP). O objetivo é construir o módulo de **Cadastros Base** de um sistema de gestão para oficinas mecânicas, aplicando os princípios de **Domain-Driven Design (DDD)**, **Clean Architecture** e práticas modernas de desenvolvimento Java com **Spring Boot**.

## 🚀 Tecnologias Utilizadas

- **Java 21**
- **Spring Boot 3.4+**
- **Spring Data JPA** & **Hibernate**
- **PostgreSQL**
- **Flyway** (Migrações de Banco de Dados)
- **MapStruct** (Mapeamento de Objetos)
- **Lombok** (Produtividade)
- **SpringDoc OpenAPI** (Swagger UI)
- **JUnit 5** & **Mockito** (Testes Unitários)
- **Testcontainers** (Testes de Integração com Docker)
- **JaCoCo** (Cobertura de Testes)

## 📁 Estrutura de Pastas

O projeto segue uma estrutura baseada em camadas, respeitando os princípios do DDD:

```text
src/main/java/com/postech/workshop_service/
├── api/                        # Camada de Interface (REST)
│   ├── controllers/            # Endpoints da API
│   └── dtos/                   # Objetos de Transferência de Dados (Request/Response)
├── application/                # Camada de Aplicação
│   └── usecases/               # Lógica de coordenação e casos de uso de negócio
├── domain/                     # Camada de Domínio (Core - Independente de Framework)
│   ├── entities/               # Entidades de Domínio (ex: Cliente, Endereco)
│   ├── valueobjects/           # Objetos de Valor (ex: Documento)
│   └── repositories/           # Interfaces de Repositório
└── infrastructure/             # Camada de Infraestrutura
    ├── config/                 # Configurações do Spring (Security, Swagger, etc.)
    └── persistence/            # Implementação de persistência
        ├── entities/           # Entidades JPA (Mapeamento de Banco)
        ├── mappers/            # Interfaces MapStruct para conversão Domínio <-> JPA
        └── repositories/       # Implementação JPA dos Repositórios de Domínio
```

## 🛠️ Como Inicializar o Projeto

### Pré-requisitos
- **Java 21** instalado.
- **Docker** e **Docker Compose** ativos (necessário para o banco de dados e execução de testes de integração).

### Passos para execução

1.  **Clonar o repositório:**
    ```bash
    git clone https://github.com/seu-usuario/workshop-service-fase1.git
    cd workshop-service-fase1
    ```

2.  **Subir o banco de dados (PostgreSQL):**
    O projeto utiliza o `docker-compose.yaml` para subir a instância do banco:
    ```bash
    docker-compose up -d
    ```

3.  **Compilar e Rodar os Testes:**
    Este comando baixa as dependências, compila o código e executa todos os testes unitários e de integração (usando Testcontainers):
    ```bash
    ./mvnw clean verify
    ```

4.  **Executar a aplicação:**
    ```bash
    ./mvnw spring-boot:run
    ```

5.  **Acessar a documentação (Swagger):**
    Após iniciar, a API estará disponível em: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

## 📊 Cobertura de Testes

O projeto está configurado com **JaCoCo** para garantir a qualidade do código. O objetivo é manter **100% de cobertura** nas camadas de Domínio e Aplicação.
Para gerar o relatório de cobertura, execute:
```bash
./mvnw test
```
O relatório estará disponível em: `target/site/jacoco/index.html`

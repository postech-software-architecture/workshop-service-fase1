# Workshop Service - Fase 1

> MVP de back-end para gestão de oficinas mecânicas, desenvolvido com foco em Domain-Driven Design (DDD), rastreabilidade operacional e evolução contínua do domínio.

---

## Sobre o Projeto

O Workshop Service foi desenvolvido como parte do **Tech Challenge - Fase 1** da Pós-Graduação em Software Architecture da POSTECH.

O projeto busca estruturar digitalmente processos operacionais de oficinas mecânicas, reduzindo problemas relacionados à rastreabilidade, controle de informações e padronização dos atendimentos.

A solução foi construída como um MVP de back-end voltado à gestão de clientes e veículos, estabelecendo uma base arquitetural preparada para futuras expansões do domínio, como:

- Ordens de Serviço
- Diagnóstico técnico
- Orçamentos
- Controle de estoque
- Fluxo de execução
- Rastreamento operacional

---

## Objetivo do Projeto

Centralizar o gerenciamento de clientes e veículos em um sistema estruturado, garantindo:

- Consistência dos dados
- Rastreabilidade das operações
- Padronização das informações
- Base sólida para evolução contínua do domínio

---

## Arquitetura e Modelagem de Domínio

O projeto foi estruturado com base em conceitos de **Domain-Driven Design (DDD)**, buscando alinhar regras de negócio, comunicação técnica e evolução arquitetural.

### Conceitos aplicados

- Linguagem Ubíqua
- Separação entre Core Domain e Supporting Domain
- Arquitetura em camadas
- Organização orientada à evolução do domínio
- Preservação de histórico através de remoção lógica

### Arquitetura em Camadas

```text
┌─────────────────────────────────┐
│ API / Presentation Layer        │
├─────────────────────────────────┤
│ Application Layer               │
├─────────────────────────────────┤
│ Domain Layer                    │
├─────────────────────────────────┤
│ Infrastructure Layer            │
└─────────────────────────────────┘
```

---

## Artefatos de Domínio
- [Dicionário de Linguagem Ubíqua](docs/linguagem-ubiqua/dicionario_linguagem_ubiqua_completo.pdf)
- [Diagramas Domain Storytelling](/docs/domain_storytelling)
- [Diagramas Event Storming](docs/event_storming/README.md)

---
## Funcionalidades Principais

- Gestão de Clientes e Veículos
- Gestão de Ordens de Serviço
- Diagnóstico e Orçamento
- Fluxo de Aprovação e Execução
- Acompanhamento Operacional
- Gestão Administrativa
- Autenticação e Segurança

---

## Tecnologias Utilizadas
### Backend:
[![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.java.com/) [![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.4.1-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot) [![Spring Web](https://img.shields.io/badge/Spring_Web-6DB33F?style=for-the-badge)](https://spring.io/projects/spring-framework) [![Spring Data JPA](https://img.shields.io/badge/Spring_Data_JPA-6DB33F?style=for-the-badge)](https://spring.io/projects/spring-data-jpa) [![Spring Security](https://img.shields.io/badge/Spring_Security-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white)](https://spring.io/projects/spring-security)

### Qualidade e Testes:
[![JUnit5](https://img.shields.io/badge/JUnit_5-25A162?style=for-the-badge&logo=junit5&logoColor=white)](https://junit.org/junit5/) [![Mockito](https://img.shields.io/badge/Mockito-78A641?style=for-the-badge)](https://site.mockito.org/) [![Testcontainers](https://img.shields.io/badge/Testcontainers-2496ED?style=for-the-badge&logo=docker&logoColor=white)](https://testcontainers.com/) [![JaCoCo](https://img.shields.io/badge/JaCoCo-Coverage-red?style=for-the-badge)](https://www.jacoco.org/)

### Banco de Dados:
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org/) [![Flyway](https://img.shields.io/badge/Flyway-CC0200?style=for-the-badge&logo=flyway&logoColor=white)](https://flywaydb.org/)

### Produtividade:
[![Lombok](https://img.shields.io/badge/Lombok-BC4521?style=for-the-badge)](https://projectlombok.org/) [![MapStruct](https://img.shields.io/badge/MapStruct-009688?style=for-the-badge)](https://mapstruct.org/)

---

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
---

## Segurança
- Autenticação baseada em JWT
- Configuração obrigatória de segredo (JWT_SECRET)
- Estrutura preparada para RBAC (Role-Based Access Control)

Detalhes: [Fluxo de autenticação e autorização JWT](docs/autenticacao-jwt-rbac/README.md)

## Event Storming
- Diagramas de apoio para modelagem dos fluxos centrais do domínio
- Visões separadas entre fluxo de negócio e análise com agregados

Detalhes: [Artefatos de Event Storming](docs/event_storming/README.md)

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

A aplicação fica disponível em `http://localhost:8080`.

Health check:

```bash
curl http://localhost:8080/actuator/health
```

### Testes

```bash
mvn test
```

### Aplicação

Antes de iniciar a API, defina a variável de ambiente `JWT_SECRET` com um segredo de pelo menos 32 caracteres.

Exemplo no PowerShell:

```powershell
$env:JWT_SECRET="defina-um-segredo-com-pelo-menos-32-caracteres"
mvn spring-boot:run
```

Sem essa variável a aplicação falha no startup por segurança.

Exemplo alternativo passando pela linha de comando:

```powershell
mvn spring-boot:run "-Dspring-boot.run.arguments=--JWT_SECRET=defina-um-segredo-com-pelo-menos-32-caracteres"
```

```bash
mvn spring-boot:run
```

### Documentação OpenAPI

- Swagger UI local: `http://localhost:8080/swagger-ui/index.html`
- Contrato OpenAPI versionado no repositório: `src/main/java/com/postech/workshop_service/api/controllers/openapi.yaml`

A documentação cobre os fluxos de autenticação, clientes, veículos, serviços, peças e insumos, estoque, ordens de serviço, orçamentos e métricas administrativas. Para acessar o Swagger UI localmente, suba a aplicação com `JWT_SECRET` configurado conforme a seção de execução da aplicação.

## Observações do MVP

- Os endpoints da API usam autenticação JWT e controle de acesso por perfil conforme descrito no contrato OpenAPI.
- A remoção de veículos é lógica, preservando rastreabilidade e referências históricas.
- O projeto representa a primeira fase da construção do domínio da oficina mecânica.

---
## Equipe

Projeto desenvolvido como parte do Tech Challenge da POSTECH da Turma 15SOAT.

<p align="center">
<a href="https://github.com/jeanrabello" title="@jeanrabello"><img src="https://github.com/jeanrabello.png" width="80px;" alt="Jean"/></a>&nbsp;&nbsp;&nbsp;
<a href="https://github.com/MahAmorim" title="@MahAmorim"><img src="https://github.com/MahAmorim.png" width="80px;" alt="Marcela"/></a>&nbsp;&nbsp;&nbsp;
<a href="https://github.com/tassyo" title="@tassyo"><img src="https://github.com/tassyo.png" width="80px;" alt="Tassyo"/></a>&nbsp;&nbsp;&nbsp;
<a href="https://github.com/ssayori" title="@ssayori"><img src="https://github.com/ssayori.png" width="80px;" alt="Suzana"/></a>&nbsp;&nbsp;&nbsp;
<a href="https://github.com/mateus-paz" title="@mateus-paz"><img src="https://github.com/mateus-paz.png" width="80px;" alt="Mateus"/></a>
</p>

<p align="center">
<sub>
Jean Paes Rabello •
Marcela Amorim •
Tassyo Monteiro •
Suzana Sayori •
Mateus Paz de Oliveira
</sub>
</p>

---

## Demonstração

Em breve: vídeo demonstrativo da aplicação e dos fluxos principais do sistema.

---

## Artefatos e Documentação

- [Linguagem Ubíqua](docs/linguagem-ubiqua/dicionario_linguagem_ubiqua_completo.pdf)
- [Domain Storytelling](/docs/domain_storytelling)
- [Fluxo JWT e RBAC](docs/autenticacao-jwt-rbac/README.md)
- [Event Storming](docs/event_storming/README.md)
- [Documento de Entrega](https://docs.google.com/document/d/1AG1UIgyuAokcU_JAC2bleT4IUDj0p0lU/edit?usp=sharing&ouid=118260149834728254325&rtpof=true&sd=true)

---

## Suporte

Caso encontre algum problema ou deseje contribuir com sugestões:

- Abra uma issue no repositório
- Entre em contato com a equipe

---

<p align="center">
  <sub>
    Desenvolvido pelo Grupo 274 • Turma 15SOAT • POSTECH
  </sub>
</p>

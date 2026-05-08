# Implementation Plan: Ciclo de Execucao da Ordem de Servico

**Branch**: `008-os-execution-cycle` | **Date**: 2026-05-07 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/008-os-execution-cycle/spec.md`

## Summary

Completar o ciclo operacional da ordem de servico apos aprovacao do orcamento, adicionando os estados `EM_EXECUCAO` e `ENTREGUE`, timestamps do ciclo, historico auditavel de status, tres transicoes operacionais e uma consulta autenticada do historico da OS. A implementacao seguira o desenho atual em camadas: regras no dominio, orquestracao em casos de uso, persistencia via repositorios/adaptadores e contratos HTTP documentados no controller e no OpenAPI estatico.

## Technical Context

**Language/Version**: Java 21  
**Primary Dependencies**: Spring Boot 3.4.1, Spring Web, Spring Data JPA, Spring Validation, Spring Security, Lombok, MapStruct, Flyway, SpringDoc OpenAPI  
**Storage**: PostgreSQL com Flyway; requer evolucao de `ordens_servico` e nova estrutura de historico de status  
**Testing**: JUnit 5, Mockito, Spring Boot Test, Spring Security Test, Testcontainers; foco em testes unitarios de dominio/use cases e integracao de controller/repository  
**Target Platform**: Servico backend Spring Boot executado em JVM  
**Project Type**: web-service em arquitetura em camadas  
**Performance Goals**: Cada transicao de status deve ocorrer em uma unica transacao curta, com uma gravacao da OS e uma gravacao de historico; consulta de historico por OS deve retornar uma linha do tempo operacional em ordem cronologica sem impactar o fluxo principal  
**Constraints**: Codigo em pt-BR, dominio rico sem setters genericos para status, Javadoc em metodos publicos, OpenAPI atualizado, migrations Flyway no padrao `V0.YYYYMMDDHHMMSS__descricao.sql`, sem baixa de estoque nesta feature, sem backfill retroativo de historico  
**Scale/Scope**: Evolucao de 1 agregado existente, 2 novos estados, 3 timestamps, 3 casos de uso de transicao, 1 caso de uso de consulta, 1 entidade de historico, 4 endpoints HTTP, atualizacao de contratos e testes para fluxo completo

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- [x] **Idioma e Plataforma**: Java 21 preservado; nomes novos devem ser em pt-BR, mantendo `UseCase`, `Controller`, `Repository` e `Service` como sufixos tecnicos ja aceitos no projeto.
- [x] **Padroes de Codigo**: Lombok sera usado onde o projeto ja usa; entidades de dominio continuarao sem setters genericos para transicoes de status.
- [x] **Arquitetura**: Controllers apenas traduzem chamadas HTTP; casos de uso orquestram; dominio valida invariantes; infraestrutura persiste.
- [x] **Dominio (DDD)**: `OrdemServico` permanece agregado rico, com metodos de negocio para iniciar execucao, finalizar e entregar.
- [x] **Documentacao**: Metodos publicos novos devem ter Javadoc e os endpoints devem ser documentados por anotacoes e no OpenAPI estatico existente.
- [x] **Testes**: Comportamentos publicos novos exigem testes unitarios e pelo menos um fluxo de integracao.
- [x] **Validacao e Padroes de Erro**: Regras de negocio usarao o padrao atual do projeto (`RegraDeNegocioException` convertida para 422), apesar da constituicao mencionar 400 para regra de negocio; alterar a politica global de erro fica fora do escopo desta feature.
- [x] **Banco de Dados**: Migrations novas devem ter UUID, auditoria quando aplicavel, comentarios SQL e FKs nomeadas.

## Project Structure

### Documentation (this feature)

```text
specs/008-os-execution-cycle/
|-- plan.md
|-- research.md
|-- data-model.md
|-- quickstart.md
|-- contracts/
|   `-- README.md
|-- checklists/
|   `-- requirements.md
`-- tasks.md
```

### Source Code (repository root)

```text
src/
|-- main/
|   |-- java/com/postech/workshop_service/
|   |   |-- api/
|   |   |   |-- controllers/
|   |   |   `-- dtos/
|   |   |-- application/
|   |   |   `-- usecases/
|   |   |-- domain/
|   |   |   |-- entities/
|   |   |   `-- repositories/
|   |   `-- infrastructure/
|   |       `-- persistence/
|   |           |-- entities/
|   |           |-- mappers/
|   |           `-- repositories/
|   `-- resources/
|       `-- db/migration/
`-- test/
    `-- java/com/postech/workshop_service/
        |-- api/controllers/
        |-- application/usecases/
        |-- domain/entities/
        `-- infrastructure/persistence/repositories/
```

**Structure Decision**: Manter a estrutura Spring Boot atual em camadas. A feature toca `domain/entities` para a maquina de estados, `application/usecases` para orquestracao e consulta, `domain/repositories` para o contrato de historico, `infrastructure/persistence` para entidade/mapeador/repositorio JPA, `api/controllers` e `api/dtos` para contratos HTTP e `src/main/resources/db/migration` para evolucao de schema.

## Phase 0: Research Summary

- A semantica dos estados fica fechada como: `EM_EXECUCAO` representa trabalho tecnico iniciado; `FINALIZADA` representa trabalho concluido e veiculo pronto; `ENTREGUE` representa retirada pelo cliente e encerramento operacional.
- Os timestamps devem existir na OS para leitura operacional direta e o historico deve existir como trilha auditavel e fonte futura de metricas.
- Historico de status deve ser entidade propria, vinculada a OS e ao usuario responsavel, sem acoplar o dominio a detalhes de seguranca ou persistencia.
- O responsavel pela transicao deve ser capturado no caso de uso a partir do principal autenticado e gravado como identificador e nome de usuario para auditoria legivel.
- Transicoes ja existentes devem passar pelo mesmo registrador de historico para que a linha do tempo fique completa a partir desta feature.
- O historico deve ser consultavel por OS para `ADMINISTRADOR`, `MECANICO` e `ATENDENTE`, que sao os perfis internos envolvidos nas transicoes.
- Ordens existentes nao terao backfill retroativo; a consulta de historico retornara apenas transicoes registradas apos a implantacao desta feature.
- Baixa de estoque fica explicitamente fora do escopo; o inicio de execucao apenas muda status e registra historico.

## Phase 1: Design Summary

- Evoluir `StatusOrdemServico` adicionando `EM_EXECUCAO` e `ENTREGUE`.
- Evoluir `OrdemServico` com `iniciarExecucao()`, `finalizarExecucao()` e `entregar()`, validando estado anterior, atualizando status, timestamp especifico e `dataUltimaAtualizacao`.
- Adicionar `dataInicioExecucao`, `dataFinalizacao` e `dataEntrega` ao agregado e a persistencia de `ordens_servico`.
- Criar `HistoricoStatusOrdemServico` no dominio, com status anterior, status novo, data da transicao, usuario responsavel e identificador da OS.
- Criar `HistoricoStatusOrdemServicoRepository` no dominio e adaptador JPA correspondente.
- Criar um servico de aplicacao para registrar historico de transicao, reutilizado pelos novos casos de uso e pelos fluxos existentes de aprovacao/rejeicao/cancelamento/encerramento de composicao.
- Criar `IniciarExecucaoUseCase`, `FinalizarExecucaoUseCase` e `EntregarVeiculoUseCase`, todos transacionais.
- Criar `ConsultarHistoricoOrdemServicoUseCase` para validar existencia da OS e retornar historico em ordem cronologica.
- Atualizar `OrdemServicoController` com tres transicoes e consulta de historico, incluindo autorizacoes por perfil.
- Criar DTO de historico e atualizar `OrdemServicoResponse` para expor os novos timestamps.
- Atualizar `src/main/java/com/postech/workshop_service/api/controllers/openapi.yaml` com endpoints, status e campos novos.
- Cobrir maquina de estados, casos de uso, persistencia do historico, consulta e controller com testes automatizados.

## Phase 2: Implementation Preview

1. Criar migration para novos status, timestamps na OS e tabela `historico_status_os`.
2. Evoluir dominio de OS com novos estados, timestamps e transicoes.
3. Criar dominio, contrato e persistencia de historico de status.
4. Criar componente de aplicacao para registrar historico com usuario autenticado.
5. Implementar casos de uso de iniciar execucao, finalizar execucao e entregar veiculo.
6. Implementar consulta autenticada de historico por OS.
7. Conectar historico aos fluxos existentes de composicao, aprovacao, rejeicao e cancelamento, sem backfill retroativo.
8. Atualizar controller, DTOs e OpenAPI.
9. Adicionar testes unitarios e integrados para transicoes validas, bloqueios, autorizacao, consulta de historico e ausencia de historico retroativo.

## Complexity Tracking

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| Politica HTTP atual usa 422 para regra de negocio, enquanto a constituicao menciona 400 | O projeto ja esta padronizado no `GlobalExceptionHandler` e em testes existentes para regra de negocio como 422 | Alterar a politica global nesta feature criaria regressao ampla e fora do escopo do ciclo de OS |

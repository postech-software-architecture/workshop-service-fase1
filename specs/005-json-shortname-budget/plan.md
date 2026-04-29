# Implementation Plan: Encerramento de Composicao Tecnica e Fluxo de Orcamento

**Branch**: `005-json-shortname-budget` | **Date**: 2026-04-29 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/005-json-shortname-budget/spec.md`

## Summary

Implementar os casos de uso `EncerrarComposicaoTecnicaUseCase`, `AprovarOrcamentoUseCase`, `RejeitarOrcamentoUseCase` e `CancelarOrcamentoUseCase` seguindo a arquitetura atual em camadas do projeto. A feature exige evoluir o dominio de `OrdemServico` e `Orcamento`, introduzir itens de composicao tecnica na ordem, adicionar persistencia dedicada para ordem e orcamento com Flyway/JPA/MapStruct, e criar services de notificacao por log para cliente e mecanico.

## Technical Context

**Language/Version**: Java 21  
**Primary Dependencies**: Spring Boot 3.4.1, Spring Data JPA, Spring Validation, Spring Security, Lombok, Flyway, MapStruct  
**Storage**: PostgreSQL com Flyway; esta feature requer novas tabelas e relacionamento entre ordem, itens de composicao e orcamento  
**Testing**: JUnit 5, Spring Boot Test, Mockito, Testcontainers; foco principal em testes unitarios de dominio e casos de uso  
**Target Platform**: Servico backend Spring Boot executado em JVM  
**Project Type**: web-service em arquitetura em camadas  
**Performance Goals**: Transicoes de fluxo de ordem e orcamento devem ocorrer em uma unica transacao, com latencia desprezivel em nivel de dominio e persistencia simples por agregado  
**Constraints**: Codigo em pt-BR, dominio rico, sem dependencia do dominio para infraestrutura, Javadoc em metodos publicos, sem envio real de e-mail, sem aprovacao parcial, sem PDF, sem pagamento e sem estoque  
**Scale/Scope**: Evolucao de 2 agregados existentes, 1 novo objeto de composicao tecnica na ordem, 4 novos casos de uso, 2 services de notificacao, 2 novos repositorios de dominio, camada JPA completa e testes unitarios cobrindo regras do fluxo

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- [x] **Idioma e Plataforma**: O plano preserva Java 21 e nomes de classes, metodos e mensagens internas em pt-BR, mantendo apenas o sufixo `UseCase` e `Service` como excecao aceitavel do projeto.
- [x] **Padroes de Codigo**: Lombok sera usado para reduzir boilerplate, sem expor setters genericos em entidades de dominio; alteracoes de estado ocorrerao por comportamentos explicitos.
- [x] **Arquitetura**: A implementacao seguira camadas separadas entre aplicacao, dominio e infraestrutura, com casos de uso orquestrando repositorios e services sem contaminar o dominio com detalhes JPA ou logging.
- [x] **Dominio (DDD)**: `OrdemServico` e `Orcamento` permanecerao como agregados ricos, encapsulando transicoes, validacoes e invariantes do fluxo de composicao e aprovacao.
- [x] **Documentacao**: Metodos publicos novos em dominio, aplicacao, repositorios e services devem manter Javadoc; nao ha endpoints novos previstos nesta etapa.
- [x] **Testes**: Todos os casos de uso publicos novos e comportamentos publicos de dominio ajustados terao cobertura automatizada por testes unitarios.
- [x] **Validacao e Padroes de Erro**: Violacoes de regra de negocio continuarao sendo sinalizadas por excecoes de negocio na aplicacao/dominio; a conversao HTTP permanece responsabilidade futura da API.
- [x] **Banco de Dados**: A feature requer migrations Flyway novas com UUID, campos de auditoria, comentarios SQL e chaves estrangeiras nomeadas no padrao da constituicao.

## Project Structure

### Documentation (this feature)

```text
specs/005-json-shortname-budget/
|-- plan.md
|-- research.md
|-- data-model.md
|-- quickstart.md
|-- contracts/
|   `-- README.md
`-- tasks.md
```

### Source Code (repository root)

```text
src/
|-- main/
|   |-- java/com/postech/workshop_service/
|   |   |-- api/
|   |   |-- application/
|   |   |   |-- exceptions/
|   |   |   `-- usecases/
|   |   |-- domain/
|   |   |   |-- entities/
|   |   |   |-- repositories/
|   |   |   `-- valueobjects/
|   |   `-- infrastructure/
|   |       `-- persistence/
|   |           |-- entities/
|   |           |-- mappers/
|   |           `-- repositories/
|   `-- resources/
|       `-- db/migration/
`-- test/
    `-- java/com/postech/workshop_service/
        |-- application/usecases/
        |-- domain/entities/
        `-- infrastructure/
```

**Structure Decision**: Manter a estrutura Spring Boot atual em camadas. Esta feature adicionara comportamento no dominio em `domain/entities`, contratos de persistencia em `domain/repositories`, casos de uso e services de notificacao em `application/usecases`, e persistencia JPA completa em `infrastructure/persistence/{entities,mappers,repositories}`. Nao ha necessidade de novos controllers nesta fase.

## Phase 0: Research Summary

- O fluxo pedido nao cabe apenas no dominio atual: faltam itens de composicao em `OrdemServico`, repositorios de `OrdemServico`/`Orcamento` e persistencia correspondente; portanto a implementacao precisa atravessar dominio, aplicacao e infraestrutura.
- Os estados atuais de `OrdemServico` (`RECEBIDA`, `AGUARDANDO_APROVACAO_ORCAMENTO`, `EM_EXECUCAO`) nao representam o fluxo do MVP; o plano deve migrar o modelo para `EM_COMPOSICAO`, `AGUARDANDO_RESPOSTA_CLIENTE`, `AGUARDANDO_EXECUCAO`, `CANCELADA` e manter compatibilidade dos testes com a nova semantica.
- A fotografia do orcamento nao deve reutilizar diretamente os itens mutaveis da ordem; o orcamento precisa armazenar sua propria copia imutavel dos itens, preservando historico mesmo apos rejeicao e nova composicao.
- Como nao existem endpoints solicitados, os contratos desta feature sao internos de aplicacao e persistencia; a exposicao HTTP pode ficar para incremento futuro sem bloquear os casos de uso.
- Os services de notificacao devem ser portas simples de aplicacao com implementacao inicial via logging, evitando acoplamento prematuro com e-mail real.

## Phase 1: Design Summary

- Evoluir `OrdemServico` para carregar itens de composicao tecnica, expor `encerrarComposicao()`, `voltarParaComposicao()`, `marcarComoAguardandoExecucao()` e `cancelar()` e validar transicoes apenas por comportamento de negocio.
- Evoluir `Orcamento` para desacoplar a aprovacao/rejeicao/cancelamento de efeitos implicitos antigos e alinhar seus metodos `aprovar()`, `rejeitar()` e `cancelar()` ao fluxo em que a aplicacao coordena a ordem vinculada.
- Introduzir um tipo de dominio para item de composicao da ordem com classificacao `SERVICO`, `PECA` e `INSUMO`, e manter `ItemOrcamento` como fotografia copiada.
- Criar `OrdemServicoRepository` e `OrcamentoRepository` no dominio, com implementacoes JPA e mapeadores dedicados na infraestrutura.
- Criar as entidades JPA e migrations para `ordens_servico`, `ordens_servico_itens`, `orcamentos` e `orcamentos_itens`, incluindo auditoria e restricao para impedir mais de um orcamento `PENDENTE_APROVACAO` por ordem.
- Implementar os quatro casos de uso como services transacionais na aplicacao, delegando validacoes de estado ao dominio, consulta de unicidade ao repositorio de orcamento e notificacao aos services apropriados.
- Cobrir o comportamento com testes unitarios de use case usando mocks de repositorio/notificacao e atualizar testes de dominio impactados pelos novos estados.

## Phase 2: Implementation Preview

1. Ajustar enums e entidades de dominio de `OrdemServico` para suportar o novo ciclo de vida e a colecao de itens de composicao tecnica.
2. Ajustar `Orcamento` e `ItemOrcamento` para refletir fotografia de proposta e transicoes alinhadas ao novo fluxo.
3. Criar contratos de dominio `OrdemServicoRepository` e `OrcamentoRepository`.
4. Criar os services `ClienteNotificationService` e `MecanicoNotificationService` como portas de aplicacao com implementacao inicial baseada em log.
5. Implementar `EncerrarComposicaoTecnicaUseCase`, `AprovarOrcamentoUseCase`, `RejeitarOrcamentoUseCase` e `CancelarOrcamentoUseCase` como services transacionais.
6. Criar migrations Flyway, entidades JPA, mapeadores MapStruct e repositorios Spring Data para ordem, itens de ordem, orcamento e itens de orcamento.
7. Adicionar testes unitarios dos casos de uso cobrindo os cenarios listados na especificacao e no pedido do usuario.
8. Atualizar testes de dominio de `OrdemServico` e `Orcamento` para refletir as novas transicoes e remover a semantica antiga que conflita com o MVP.

## Complexity Tracking

Nenhuma violacao de constituicao prevista.

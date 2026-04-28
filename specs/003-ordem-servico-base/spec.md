# Feature Specification: Entidade Base de Ordem de Servico

**Feature Branch**: `003-ordem-servico-base`  
**Created**: 2026-04-28  
**Status**: Draft  
**Input**: User description: "Criar a entidade de dominio OrdemServico, ainda sem fluxo de CRUD, controllers ou casos de uso completos."

## Clarifications

### Session 2026-04-28

- Q: Qual deve ser o status inicial de uma nova OrdemServico? -> A: RECEBIDA

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Definir uma ordem de servico rastreavel (Priority: P1)

Como responsavel pelas regras de negocio da oficina, quero que exista uma ordem de servico vinculada a um cliente e a um veiculo, para que futuros processos, como orcamento e execucao, reutilizem um conceito unico e consistente.

**Why this priority**: Sem esse conceito base, os proximos fluxos precisam reinventar a identificacao da ordem de servico e suas associacoes essenciais, aumentando inconsistencias de regras e dados.

**Independent Test**: Pode ser testada validando que uma ordem de servico sempre contem identificacao propria, referencia ao cliente, referencia ao veiculo e nasce com o status inicial previsto.

**Acceptance Scenarios**:

1. **Given** a necessidade de registrar uma nova ordem de servico, **When** a ordem e definida, **Then** ela inclui um identificador proprio, uma referencia ao cliente, uma referencia ao veiculo e nasce com o status recebida.
2. **Given** que outros processos dependem da ordem de servico, **When** consultam a definicao da entidade, **Then** encontram um conceito unico para representar a ordem vinculada ao cliente e ao veiculo.

---

### User Story 2 - Controlar estados iniciais da ordem (Priority: P2)

Como responsavel pela operacao da oficina, quero que a ordem de servico aceite apenas estados de negocio previstos, para que o ciclo de vida inicial da OS seja claro e consistente desde seu recebimento.

**Why this priority**: A padronizacao de estados reduz ambiguidade para os proximos agregados e evita interpretacoes diferentes sobre o momento da ordem no processo da oficina.

**Independent Test**: Pode ser testada verificando que a ordem de servico reconhece somente os estados definidos para esta etapa e que novas ordens sempre comecam no estado recebida.

**Acceptance Scenarios**:

1. **Given** uma ordem de servico existente, **When** seu estado e avaliado, **Then** ele corresponde a um dos estados de negocio previstos para esta etapa.
2. **Given** a criacao de uma nova ordem de servico, **When** ela e inicializada, **Then** seu status inicial e recebida.
3. **Given** a evolucao futura do processo operacional, **When** outros modulos utilizam o status da ordem, **Then** eles conseguem diferenciar claramente recebimento, espera por aprovacao de orcamento, execucao, cancelamento e finalizacao.

---

### User Story 3 - Aplicar regra de cancelamento (Priority: P3)

Como responsavel pelas regras da oficina, quero que o cancelamento da ordem de servico respeite o status atual, para impedir cancelamentos indevidos depois que a execucao comecou ou a ordem ja foi encerrada.

**Why this priority**: A regra de cancelamento protege a integridade do processo e evita que estados irreversiveis sejam alterados sem controle.

**Independent Test**: Pode ser testada verificando que ordens em estados cancelaveis mudam para cancelada e que ordens em estados nao cancelaveis bloqueiam a operacao com erro de dominio.

**Acceptance Scenarios**:

1. **Given** uma ordem de servico em estado recebida, **When** o cancelamento e solicitado, **Then** a ordem passa para o estado cancelada.
2. **Given** uma ordem de servico aguardando aprovacao de orcamento, **When** o cancelamento e solicitado, **Then** a ordem passa para o estado cancelada.
3. **Given** uma ordem de servico em execucao, finalizada ou ja cancelada, **When** o cancelamento e solicitado, **Then** a operacao e impedida com erro de dominio e o status permanece inalterado.

---

### Edge Cases

- O que acontece quando uma tentativa de cancelamento e feita sobre uma ordem ja cancelada?
- Como o dominio deve reagir se houver tentativa de cancelamento apos o inicio da execucao?
- Como garantir que uma ordem sem vinculo de cliente ou sem vinculo de veiculo nao seja tratada como valida nesta definicao base?
- O que acontece se algum processo futuro tentar usar um status fora da lista definida para esta etapa?

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: O sistema DEVE definir uma entidade de ordem de servico que represente uma ordem vinculada a um cliente e a um veiculo.
- **FR-002**: O sistema DEVE garantir que cada ordem de servico possua, no minimo, identificador proprio, identificador do cliente, identificador do veiculo e status.
- **FR-003**: O sistema DEVE reconhecer, nesta etapa, apenas os seguintes status para a ordem de servico: recebida, aguardando aprovacao de orcamento, em execucao, cancelada e finalizada.
- **FR-004**: O sistema DEVE definir que toda nova ordem de servico nasce com o status recebida.
- **FR-005**: O sistema DEVE informar se uma ordem de servico pode ser cancelada com base em seu status atual.
- **FR-006**: O sistema DEVE permitir cancelamento apenas para ordens de servico nos estados recebida e aguardando aprovacao de orcamento.
- **FR-007**: O sistema DEVE impedir o cancelamento de ordens de servico nos estados em execucao, finalizada ou ja cancelada.
- **FR-008**: Quando o cancelamento for permitido, o sistema DEVE alterar o status da ordem de servico para cancelada.
- **FR-009**: Quando o cancelamento nao for permitido, o sistema DEVE impedir a operacao com erro de dominio e manter o status atual inalterado.
- **FR-010**: Esta etapa NAO DEVE incluir fluxos completos de cadastro, consulta, alteracao ou exclusao da ordem de servico.
- **FR-011**: Esta etapa NAO DEVE incluir controladores, endpoints publicos ou casos de uso completos dedicados a ordem de servico.

### Key Entities *(include if feature involves data)*

- **Ordem de Servico**: Representa a ordem central usada pela oficina para associar um servico a um cliente e a um veiculo. Possui identificador proprio, referencia ao cliente, referencia ao veiculo e status de negocio.
- **Status da Ordem de Servico**: Representa o estagio de negocio atual da ordem, limitado nesta etapa aos estados recebida, aguardando aprovacao de orcamento, em execucao, cancelada e finalizada.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% das ordens de servico definidas nesta etapa incluem identificacao da ordem, identificacao do cliente, identificacao do veiculo e status.
- **SC-002**: 100% das novas ordens de servico definidas nesta etapa nascem com o status recebida.
- **SC-003**: 100% dos estados usados por esta entidade pertencem ao conjunto de cinco estados previstos para a etapa inicial.
- **SC-004**: 100% das tentativas de cancelamento em estados recebida e aguardando aprovacao de orcamento resultam em mudanca do status para cancelada.
- **SC-005**: 100% das tentativas de cancelamento em estados em execucao, finalizada e cancelada sao bloqueadas sem alteracao do status.
- **SC-006**: A especificacao da etapa permanece limitada a definicao da entidade base e suas regras iniciais, sem incluir fluxos completos externos para ordem de servico.

## Assumptions

- Assume-se que cliente e veiculo ja sao conceitos existentes ou planejados no dominio e serao referenciados pela ordem de servico por seus identificadores.
- Assume-se que regras adicionais de transicao de status alem do cancelamento serao definidas em etapas futuras.
- Assume-se que integracoes externas, persistencia detalhada, interface de usuario e fluxos operacionais completos estao fora do escopo desta etapa.
- Assume-se que o orcamento sera um dos principais consumidores desta entidade base em incrementos posteriores.

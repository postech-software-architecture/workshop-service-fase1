# Feature Specification: Encerramento de Composicao Tecnica e Aprovacao de Orcamento

**Feature Branch**: `005-json-shortname-budget`  
**Created**: 2026-04-29  
**Status**: Draft  
**Input**: User description: "Implementar os use cases relacionados ao encerramento da composicao tecnica da Ordem de Servico e ao fluxo de aprovacao do Orcamento."

## Clarifications

### Session 2026-04-29

- Q: Quem executa no sistema a aprovacao, rejeicao ou cancelamento do orcamento? → A: O atendente registra no sistema a resposta recebida do cliente.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Encerrar composicao tecnica e emitir proposta (Priority: P1)

Como mecanico, eu quero encerrar a composicao tecnica de uma ordem de servico para enviar ao cliente uma proposta formal baseada nos itens levantados durante o diagnostico.

**Why this priority**: Sem o encerramento da composicao tecnica e a geracao do orcamento, o atendimento nao avanca para a resposta do cliente e o fluxo principal da oficina fica bloqueado.

**Independent Test**: Pode ser testada de forma independente ao encerrar uma ordem de servico com pelo menos um item e verificar a geracao de um novo orcamento, a mudanca de status da ordem e o registro da notificacao ao cliente.

**Acceptance Scenarios**:

1. **Given** uma ordem de servico em composicao com pelo menos um item tecnico, **When** o mecanico encerra a composicao tecnica, **Then** a ordem muda para `AGUARDANDO_RESPOSTA_CLIENTE`, um novo orcamento e criado com status `PENDENTE_APROVACAO`, os itens atuais sao copiados como fotografia da proposta e o cliente e notificado.
2. **Given** uma ordem de servico sem itens tecnicos, **When** o mecanico tenta encerrar a composicao tecnica, **Then** o encerramento e rejeitado e nenhum orcamento e criado.
3. **Given** uma ordem de servico que ja possui um orcamento com status `PENDENTE_APROVACAO`, **When** o mecanico tenta encerrar novamente a composicao tecnica, **Then** a operacao e rejeitada para impedir mais de um orcamento pendente ao mesmo tempo.

---

### User Story 2 - Aprovar ou rejeitar proposta do cliente (Priority: P2)

Como atendente responsavel pela resposta do cliente, eu quero registrar no sistema a aprovacao ou rejeicao do orcamento pendente para que a ordem de servico siga para execucao ou retorne para ajuste tecnico.

**Why this priority**: Esta jornada concretiza a decisao do cliente sobre a proposta enviada e determina o proximo estado operacional da ordem de servico.

**Independent Test**: Pode ser testada de forma independente a partir de um orcamento pendente vinculado a uma ordem aguardando resposta do cliente, validando que o atendente consegue registrar aprovacao e rejeicao com suas respectivas transicoes e notificacoes ao mecanico.

**Acceptance Scenarios**:

1. **Given** um orcamento com status `PENDENTE_APROVACAO` vinculado a uma ordem com status `AGUARDANDO_RESPOSTA_CLIENTE`, **When** o orcamento e aprovado, **Then** o orcamento muda para `APROVADO`, a ordem muda para `AGUARDANDO_EXECUCAO` e o mecanico e notificado.
2. **Given** um orcamento com status `PENDENTE_APROVACAO` vinculado a uma ordem com status `AGUARDANDO_RESPOSTA_CLIENTE`, **When** o orcamento e rejeitado, **Then** o orcamento muda para `REJEITADO`, a ordem volta para `EM_COMPOSICAO` e o mecanico e notificado.
3. **Given** um orcamento que nao esteja `PENDENTE_APROVACAO` ou vinculado a uma ordem fora de `AGUARDANDO_RESPOSTA_CLIENTE`, **When** alguem tenta aprovar ou rejeitar o orcamento, **Then** a operacao e rejeitada sem alterar os estados atuais.

---

### User Story 3 - Cancelar proposta e encerrar atendimento (Priority: P3)

Como atendente, eu quero cancelar um orcamento pendente quando o atendimento nao deve continuar para que a ordem de servico seja encerrada de forma consistente.

**Why this priority**: O cancelamento encerra formalmente o fluxo quando o atendimento nao prossegue, preservando rastreabilidade sem depender de fluxos manuais paralelos.

**Independent Test**: Pode ser testada de forma independente a partir de um orcamento pendente vinculado a uma ordem aguardando resposta do cliente, verificando a mudanca de status do orcamento, o cancelamento da ordem e a notificacao ao mecanico.

**Acceptance Scenarios**:

1. **Given** um orcamento com status `PENDENTE_APROVACAO` vinculado a uma ordem com status `AGUARDANDO_RESPOSTA_CLIENTE`, **When** o orcamento e cancelado, **Then** o orcamento muda para `CANCELADO`, a ordem muda para `CANCELADA` e o mecanico e notificado.
2. **Given** um orcamento que nao esteja `PENDENTE_APROVACAO` ou vinculado a uma ordem fora de `AGUARDANDO_RESPOSTA_CLIENTE`, **When** alguem tenta cancelar o orcamento, **Then** a operacao e rejeitada sem alterar os estados atuais.

### Edge Cases

- Tentativa de encerrar a composicao tecnica de uma ordem sem qualquer item de servico, peca ou insumo deve falhar sem gerar proposta parcial.
- Tentativa de gerar um novo orcamento enquanto ja existe outro `PENDENTE_APROVACAO` para a mesma ordem deve ser bloqueada.
- A fotografia dos itens do orcamento deve permanecer inalterada mesmo que a ordem volte para `EM_COMPOSICAO` e seus itens sejam ajustados depois.
- Um orcamento aprovado, rejeitado ou cancelado nao pode voltar ao fluxo de resposta do cliente.
- Se a notificacao nao puder ser concluida, a decisao de negocio deve permanecer registrada e a falha de comunicacao deve ficar auditavel para tratativa operacional.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: O sistema MUST permitir que uma ordem de servico mantenha itens de composicao tecnica classificados como servico, peca ou insumo.
- **FR-002**: O sistema MUST exigir a existencia de pelo menos um item de composicao tecnica para encerrar a composicao de uma ordem de servico.
- **FR-003**: O sistema MUST impedir o encerramento da composicao tecnica quando a ordem de servico ja possuir um orcamento com status `PENDENTE_APROVACAO`.
- **FR-004**: O sistema MUST criar um novo orcamento vinculado a ordem de servico quando a composicao tecnica for encerrada com sucesso.
- **FR-005**: O sistema MUST definir o novo orcamento como `PENDENTE_APROVACAO` no momento da sua criacao.
- **FR-006**: O sistema MUST mover a ordem de servico para `AGUARDANDO_RESPOSTA_CLIENTE` quando o orcamento for gerado.
- **FR-007**: O sistema MUST copiar todos os itens atuais da ordem de servico para o orcamento como uma fotografia imutavel da proposta enviada ao cliente.
- **FR-008**: O sistema MUST registrar que o cliente foi notificado sempre que um novo orcamento for enviado para resposta.
- **FR-009**: O sistema MUST permitir que somente o atendente registre a aprovacao de orcamentos com status `PENDENTE_APROVACAO` vinculados a ordens em `AGUARDANDO_RESPOSTA_CLIENTE`.
- **FR-010**: O sistema MUST mudar o orcamento para `APROVADO` e a ordem de servico para `AGUARDANDO_EXECUCAO` quando a aprovacao for concluida.
- **FR-011**: O sistema MUST permitir que somente o atendente registre a rejeicao de orcamentos com status `PENDENTE_APROVACAO` vinculados a ordens em `AGUARDANDO_RESPOSTA_CLIENTE`.
- **FR-012**: O sistema MUST mudar o orcamento para `REJEITADO` e a ordem de servico para `EM_COMPOSICAO` quando a rejeicao for concluida.
- **FR-013**: O sistema MUST permitir que somente o atendente registre o cancelamento de orcamentos com status `PENDENTE_APROVACAO` vinculados a ordens em `AGUARDANDO_RESPOSTA_CLIENTE`.
- **FR-014**: O sistema MUST mudar o orcamento para `CANCELADO` e a ordem de servico para `CANCELADA` quando o cancelamento for concluido.
- **FR-015**: O sistema MUST registrar que o mecanico foi notificado sempre que um orcamento for aprovado, rejeitado ou cancelado.
- **FR-016**: O sistema MUST permitir que, apos uma rejeicao, a ordem de servico possa receber ajustes nos itens e gere um novo orcamento em uma rodada posterior.
- **FR-017**: O sistema MUST manter historico suficiente para distinguir cada orcamento gerado para a mesma ordem de servico e o resultado de cada decisao.
- **FR-018**: O sistema MUST manter fora de escopo neste MVP a aprovacao parcial de itens, pagamento, emissao de nota fiscal, geracao de PDF e controle de estoque.

### Key Entities *(include if feature involves data)*

- **Ordem de Servico**: Representa o ciclo de vida do atendimento na oficina, incluindo os estados operacionais, os itens de composicao tecnica e o vinculo com os orcamentos gerados ao longo do atendimento.
- **Item de Composicao Tecnica**: Representa cada elemento levantado pelo mecanico durante o diagnostico, podendo ser classificado como servico, peca ou insumo e servindo de base para a proposta enviada.
- **Orcamento**: Representa a proposta enviada ao cliente, vinculada a uma ordem de servico, com status proprio de decisao e uma fotografia dos itens que compoem a proposta naquele momento.
- **Registro de Notificacao**: Representa a evidencia operacional de que uma mensagem foi enviada ao cliente ou ao mecanico em resposta a um evento do fluxo de orcamento.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% das tentativas validas de encerrar a composicao tecnica resultam em um orcamento criado, uma ordem movida para `AGUARDANDO_RESPOSTA_CLIENTE` e um registro de notificacao ao cliente.
- **SC-002**: 100% das tentativas invalidas de encerrar composicao, aprovar, rejeitar ou cancelar orcamentos fora das pre-condicoes definidas sao recusadas sem criar estados inconsistentes.
- **SC-003**: 100% das respostas validas do cliente sobre orcamentos pendentes atualizam corretamente o par orcamento-ordem para o estado esperado na primeira tentativa.
- **SC-004**: Em testes de aceite do fluxo, um atendente consegue concluir a decisao de aprovacao, rejeicao ou cancelamento de um orcamento em no maximo 1 minuto apos localizar a proposta pendente.

## Assumptions

- A oficina ja possui um fluxo existente para criar e manter ordens de servico em `EM_COMPOSICAO` antes desta feature.
- O atendente que executa aprovacao, rejeicao ou cancelamento ja esta autenticado e autorizado pelos mecanismos existentes do sistema.
- A notificacao exigida neste MVP precisa apenas produzir um registro operacional confiavel de envio, sem entrega real por canais externos.
- A ordem de servico pode acumular multiplos orcamentos historicos ao longo do atendimento, desde que somente um permaneca `PENDENTE_APROVACAO` por vez.
- O cliente responde ao orcamento de forma integral, sem aprovacao ou rejeicao parcial por item nesta fase.

# Feature Specification: Estoque Integrado ao Ciclo da Ordem

**Feature Branch**: `009-estoque-integrado`  
**Created**: 2026-05-07  
**Status**: Draft  
**Input**: User description: "docs/roadmap-fase1/bloco-3-estoque-integrado.md"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Reservar estoque ao aprovar orcamento (Priority: P1)

Como consultor ou operador da oficina, quero que a aprovacao de um orcamento reserve automaticamente as pecas e insumos necessarios para a ordem de servico, para que o estoque disponivel reflita compromissos assumidos com o cliente antes da execucao.

**Why this priority**: Sem reserva na aprovacao, a oficina pode vender o mesmo item para mais de uma ordem e descobrir a falta apenas durante a execucao.

**Independent Test**: Pode ser testado aprovando um orcamento com itens de estoque suficientes e verificando que o saldo reservado aumenta, o saldo disponivel diminui e a ordem permanece auditavel.

**Acceptance Scenarios**:

1. **Given** uma ordem de servico com orcamento pendente e itens de estoque suficientes, **When** o orcamento e aprovado, **Then** o sistema reserva a quantidade necessaria de cada item, registra o evento de reserva e confirma a aprovacao.
2. **Given** uma ordem de servico com orcamento pendente e ao menos um item sem saldo suficiente, **When** o orcamento e aprovado, **Then** o sistema recusa a aprovacao com uma mensagem de negocio clara e nao altera reservas, saldos ou status.
3. **Given** um orcamento ja aprovado com reservas existentes, **When** a aprovacao e solicitada novamente, **Then** o sistema impede reserva duplicada e mantem os saldos existentes.

---

### User Story 2 - Consumir reservas ao iniciar execucao (Priority: P2)

Como tecnico ou operador da oficina, quero que o inicio da execucao consuma as reservas ja feitas para a ordem, para que o estoque deixe de representar compromisso futuro e passe a representar consumo real.

**Why this priority**: A reserva protege a disponibilidade ate o inicio do trabalho, mas o estoque so fica correto se a execucao converter essa reserva em consumo efetivo.

**Independent Test**: Pode ser testado iniciando a execucao de uma ordem aprovada com reservas e verificando que cada reserva e consumida uma unica vez.

**Acceptance Scenarios**:

1. **Given** uma ordem aprovada com reservas de pecas e insumos, **When** a execucao e iniciada, **Then** o sistema consome as quantidades reservadas e registra o consumo vinculado a ordem.
2. **Given** uma ordem cuja execucao ja foi iniciada, **When** o inicio da execucao e solicitado novamente, **Then** o sistema nao duplica consumo nem cria novas reservas.
3. **Given** uma ordem sem reservas validas, **When** a execucao e iniciada, **Then** o sistema informa que nao ha reserva apta para consumo e preserva o estoque.

---

### User Story 3 - Liberar reservas em rejeicao ou cancelamento (Priority: P3)

Como atendente ou gestor da oficina, quero que a rejeicao ou cancelamento de um orcamento libere automaticamente as reservas relacionadas, para que itens nao utilizados voltem ao saldo disponivel sem intervencao manual.

**Why this priority**: Reservas presas reduzem artificialmente o estoque disponivel e prejudicam novas vendas ou execucoes.

**Independent Test**: Pode ser testado rejeitando ou cancelando um orcamento aprovado e verificando que somente as quantidades reservadas para aquela ordem retornam ao saldo disponivel.

**Acceptance Scenarios**:

1. **Given** uma ordem com orcamento aprovado e reservas ativas, **When** o orcamento e rejeitado, **Then** o sistema libera as reservas da ordem e registra a liberacao.
2. **Given** uma ordem com orcamento aprovado e reservas ativas, **When** o orcamento e cancelado, **Then** o sistema libera as reservas da ordem e registra a liberacao.
3. **Given** uma ordem sem reserva ativa ou ja liberada, **When** rejeicao ou cancelamento e solicitado, **Then** o sistema nao libera quantidade maior que a reservada.

---

### User Story 4 - Auditar movimentacoes do ciclo operacional (Priority: P4)

Como gestor da oficina, quero consultar as movimentacoes de estoque relacionadas a uma ordem de servico, para entender quando itens foram reservados, consumidos ou liberados durante o ciclo operacional.

**Why this priority**: A rastreabilidade reduz investigacoes manuais, evita depender de textos informais e prepara a base para indicadores futuros.

**Independent Test**: Pode ser testado executando o ciclo completo de uma ordem e verificando que as movimentacoes de reserva, consumo e liberacao ficam vinculadas ao contexto operacional correto.

**Acceptance Scenarios**:

1. **Given** uma ordem com movimentacoes de estoque no ciclo operacional, **When** o historico da ordem e consultado, **Then** o sistema apresenta as movimentacoes relacionadas a essa ordem.
2. **Given** movimentacoes de ordens diferentes para o mesmo item, **When** o historico de uma ordem especifica e consultado, **Then** apenas as movimentacoes daquela ordem aparecem como relacionadas.

### Edge Cases

- Aprovacoes concorrentes que disputam o mesmo saldo devem permitir somente reservas que caibam no estoque disponivel real.
- Se qualquer item do orcamento nao puder ser reservado, nenhuma reserva parcial deve permanecer para a aprovacao.
- Rejeicao ou cancelamento apos consumo da reserva nao deve devolver automaticamente itens ja consumidos.
- Movimentacoes internas de reserva e liberacao nao devem ser lancadas manualmente por usuarios.
- Falhas durante aprovacao, inicio de execucao, rejeicao ou cancelamento devem deixar ordem, orcamento, saldos e movimentacoes em estado consistente.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The system MUST validate available stock for every stock-controlled item in an order budget before confirming budget approval.
- **FR-002**: The system MUST reject budget approval when any required stock-controlled item lacks sufficient available quantity, with no partial stock changes.
- **FR-003**: The system MUST create one reservation record per required stock-controlled item when a budget is approved successfully.
- **FR-004**: The system MUST update order status, budget status, stock balances and stock movement history as one consistent business operation during approval.
- **FR-005**: The system MUST prevent duplicate reservations for the same approved budget or order.
- **FR-006**: The system MUST consume existing reservations when execution of the related order starts.
- **FR-007**: The system MUST prevent duplicate stock consumption when execution start is requested more than once for the same order.
- **FR-008**: The system MUST release active reservations related to an order when the related budget is rejected or cancelled before consumption.
- **FR-009**: The system MUST prevent reservation release from exceeding the quantity still reserved for the order.
- **FR-010**: The system MUST keep reservation, consumption and release movements traceable to the related service order.
- **FR-011**: The system MUST distinguish internal reservation and release movements from manual stock entries and exits available to users.
- **FR-012**: The system MUST protect stock from concurrent approvals so that competing orders cannot reserve more than the available quantity.
- **FR-013**: The system MUST provide a clear business error when a concurrent approval cannot be completed because stock is no longer available.
- **FR-014**: The system MUST preserve an auditable history for each stock movement created by budget approval, execution start, rejection or cancellation.

### Key Entities *(include if feature involves data)*

- **Service Order**: Represents the workshop service request and its operational cycle; relates budget decisions to stock activity.
- **Budget**: Represents the approved, rejected or cancelled commercial proposal for a service order; contains the items that may require stock reservation.
- **Stock Item**: Represents a part or consumable whose available, reserved and consumed quantities must remain accurate.
- **Stock Movement**: Represents a reservation, consumption, release, manual entry or manual exit of stock, with enough context to audit its business origin.
- **Reservation**: Represents a committed quantity of a stock item for a specific service order before execution consumes it or cancellation releases it.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% of successful budget approvals with stock-controlled items create matching reservations for the exact approved quantities.
- **SC-002**: 100% of budget approvals with insufficient stock are blocked before any order status, budget status or stock quantity is changed.
- **SC-003**: 100% of service orders that start execution after approval consume each active reservation exactly once.
- **SC-004**: 100% of rejected or cancelled budgets before execution return active reserved quantities to available stock without exceeding the reserved amount.
- **SC-005**: In concurrent approval attempts for the same limited stock, the final reserved quantity never exceeds available stock.
- **SC-006**: Users reviewing a service order can identify all stock reservations, consumptions and releases related to that order in under 1 minute.

## Assumptions

- The feature uses the service order as the minimum required context for tracing stock movements.
- Budget approval remains the business moment when stock becomes committed to a customer order.
- Execution start is the business moment when reserved items become consumed stock.
- Rejection and cancellation release only reservations that have not already been consumed.
- Manual stock operations remain limited to regular stock entries and exits; reservation and release are internal business movements.
- When concurrent approvals conflict, the user receives a business-level message asking them to review current stock availability and retry if appropriate.
- This feature depends on the service order execution milestone being available before end-to-end execution consumption can be completed.

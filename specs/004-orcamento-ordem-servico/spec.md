# Feature Specification: Entidade de Dominio Orcamento

**Feature Branch**: `004-orcamento-ordem-servico`  
**Created**: 2026-04-28  
**Status**: Draft  
**Input**: User description: "Criar a entidade de dominio Orcamento, responsavel por representar os orcamentos vinculados a uma Ordem de Servico."

## Clarifications

### Session 2026-04-28

- Q: Quantos itens minimos um orcamento deve possuir? -> A: Pelo menos 1 item

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Registrar o orcamento de uma ordem de servico (Priority: P1)

Como responsavel pelas regras operacionais da oficina, quero que exista um orcamento vinculado a uma ordem de servico, para que os valores e itens cobrados sejam representados por um conceito unico e reutilizavel.

**Why this priority**: Sem o orcamento como entidade propria, a ordem de servico nao consegue suportar aprovacao comercial, rejeicao ou adicao posterior de servicos com rastreabilidade.

**Independent Test**: Pode ser testada verificando que um orcamento sempre possui identificador proprio, ordem de servico vinculada, valor, pelo menos um item, tipo e status inicial valido.

**Acceptance Scenarios**:

1. **Given** a necessidade de precificar uma ordem de servico, **When** um novo orcamento e definido, **Then** ele fica vinculado a uma ordem de servico e contem identificador, valor, pelo menos um item, tipo e status.
2. **Given** um orcamento inicial da ordem, **When** ele e classificado, **Then** seu tipo identifica se representa o servico original ou uma adicao posterior de servico.

---

### User Story 2 - Submeter e decidir um orcamento pelo cliente (Priority: P2)

Como responsavel pelo relacionamento com o cliente, quero enviar um orcamento para aprovacao e registrar sua aprovacao ou rejeicao, para que a oficina saiba se pode iniciar ou revisar o servico planejado.

**Why this priority**: O fluxo de aprovacao e o nucleo do uso do orcamento e determina se a ordem pode avancar para execucao ou se precisara de um novo orcamento.

**Independent Test**: Pode ser testada verificando que apenas orcamentos criados podem ir para aprovacao, que apenas orcamentos pendentes podem ser aprovados ou rejeitados e que cada decisao muda o status para o valor esperado.

**Acceptance Scenarios**:

1. **Given** um orcamento com status criado, **When** ele e enviado para aprovacao, **Then** o status muda para pendente de aprovacao.
2. **Given** um orcamento com status pendente de aprovacao, **When** o cliente aprova o orcamento, **Then** o status muda para aprovado.
3. **Given** um orcamento com status pendente de aprovacao, **When** o cliente rejeita o orcamento, **Then** o status muda para rejeitado e a entidade passa a exigir a criacao de um novo orcamento para continuidade.

---

### User Story 3 - Coordenar cancelamento do orcamento com a ordem de servico (Priority: P3)

Como responsavel pelas regras de execucao da oficina, quero que o cancelamento do orcamento respeite seu tipo e o estado atual da ordem de servico, para evitar cancelamentos indevidos depois do inicio da execucao ou em orcamentos complementares.

**Why this priority**: O cancelamento do orcamento afeta diretamente a ordem de servico e precisa preservar consistencia entre o fluxo comercial e o fluxo operacional.

**Independent Test**: Pode ser testada verificando que somente os status permitidos cancelam o orcamento, que apenas o orcamento do tipo servico original tenta cancelar a ordem vinculada e que a ordem nao e cancelada quando ja nao pode mais ser cancelada.

**Acceptance Scenarios**:

1. **Given** um orcamento inicial com status criado, pendente de aprovacao ou aprovado e uma ordem de servico ainda cancelavel, **When** o orcamento e cancelado, **Then** o orcamento muda para cancelado e a ordem de servico vinculada tambem e cancelada.
2. **Given** um orcamento do tipo adicao de servico com status cancelavel, **When** o orcamento e cancelado, **Then** apenas o orcamento muda para cancelado e a ordem de servico permanece inalterada.
3. **Given** um orcamento inicial aprovado cuja ordem de servico avancou para execucao, **When** o cancelamento do orcamento e solicitado, **Then** a ordem de servico nao e cancelada a partir do orcamento.

---

### Edge Cases

- O que acontece quando se tenta enviar para aprovacao um orcamento que nao esta com status criado?
- Como o dominio reage quando uma aprovacao ou rejeicao e solicitada para um orcamento que nao esta pendente de aprovacao?
- O que acontece quando um orcamento inicial e cancelado, mas a ordem de servico vinculada ja nao pode mais ser cancelada?
- Como o dominio deve impedir que um orcamento do tipo adicao de servico cancele a ordem de servico?
- O que acontece quando um orcamento rejeitado precisa ser substituido por outro, sem reaproveitar a mesma decisao de aprovacao?

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: O sistema DEVE definir uma entidade de orcamento vinculada a uma ordem de servico.
- **FR-002**: O sistema DEVE garantir que cada orcamento possua, no minimo, identificador proprio, identificador da ordem de servico, valor, status, itens e tipo.
- **FR-003**: O sistema DEVE garantir que todo orcamento possua pelo menos um item.
- **FR-004**: O sistema DEVE reconhecer, nesta etapa, apenas os seguintes status para o orcamento: criado, pendente de aprovacao, aprovado, rejeitado e cancelado.
- **FR-005**: O sistema DEVE reconhecer, nesta etapa, apenas os seguintes tipos para o orcamento: servico original e adicao de servico.
- **FR-006**: O sistema DEVE permitir enviar para aprovacao apenas orcamentos com status criado.
- **FR-007**: Quando o envio para aprovacao for permitido, o sistema DEVE alterar o status do orcamento para pendente de aprovacao.
- **FR-008**: O sistema DEVE permitir aprovar apenas orcamentos com status pendente de aprovacao.
- **FR-009**: Quando a aprovacao for permitida, o sistema DEVE alterar o status do orcamento para aprovado.
- **FR-010**: O sistema DEVE permitir rejeitar apenas orcamentos com status pendente de aprovacao.
- **FR-011**: Quando a rejeicao for permitida, o sistema DEVE alterar o status do orcamento para rejeitado.
- **FR-012**: Esta etapa NAO DEVE permitir rejeicao parcial de itens do orcamento.
- **FR-013**: Quando um orcamento for rejeitado, o sistema DEVE considerar que um novo orcamento precisara ser criado para continuidade do atendimento.
- **FR-014**: O sistema DEVE permitir cancelar apenas orcamentos com status criado, pendente de aprovacao ou aprovado.
- **FR-015**: Quando o cancelamento do orcamento for permitido, o sistema DEVE alterar o status do orcamento para cancelado.
- **FR-016**: Apenas orcamentos do tipo servico original DEVEM tentar cancelar a ordem de servico vinculada.
- **FR-017**: Orcamentos do tipo adicao de servico NAO DEVEM cancelar a ordem de servico vinculada.
- **FR-018**: Ao cancelar um orcamento do tipo servico original, o sistema DEVE cancelar a ordem de servico vinculada somente se a propria ordem permitir cancelamento.
- **FR-019**: O sistema NAO DEVE cancelar a ordem de servico a partir do orcamento quando a ordem vinculada estiver em execucao ou em qualquer outro estado nao cancelavel.
- **FR-020**: Ao aprovar um orcamento do tipo servico original, o sistema DEVE avancar a ordem de servico vinculada para em execucao.
- **FR-021**: Depois que a ordem de servico vinculada avancar para em execucao em decorrencia da aprovacao do orcamento inicial, o cancelamento desse orcamento inicial NAO DEVE mais cancelar a ordem de servico.

### Key Entities *(include if feature involves data)*

- **Orcamento**: Representa a proposta comercial vinculada a uma ordem de servico. Possui identificador proprio, referencia a ordem, valor, itens, tipo e status de negocio.
- **Status do Orcamento**: Representa o estagio de decisao comercial do orcamento, limitado nesta etapa aos estados criado, pendente de aprovacao, aprovado, rejeitado e cancelado.
- **Tipo do Orcamento**: Diferencia se o orcamento representa o servico original da ordem ou uma adicao posterior de servico.
- **Item de Orcamento**: Representa cada componente cobrado dentro do orcamento. Nesta etapa, os itens existem como parte da entidade, sem regras de rejeicao parcial, e todo orcamento deve possuir pelo menos um item.
- **Ordem de Servico**: Representa a ordem operacional vinculada ao orcamento, afetada pela aprovacao ou cancelamento do orcamento inicial conforme suas proprias regras de cancelamento.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% dos orcamentos definidos nesta etapa incluem identificador, ordem de servico vinculada, valor, pelo menos um item, tipo e status.
- **SC-002**: 100% dos envios para aprovacao realizados a partir de orcamentos com status criado resultam em mudanca para pendente de aprovacao.
- **SC-003**: 100% das aprovacoes e rejeicoes aceitas acontecem apenas a partir de orcamentos pendentes de aprovacao e atualizam o status para aprovado ou rejeitado, respectivamente.
- **SC-004**: 100% das tentativas de cancelamento aceitas ocorrem apenas nos status criado, pendente de aprovacao ou aprovado.
- **SC-005**: 100% dos cancelamentos de orcamentos do tipo adicao de servico preservam a ordem de servico vinculada sem alteracao.
- **SC-006**: 100% das aprovacoes de orcamento inicial fazem a ordem de servico vinculada avancar para em execucao.
- **SC-007**: 100% das tentativas de cancelar a ordem por meio de um orcamento inicial sao bloqueadas quando a ordem de servico vinculada nao pode mais ser cancelada.

## Assumptions

- Assume-se que a entidade `OrdemServico` ja existe no dominio e expoe, no minimo, a verificacao de cancelamento e a capacidade de mudar para cancelada ou em execucao por comportamento de negocio apropriado.
- Assume-se que o valor e os itens do orcamento serao detalhados tecnicamente em etapas posteriores, mantendo nesta fase o foco na presenca desses dados e nas transicoes de estado.
- Assume-se que a criacao do novo orcamento apos rejeicao sera suportada em fluxo futuro, sem necessidade de detalhar orquestracao externa nesta especificacao.
- Assume-se que persistencia, endpoints, casos de uso completos e fluxos de interface relacionados ao orcamento serao tratados em incrementos posteriores, salvo se um proximo passo explicitar o contrario.

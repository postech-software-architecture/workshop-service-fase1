# Feature Specification: Ciclo de Execucao da Ordem de Servico

**Feature Branch**: `008-os-execution-cycle`  
**Created**: 2026-05-07  
**Status**: Draft  
**Input**: User description: "docs\\roadmap-fase1\\bloco-2-ciclo-execucao-os.md"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Iniciar execucao da ordem aprovada (Priority: P1)

Como mecanico ou administrador, quero iniciar a execucao de uma ordem de servico com orcamento aprovado para que a oficina registre quando o trabalho saiu da fila e entrou em execucao real.

**Why this priority**: Sem esta etapa, a ordem permanece indefinidamente aguardando execucao e o ciclo operacional nao representa o trabalho feito na oficina.

**Independent Test**: Pode ser testada com uma ordem de servico em estado de aguardando execucao; ao iniciar a execucao, a ordem deve mudar para em execucao, registrar a data do inicio e criar um registro auditavel da transicao.

**Acceptance Scenarios**:

1. **Given** uma ordem de servico com orcamento aprovado e aguardando execucao, **When** um mecanico inicia a execucao, **Then** a ordem passa para em execucao e registra quando e por quem a transicao foi feita.
2. **Given** uma ordem de servico que ainda aguarda resposta do cliente, **When** um usuario tenta iniciar a execucao, **Then** a operacao e recusada e a ordem permanece no estado anterior.
3. **Given** uma ordem de servico cancelada, finalizada ou entregue, **When** um usuario tenta iniciar a execucao, **Then** a operacao e recusada e nenhuma nova transicao e registrada.

---

### User Story 2 - Finalizar execucao tecnica (Priority: P1)

Como mecanico ou administrador, quero finalizar uma ordem que esta em execucao para indicar que o trabalho tecnico da oficina foi concluido e o veiculo esta pronto para retirada.

**Why this priority**: A finalizacao separa o encerramento do trabalho tecnico da entrega ao cliente, permitindo acompanhamento operacional mais fiel e base para metricas de tempo de execucao.

**Independent Test**: Pode ser testada com uma ordem em execucao; ao finalizar, a ordem deve passar para finalizada, registrar a data de finalizacao e manter historico da transicao.

**Acceptance Scenarios**:

1. **Given** uma ordem de servico em execucao, **When** um mecanico finaliza a execucao, **Then** a ordem passa para finalizada e registra quando e por quem a transicao foi feita.
2. **Given** uma ordem que ainda nao teve execucao iniciada, **When** um usuario tenta finalizar, **Then** a operacao e recusada.
3. **Given** uma ordem ja entregue, **When** um usuario tenta finalizar novamente, **Then** a operacao e recusada e o historico nao e duplicado.

---

### User Story 3 - Registrar entrega do veiculo (Priority: P2)

Como atendente ou administrador, quero registrar a entrega de uma ordem finalizada para indicar que o cliente retirou o veiculo e o atendimento foi encerrado.

**Why this priority**: A entrega fecha o ciclo completo da ordem de servico e diferencia veiculos prontos dos veiculos efetivamente retirados.

**Independent Test**: Pode ser testada com uma ordem finalizada; ao registrar a entrega, a ordem deve passar para entregue, registrar a data de entrega e manter a trilha de auditoria.

**Acceptance Scenarios**:

1. **Given** uma ordem de servico finalizada, **When** um atendente registra a entrega, **Then** a ordem passa para entregue e registra quando e por quem a transicao foi feita.
2. **Given** uma ordem em execucao, **When** um usuario tenta registrar entrega, **Then** a operacao e recusada porque o servico ainda nao foi finalizado.
3. **Given** uma ordem entregue, **When** um usuario consulta seu status, **Then** o sistema indica que o ciclo operacional foi concluido.

---

### User Story 4 - Auditar historico de status da ordem (Priority: P2)

Como gestor da oficina, quero que cada mudanca relevante de status da ordem fique registrada para que seja possivel auditar o ciclo, entender gargalos e alimentar metricas administrativas.

**Why this priority**: A rastreabilidade das transicoes e necessaria para confianca operacional e para calcular metricas posteriores de tempo medio.

**Independent Test**: Pode ser testada executando o ciclo completo de uma ordem e verificando se todas as transicoes relevantes aparecem em ordem cronologica, com status anterior, status novo, data e responsavel.

**Acceptance Scenarios**:

1. **Given** uma ordem que passa por aprovacao, inicio de execucao, finalizacao e entrega, **When** o historico da ordem e avaliado, **Then** todas as transicoes aparecem em ordem cronologica.
2. **Given** uma tentativa invalida de transicao, **When** a operacao e recusada, **Then** nenhum registro de historico e criado para essa tentativa.
3. **Given** uma ordem que ja possuia transicoes anteriores ao novo ciclo, **When** novas transicoes acontecem, **Then** o historico preserva a sequencia completa sem sobrescrever registros anteriores.

### Edge Cases

- Ordem inexistente: a operacao deve informar que a ordem nao foi encontrada.
- Ordem em estado incompatavel: a operacao deve ser recusada sem alterar status, datas ou historico.
- Usuario sem permissao para a etapa solicitada: a operacao deve ser recusada sem alterar a ordem.
- Tentativa repetida da mesma transicao: a operacao deve ser recusada e nao deve duplicar historico.
- Falha ao identificar o responsavel pela transicao: a mudanca de status nao deve ocorrer sem registro de responsavel auditavel.
- Consulta de ordem entregue: deve preservar todas as datas relevantes do ciclo completo.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: O sistema MUST permitir que usuarios autorizados iniciem a execucao de uma ordem de servico que esteja aguardando execucao.
- **FR-002**: O sistema MUST alterar a ordem para o estado em execucao quando a execucao for iniciada com sucesso.
- **FR-003**: O sistema MUST registrar a data e hora de inicio da execucao quando a ordem entrar em execucao.
- **FR-004**: O sistema MUST impedir o inicio da execucao para ordens que nao estejam aguardando execucao.
- **FR-005**: O sistema MUST permitir que usuarios autorizados finalizem uma ordem de servico que esteja em execucao.
- **FR-006**: O sistema MUST alterar a ordem para o estado finalizada quando a execucao tecnica for concluida.
- **FR-007**: O sistema MUST registrar a data e hora de finalizacao quando a ordem for finalizada.
- **FR-008**: O sistema MUST impedir a finalizacao de ordens que nao estejam em execucao.
- **FR-009**: O sistema MUST permitir que usuarios autorizados registrem a entrega de uma ordem finalizada.
- **FR-010**: O sistema MUST alterar a ordem para o estado entregue quando a entrega do veiculo for registrada com sucesso.
- **FR-011**: O sistema MUST registrar a data e hora de entrega quando o veiculo for entregue.
- **FR-012**: O sistema MUST impedir a entrega de ordens que nao estejam finalizadas.
- **FR-013**: O sistema MUST registrar historico auditavel para toda transicao de status bem-sucedida da ordem de servico.
- **FR-014**: Cada registro de historico MUST identificar a ordem, o status anterior, o status novo, a data da transicao e o usuario responsavel.
- **FR-015**: O sistema MUST preservar registros de historico ja existentes quando novas transicoes forem realizadas.
- **FR-016**: O sistema MUST registrar historico tambem para transicoes ja existentes no ciclo da ordem, incluindo encerramento da composicao, aprovacao, rejeicao e cancelamento.
- **FR-017**: O sistema MUST recusar transicoes invalidas sem alterar o status atual, sem atualizar datas de ciclo e sem criar historico indevido.
- **FR-018**: O sistema MUST expor os estados em execucao e entregue nas consultas e respostas que apresentem o status da ordem.
- **FR-019**: O sistema MUST restringir o inicio e a finalizacao da execucao a perfis operacionais autorizados da oficina.
- **FR-020**: O sistema MUST restringir a entrega do veiculo a perfis autorizados para atendimento ou administracao.

### Key Entities

- **Ordem de Servico**: Representa o atendimento do veiculo na oficina, com cliente, veiculo, status atual e datas relevantes do ciclo operacional.
- **Status da Ordem de Servico**: Representa a etapa atual do ciclo da ordem, incluindo composicao, resposta do cliente, aguardando execucao, em execucao, finalizada, entregue e cancelada.
- **Historico de Status da Ordem**: Registro auditavel de cada transicao bem-sucedida, com status anterior, status novo, data da transicao e usuario responsavel.
- **Usuario Responsavel**: Pessoa autenticada que executa uma transicao de status dentro de seu perfil autorizado.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% das ordens com orcamento aprovado podem avancar de aguardando execucao ate entregue seguindo apenas transicoes validas.
- **SC-002**: 100% das tentativas de transicao fora da ordem permitida sao recusadas sem alterar a ordem.
- **SC-003**: 100% das transicoes bem-sucedidas registram status anterior, status novo, data da transicao e responsavel.
- **SC-004**: Gestores conseguem reconstruir a linha do tempo de uma ordem entregue em ate 1 minuto usando os dados apresentados pelo sistema.
- **SC-005**: O ciclo separa veiculos em execucao, veiculos prontos e veiculos entregues em consultas operacionais sem ambiguidade de status.
- **SC-006**: Pelo menos um fluxo completo de ordem de servico, da aprovacao ate a entrega, pode ser demonstrado sem intervencao manual nos dados.

## Assumptions

- `FINALIZADA` significa que o trabalho tecnico da oficina foi concluido e o veiculo esta pronto para retirada.
- `ENTREGUE` significa que o cliente retirou o veiculo e o atendimento foi encerrado operacionalmente.
- `EM_EXECUCAO` representa uma ordem que saiu da fila de aguardando execucao e esta em trabalho tecnico.
- O usuario responsavel pela transicao deve ser identificado pela sessao autenticada.
- As regras de estoque associadas ao inicio da execucao fazem parte de uma feature posterior e nao bloqueiam esta especificacao.
- O historico de status desta feature sera a fonte confiavel para metricas futuras de tempo medio.

# Data Model: Estoque Integrado ao Ciclo da Ordem

## Service Order (`OrdemServico`)

**Purpose**: Contexto operacional que conecta orcamento, composicao tecnica e movimentacoes de estoque.

**Relevant fields**:

- `id`: identificador da OS.
- `numero`: numero de negocio usado em comunicacao e motivos.
- `status`: controla se a OS pode aprovar orcamento, iniciar execucao, finalizar ou entregar.
- `itensComposicao`: itens tecnicos que podem referenciar pecas/insumos.

**Relationships**:

- Possui um ou mais orcamentos.
- Origina reservas, saidas e liberacoes de estoque por meio de movimentacoes vinculadas.

**Validation rules**:

- Aprovacao de orcamento exige OS em `AGUARDANDO_RESPOSTA_CLIENTE`.
- Inicio de execucao exige OS aprovada/aguardando execucao.
- Rejeicao ou cancelamento antes da execucao libera apenas reservas ativas.

## Budget (`Orcamento`)

**Purpose**: Decisao comercial que aprova, rejeita ou cancela itens ligados a OS.

**Relevant fields**:

- `id`: identificador do orcamento.
- `idOrdemServico`: OS vinculada.
- `status`: pendente, aprovado, rejeitado ou cancelado.
- `itens`: itens financeiros do orcamento.

**Relationships**:

- Pertence a uma OS.
- Pode originar reservas e liberacoes por meio das movimentacoes de estoque.

**Validation rules**:

- Aprovacao nao pode criar reservas duplicadas para o mesmo orcamento/OS.
- Rejeicao e cancelamento nao podem liberar mais do que ainda esta reservado.

## Stock (`Estoque`)

**Purpose**: Saldo fisico/disponivel de uma peca ou insumo em uma localizacao.

**Relevant fields**:

- `id`: identificador do estoque.
- `pecaInsumoId`: item controlado.
- `localizacao`: local fisico.
- `quantidade`: saldo disponivel atual.
- `ativo`: controle de remocao logica.
- `versao`: controle de concorrencia.

**Relationships**:

- Recebe movimentacoes de entrada, saida, ajuste, reserva e liberacao.

**Validation rules**:

- Reserva e saida exigem quantidade positiva e saldo suficiente.
- Liberacao exige quantidade positiva.
- Aprovacoes concorrentes devem respeitar a versao atual do estoque.

## Stock Movement (`MovimentacaoEstoque`)

**Purpose**: Historico auditavel de alteracoes de estoque.

**Relevant fields**:

- `id`: identificador da movimentacao.
- `estoqueId`: estoque movimentado.
- `ordemServicoId`: OS relacionada quando a movimentacao nasce do ciclo operacional.
- `orcamentoId`: orcamento relacionado quando a movimentacao nasce de aprovacao, rejeicao ou cancelamento.
- `tipo`: entrada, saida, ajuste, reserva ou liberacao.
- `quantidade`: quantidade movimentada.
- `quantidadeAnterior`: saldo antes da movimentacao.
- `quantidadePosterior`: saldo apos a movimentacao.
- `motivo`: descricao complementar.
- `dataMovimentacao`: momento da movimentacao.
- `dataCriacao`: auditoria de criacao.

**Relationships**:

- Pertence a um estoque.
- Pode pertencer a uma OS e a um orcamento.

**Validation rules**:

- `ordemServicoId` e obrigatorio para movimentacoes internas de `RESERVA`, `SAIDA` por inicio de execucao e `LIBERACAO` do ciclo da OS.
- `orcamentoId` e obrigatorio para `RESERVA` e `LIBERACAO` originadas por orcamento.
- Movimentacoes manuais de entrada/saida podem nao ter OS/orcamento.
- Movimentacoes de ajuste nao fazem parte do endpoint manual desta feature.

## Reservation State

**Purpose**: Estado derivado das movimentacoes de uma OS e item de estoque.

**Derived fields**:

- `quantidadeReservada`: soma de `RESERVA` por OS/estoque/orcamento.
- `quantidadeConsumida`: soma de `SAIDA` por OS/estoque relacionada ao inicio de execucao.
- `quantidadeLiberada`: soma de `LIBERACAO` por OS/estoque/orcamento.
- `quantidadeAtiva`: `quantidadeReservada - quantidadeConsumida - quantidadeLiberada`.

**State transitions**:

```text
sem_reserva -> reservada -> consumida
sem_reserva -> reservada -> liberada
```

**Validation rules**:

- `quantidadeAtiva` nunca pode ficar negativa.
- Inicio de execucao so consome `quantidadeAtiva` positiva.
- Rejeicao/cancelamento so libera `quantidadeAtiva` positiva.

## Database Changes

Planned migration:

```text
src/main/resources/db/migration/V0.YYYYMMDDHHMMSS__vincular_movimentacao_estoque_ordem_orcamento.sql
```

Required changes:

- Add nullable `ordem_servico_id UUID` to `movimentacoes_estoque`.
- Add nullable `orcamento_id UUID` to `movimentacoes_estoque`.
- Add foreign key `fk_movimentacoes_estoque_ordem_servico`.
- Add foreign key `fk_movimentacoes_estoque_orcamento`.
- Add index for `ordem_servico_id`.
- Add index for `orcamento_id`.
- Add comments for every new column, foreign key purpose and index purpose.

## Query Model

### Stock movements by service order

**Purpose**: Permitir auditoria operacional por OS.

**Filter**:

- `ordemServicoId`: obrigatorio.

**Result**:

- Lista somente movimentacoes vinculadas a OS informada.
- Inclui reservas, saidas, liberacoes e qualquer movimentacao historica vinculada a OS.
- Nao mistura movimentacoes de outras ordens, mesmo quando o estoque ou peca/insumo e o mesmo.

# Data Model: Entidade de Dominio Orcamento

## Entidade: Orcamento

### Descricao

Entidade de dominio que representa a proposta comercial vinculada a uma `OrdemServico`, controlando aprovacao, rejeicao e cancelamento de acordo com seu tipo e com o estado atual da ordem.

### Campos

| Campo | Tipo | Obrigatorio | Origem | Regra |
|-------|------|-------------|--------|-------|
| id | UUID | Sim | `EntidadeBase` | Identificador unico do orcamento |
| idOrdemServico | UUID | Sim | Dominio | Referencia a ordem de servico vinculada |
| valor | Valor monetario do dominio | Sim | Dominio | Deve representar o total do orcamento |
| status | `StatusOrcamento` | Sim | Dominio | Toda nova instancia inicia em `CRIADO` |
| itens | Colecao de `ItemOrcamento` | Sim | Dominio | Deve representar os componentes cobrados do orcamento |
| tipo | `TipoOrcamento` | Sim | Dominio | Diferencia servico original de adicao de servico |
| dataCriacao | LocalDateTime | Sim | `EntidadeBase` | Preenchido na criacao ou reconstituicao |
| dataUltimaAtualizacao | LocalDateTime | Sim | `EntidadeBase` | Atualizado em cada transicao de estado aceita |
| dataRemocao | LocalDateTime | Nao | `EntidadeBase` | Sem uso funcional previsto nesta etapa |

### Invariantes

- `idOrdemServico` nao pode ser nulo.
- `valor` nao pode ser nulo.
- `tipo` nao pode ser nulo.
- `status` nao pode ser nulo.
- `itens` nao pode ser nulo e deve representar a composicao do orcamento.
- Todo novo `Orcamento` inicia com status `CRIADO`.

### Comportamentos

| Metodo | Entrada | Saida | Regra |
|--------|---------|-------|-------|
| `enviarParaAprovacao()` | Nenhuma | Nenhuma | Somente `CRIADO` pode transicionar para `PENDENTE_APROVACAO` |
| `aprovar(OrdemServico ordemServico)` | `OrdemServico` | Nenhuma | Somente `PENDENTE_APROVACAO` pode transicionar para `APROVADO`; se o tipo for `SERVICO_ORIGINAL`, a ordem vinculada deve avancar para `EM_EXECUCAO` |
| `rejeitar()` | Nenhuma | Nenhuma | Somente `PENDENTE_APROVACAO` pode transicionar para `REJEITADO` |
| `cancelar(OrdemServico ordemServico)` | `OrdemServico` | Nenhuma | Somente `CRIADO`, `PENDENTE_APROVACAO` ou `APROVADO` podem transicionar para `CANCELADO`; apenas `SERVICO_ORIGINAL` tenta cancelar a ordem |

### Transicoes de Estado do Orcamento

| Estado Atual | Acao | Estado Resultante | Observacao |
|--------------|------|-------------------|------------|
| `CRIADO` | `enviarParaAprovacao()` | `PENDENTE_APROVACAO` | Envio permitido |
| `PENDENTE_APROVACAO` | `aprovar(...)` | `APROVADO` | Aprovacao permitida |
| `PENDENTE_APROVACAO` | `rejeitar()` | `REJEITADO` | Rejeicao permitida |
| `CRIADO` | `cancelar(...)` | `CANCELADO` | Cancelamento permitido |
| `PENDENTE_APROVACAO` | `cancelar(...)` | `CANCELADO` | Cancelamento permitido |
| `APROVADO` | `cancelar(...)` | `CANCELADO` | Cancelamento do orcamento permitido; impacto na OS depende do tipo e do estado da ordem |
| `REJEITADO` | Qualquer transicao acima | Sem alteracao | Deve lancar erro de dominio |
| `CANCELADO` | Qualquer transicao acima | Sem alteracao | Deve lancar erro de dominio |

## Enumeracao: StatusOrcamento

| Valor | Significado |
|-------|-------------|
| `CRIADO` | Orcamento definido, ainda nao enviado ao cliente |
| `PENDENTE_APROVACAO` | Orcamento aguardando decisao do cliente |
| `APROVADO` | Orcamento aceito pelo cliente |
| `REJEITADO` | Orcamento rejeitado pelo cliente |
| `CANCELADO` | Orcamento cancelado pela oficina ou pelo fluxo de negocio |

## Enumeracao: TipoOrcamento

| Valor | Significado |
|-------|-------------|
| `SERVICO_ORIGINAL` | Orcamento inicial da ordem de servico |
| `ADICAO_SERVICO` | Orcamento complementar para novos servicos apos o inicial |

## Entidade/Objeto: ItemOrcamento

### Descricao

Representa um componente cobrado dentro do orcamento. Nesta etapa existe para compor a proposta comercial, sem rejeicao parcial individual.

### Campos Minimos Esperados

| Campo | Tipo | Obrigatorio | Regra |
|-------|------|-------------|-------|
| descricao | Texto de dominio | Sim | Identifica o item cobrado |
| valor | Valor monetario do dominio | Sim | Representa o valor individual do item |

## Relacionamentos

- `Orcamento` referencia uma `OrdemServico` por `idOrdemServico`.
- `Orcamento` coordena comportamentos com uma instancia de `OrdemServico` quando aprovado ou cancelado.
- `Orcamento` contem uma colecao de `ItemOrcamento`.

## Impacto em OrdemServico

| Acao do Orcamento | Tipo | Efeito esperado na Ordem |
|-------------------|------|--------------------------|
| `aprovar(...)` | `SERVICO_ORIGINAL` | Ordem vinculada avanca para `EM_EXECUCAO` |
| `aprovar(...)` | `ADICAO_SERVICO` | Ordem permanece sem alteracao obrigatoria nesta etapa |
| `cancelar(...)` | `SERVICO_ORIGINAL` | Ordem vinculada e cancelada somente se ainda puder ser cancelada |
| `cancelar(...)` | `ADICAO_SERVICO` | Ordem permanece inalterada |

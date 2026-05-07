# Data Model: Entidade Base de Ordem de Servico

## Entidade: OrdemServico

### Descricao

Entidade de dominio que representa uma ordem de servico vinculada a um cliente e a um veiculo, servindo como base para fluxos posteriores como orcamento e execucao.

### Campos

| Campo | Tipo | Obrigatorio | Origem | Regra |
|-------|------|-------------|--------|-------|
| id | UUID | Sim | `EntidadeBase` | Identificador unico da ordem de servico |
| idCliente | UUID | Sim | Dominio | Deve referenciar um cliente valido no contexto da aplicacao |
| idVeiculo | UUID | Sim | Dominio | Deve referenciar um veiculo valido no contexto da aplicacao |
| status | StatusOrdemServico | Sim | Dominio | Toda nova ordem nasce como `RECEBIDA` |
| dataCriacao | LocalDateTime | Sim | `EntidadeBase` | Preenchido na criacao ou reconstituicao |
| dataUltimaAtualizacao | LocalDateTime | Sim | `EntidadeBase` | Atualizado em mudancas de estado |
| dataRemocao | LocalDateTime | Nao | `EntidadeBase` | Sem uso funcional previsto nesta etapa |

### Invariantes

- `id` nao pode ser nulo.
- `idCliente` nao pode ser nulo.
- `idVeiculo` nao pode ser nulo.
- `status` nao pode ser nulo.
- Toda nova `OrdemServico` deve iniciar com `RECEBIDA`.

### Comportamentos

| Metodo | Entrada | Saida | Regra |
|--------|---------|-------|-------|
| `podeSerCancelada()` | Nenhuma | `boolean` | Retorna `true` apenas para `RECEBIDA` e `AGUARDANDO_APROVACAO_ORCAMENTO` |
| `cancelar()` | Nenhuma | Nenhuma | Altera status para `CANCELADA` se permitido; caso contrario lanca `RegraDeNegocioException` |

### Transicoes de Estado Consideradas Nesta Fase

| Estado Atual | Acao | Estado Resultante | Observacao |
|--------------|------|-------------------|------------|
| `RECEBIDA` | `cancelar()` | `CANCELADA` | Cancelamento permitido |
| `AGUARDANDO_APROVACAO_ORCAMENTO` | `cancelar()` | `CANCELADA` | Cancelamento permitido |
| `EM_EXECUCAO` | `cancelar()` | Sem alteracao | Deve lancar erro de dominio |
| `FINALIZADA` | `cancelar()` | Sem alteracao | Deve lancar erro de dominio |
| `CANCELADA` | `cancelar()` | Sem alteracao | Deve lancar erro de dominio |

## Enumeracao: StatusOrdemServico

| Valor | Significado |
|-------|-------------|
| `RECEBIDA` | Ordem recem criada e ainda nao submetida a aprovacao de orcamento |
| `AGUARDANDO_APROVACAO_ORCAMENTO` | Ordem aguardando decisao sobre o orcamento |
| `EM_EXECUCAO` | Ordem com execucao iniciada |
| `CANCELADA` | Ordem cancelada por comportamento de dominio valido |
| `FINALIZADA` | Ordem encerrada com execucao concluida |

## Relacionamentos

- `OrdemServico` referencia um `Cliente` por `idCliente`.
- `OrdemServico` referencia um `Veiculo` por `idVeiculo`.
- Nesta fase, os relacionamentos sao apenas referenciais no dominio, sem navegacao de objeto, sem repositorio proprio e sem mapeamento de persistencia.

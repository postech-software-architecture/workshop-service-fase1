# Data Model: Encerramento de Composicao Tecnica e Fluxo de Orcamento

## Entidade: OrdemServico

### Descricao

Agregado raiz que representa o atendimento da oficina, com controle do ciclo de vida operacional e da composicao tecnica levantada pelo mecanico.

### Campos

| Campo | Tipo | Obrigatorio | Origem | Regra |
|-------|------|-------------|--------|-------|
| id | UUID | Sim | `EntidadeBase` | Identificador unico da ordem |
| idCliente | UUID | Sim | Dominio | Cliente vinculado a ordem |
| idVeiculo | UUID | Sim | Dominio | Veiculo vinculado a ordem |
| status | `StatusOrdemServico` | Sim | Dominio | Nova ordem entra em `EM_COMPOSICAO` nesta fase |
| itensComposicao | Colecao de `ItemComposicaoTecnica` | Sim | Dominio | Pode iniciar vazia, mas precisa ter ao menos um item para encerrar composicao |
| dataCriacao | LocalDateTime | Sim | `EntidadeBase` | Auditoria |
| dataUltimaAtualizacao | LocalDateTime | Sim | `EntidadeBase` | Atualizada a cada mudanca valida |
| dataRemocao | LocalDateTime | Nao | `EntidadeBase` | Sem comportamento funcional novo nesta feature |

### Invariantes

- `idCliente` e `idVeiculo` nao podem ser nulos.
- `status` nao pode ser nulo.
- `itensComposicao` nao pode conter itens nulos.
- A ordem so pode encerrar composicao se possuir ao menos um item.
- A ordem so pode ir para `AGUARDANDO_RESPOSTA_CLIENTE` por meio de `encerrarComposicao()`.
- A ordem so pode voltar para `EM_COMPOSICAO` a partir de `AGUARDANDO_RESPOSTA_CLIENTE`.
- A ordem so pode ir para `AGUARDANDO_EXECUCAO` a partir de `AGUARDANDO_RESPOSTA_CLIENTE`.
- A ordem so pode ser cancelada a partir de `AGUARDANDO_RESPOSTA_CLIENTE` neste fluxo.

### Comportamentos

| Metodo | Entrada | Saida | Regra |
|--------|---------|-------|-------|
| `encerrarComposicao()` | Nenhuma | Nenhuma | Exige ao menos um item e move a ordem para `AGUARDANDO_RESPOSTA_CLIENTE` |
| `voltarParaComposicao()` | Nenhuma | Nenhuma | Exige ordem em `AGUARDANDO_RESPOSTA_CLIENTE` e move para `EM_COMPOSICAO` |
| `marcarComoAguardandoExecucao()` | Nenhuma | Nenhuma | Exige ordem em `AGUARDANDO_RESPOSTA_CLIENTE` e move para `AGUARDANDO_EXECUCAO` |
| `cancelar()` | Nenhuma | Nenhuma | Exige ordem em `AGUARDANDO_RESPOSTA_CLIENTE` e move para `CANCELADA` |

### Transicoes de Estado da Ordem

| Estado Atual | Acao | Estado Resultante |
|--------------|------|-------------------|
| `EM_COMPOSICAO` | `encerrarComposicao()` | `AGUARDANDO_RESPOSTA_CLIENTE` |
| `AGUARDANDO_RESPOSTA_CLIENTE` | `voltarParaComposicao()` | `EM_COMPOSICAO` |
| `AGUARDANDO_RESPOSTA_CLIENTE` | `marcarComoAguardandoExecucao()` | `AGUARDANDO_EXECUCAO` |
| `AGUARDANDO_RESPOSTA_CLIENTE` | `cancelar()` | `CANCELADA` |

## Objeto de Dominio: ItemComposicaoTecnica

### Descricao

Representa um item levantado durante o diagnostico da ordem e que podera compor uma proposta comercial.

### Campos

| Campo | Tipo | Obrigatorio | Regra |
|-------|------|-------------|-------|
| descricao | Texto | Sim | Deve identificar claramente o item |
| valor | Valor monetario | Sim | Deve representar o valor do item |
| tipo | `TipoItemComposicaoTecnica` | Sim | Deve ser `SERVICO`, `PECA` ou `INSUMO` |

## Enumeracao: TipoItemComposicaoTecnica

| Valor | Significado |
|-------|-------------|
| `SERVICO` | Trabalho executado pela oficina |
| `PECA` | Componente fisico substituido ou instalado |
| `INSUMO` | Material de consumo utilizado no atendimento |

## Entidade: Orcamento

### Descricao

Agregado raiz que representa a proposta enviada ao cliente para uma ordem de servico, com sua propria fotografia de itens e historico de decisao.

### Campos

| Campo | Tipo | Obrigatorio | Origem | Regra |
|-------|------|-------------|--------|-------|
| id | UUID | Sim | `EntidadeBase` | Identificador unico do orcamento |
| idOrdemServico | UUID | Sim | Dominio | Referencia a ordem vinculada |
| valor | Valor monetario | Sim | Dominio | Soma dos itens fotografados |
| status | `StatusOrcamento` | Sim | Dominio | Novo orcamento do fluxo nasce em `CRIADO` e depois vai para `PENDENTE_APROVACAO` |
| itens | Colecao de `ItemOrcamento` | Sim | Dominio | Fotografia da composicao da ordem no momento do encerramento |
| tipo | `TipoOrcamento` | Sim | Dominio | Mantido para compatibilidade, com uso principal em `SERVICO_ORIGINAL` neste MVP |
| dataCriacao | LocalDateTime | Sim | `EntidadeBase` | Auditoria |
| dataUltimaAtualizacao | LocalDateTime | Sim | `EntidadeBase` | Atualizada em cada transicao valida |
| dataRemocao | LocalDateTime | Nao | `EntidadeBase` | Sem comportamento funcional novo nesta feature |

### Invariantes

- `idOrdemServico`, `valor`, `status`, `tipo` e `itens` nao podem ser nulos.
- `itens` deve conter ao menos um item.
- Apenas `PENDENTE_APROVACAO` pode ser aprovado, rejeitado ou cancelado neste fluxo.
- O orcamento nao altera diretamente a ordem; a aplicacao coordena a ordem vinculada apos validar ambos os estados.

### Comportamentos

| Metodo | Entrada | Saida | Regra |
|--------|---------|-------|-------|
| `enviarParaAprovacao()` | Nenhuma | Nenhuma | Move de `CRIADO` para `PENDENTE_APROVACAO` |
| `aprovar()` | Nenhuma | Nenhuma | Exige `PENDENTE_APROVACAO` e move para `APROVADO` |
| `rejeitar()` | Nenhuma | Nenhuma | Exige `PENDENTE_APROVACAO` e move para `REJEITADO` |
| `cancelar()` | Nenhuma | Nenhuma | Exige `PENDENTE_APROVACAO` e move para `CANCELADO` |

### Transicoes de Estado do Orcamento

| Estado Atual | Acao | Estado Resultante |
|--------------|------|-------------------|
| `CRIADO` | `enviarParaAprovacao()` | `PENDENTE_APROVACAO` |
| `PENDENTE_APROVACAO` | `aprovar()` | `APROVADO` |
| `PENDENTE_APROVACAO` | `rejeitar()` | `REJEITADO` |
| `PENDENTE_APROVACAO` | `cancelar()` | `CANCELADO` |

## Objeto de Dominio: ItemOrcamento

### Descricao

Fotografia imutavel de um item enviado ao cliente dentro do orcamento.

### Campos

| Campo | Tipo | Obrigatorio | Regra |
|-------|------|-------------|-------|
| descricao | Texto | Sim | Copiada do item da ordem com sanitizacao |
| valor | Valor monetario | Sim | Copiado do item da ordem |

## Enumeracao: StatusOrdemServico

| Valor | Significado |
|-------|-------------|
| `EM_COMPOSICAO` | Ordem em levantamento tecnico |
| `AGUARDANDO_RESPOSTA_CLIENTE` | Orcamento enviado aguardando decisao |
| `AGUARDANDO_EXECUCAO` | Orcamento aprovado aguardando inicio do servico |
| `CANCELADA` | Atendimento encerrado sem continuidade |
| `FINALIZADA` | Estado reservado ao fluxo posterior de conclusao do servico |

## Enumeracao: StatusOrcamento

| Valor | Significado |
|-------|-------------|
| `CRIADO` | Orcamento montado, ainda nao enviado |
| `PENDENTE_APROVACAO` | Orcamento enviado aguardando resposta |
| `APROVADO` | Orcamento aceito |
| `REJEITADO` | Orcamento recusado |
| `CANCELADO` | Orcamento cancelado |

## Relacionamentos

- `OrdemServico` possui varios `ItemComposicaoTecnica`.
- `Orcamento` referencia uma `OrdemServico` por `idOrdemServico`.
- `Orcamento` possui varios `ItemOrcamento`.
- Uma `OrdemServico` pode ter varios `Orcamento` historicos.
- Uma `OrdemServico` pode ter no maximo um `Orcamento` com status `PENDENTE_APROVACAO`.

## Persistencia Prevista

| Estrutura | Papel |
|-----------|-------|
| `ordens_servico` | Tabela raiz da ordem |
| `ordens_servico_itens` | Itens tecnicos editaveis da ordem |
| `orcamentos` | Tabela raiz do orcamento |
| `orcamentos_itens` | Fotografia dos itens enviados ao cliente |

## Consultas Necessarias

| Consulta | Uso |
|----------|-----|
| Buscar ordem por id | Todos os casos de uso ligados a ordem |
| Buscar orcamento por id | Aprovar, rejeitar e cancelar |
| Verificar existencia de orcamento pendente por ordem | Encerrar composicao |
| Salvar ordem | Encerrar composicao, rejeitar, aprovar, cancelar |
| Salvar orcamento | Encerrar composicao, aprovar, rejeitar, cancelar |

# Data Model: Ciclo de Execucao da Ordem de Servico

## Entidade: OrdemServico

### Descricao

Agregado raiz que representa o atendimento da oficina, agora com ciclo pos-aprovacao completo ate a entrega do veiculo.

### Campos novos ou alterados

| Campo | Tipo | Obrigatorio | Regra |
|-------|------|-------------|-------|
| status | `StatusOrdemServico` | Sim | Deve aceitar `EM_EXECUCAO` e `ENTREGUE` alem dos estados existentes |
| dataInicioExecucao | Data/hora | Nao | Preenchida ao iniciar execucao |
| dataFinalizacao | Data/hora | Nao | Preenchida ao finalizar execucao tecnica |
| dataEntrega | Data/hora | Nao | Preenchida ao entregar o veiculo |

### Invariantes

- A ordem so pode iniciar execucao a partir de `AGUARDANDO_EXECUCAO`.
- A ordem so pode finalizar execucao a partir de `EM_EXECUCAO`.
- A ordem so pode ser entregue a partir de `FINALIZADA`.
- Uma transicao invalida nao altera status nem datas.
- Cada transicao valida atualiza `dataUltimaAtualizacao`.

### Comportamentos novos

| Metodo | Estado exigido | Estado resultante | Efeito adicional |
|--------|----------------|-------------------|------------------|
| `iniciarExecucao()` | `AGUARDANDO_EXECUCAO` | `EM_EXECUCAO` | Define `dataInicioExecucao` |
| `finalizarExecucao()` | `EM_EXECUCAO` | `FINALIZADA` | Define `dataFinalizacao` |
| `entregar()` | `FINALIZADA` | `ENTREGUE` | Define `dataEntrega` |

### Transicoes de estado completas

| Estado Atual | Acao | Estado Resultante |
|--------------|------|-------------------|
| `EM_COMPOSICAO` | Encerrar composicao | `AGUARDANDO_RESPOSTA_CLIENTE` |
| `AGUARDANDO_RESPOSTA_CLIENTE` | Rejeitar orcamento | `EM_COMPOSICAO` |
| `AGUARDANDO_RESPOSTA_CLIENTE` | Aprovar orcamento | `AGUARDANDO_EXECUCAO` |
| `AGUARDANDO_RESPOSTA_CLIENTE` | Cancelar orcamento | `CANCELADA` |
| `AGUARDANDO_EXECUCAO` | Iniciar execucao | `EM_EXECUCAO` |
| `EM_EXECUCAO` | Finalizar execucao | `FINALIZADA` |
| `FINALIZADA` | Entregar veiculo | `ENTREGUE` |

## Entidade: HistoricoStatusOrdemServico

### Descricao

Registro auditavel de uma transicao de status bem-sucedida da ordem de servico.

### Campos

| Campo | Tipo | Obrigatorio | Regra |
|-------|------|-------------|-------|
| id | UUID | Sim | Identificador unico do historico |
| idOrdemServico | UUID | Sim | Ordem de servico vinculada |
| statusAnterior | `StatusOrdemServico` | Sim | Status antes da transicao |
| statusNovo | `StatusOrdemServico` | Sim | Status depois da transicao |
| dataTransicao | Data/hora | Sim | Momento em que a transicao ocorreu |
| idUsuario | UUID | Sim | Usuario autenticado responsavel |
| usernameUsuario | Texto | Sim | Nome de usuario responsavel para auditoria legivel |
| dataCriacao | Data/hora | Sim | Auditoria do registro |
| dataUltimaAtualizacao | Data/hora | Sim | Auditoria do registro |

### Invariantes

- `idOrdemServico` nao pode ser nulo.
- `statusAnterior` e `statusNovo` nao podem ser nulos.
- `statusAnterior` deve ser diferente de `statusNovo`.
- `dataTransicao` nao pode ser nula.
- `idUsuario` e `usernameUsuario` sao obrigatorios.
- Historico e imutavel para fins funcionais apos criado.
- Nao ha criacao retroativa de historico para transicoes anteriores a esta feature.

## Entidade: Usuario Responsavel

### Descricao

Representa o usuario autenticado que executa ou consulta transicoes do ciclo da OS.

### Campos utilizados

| Campo | Tipo | Obrigatorio | Regra |
|-------|------|-------------|-------|
| idUsuario | UUID | Sim | Obtido da sessao autenticada |
| username | Texto | Sim | Obtido da sessao autenticada |
| perfis | Colecao | Sim | `ADMINISTRADOR`, `MECANICO` e `ATENDENTE` podem consultar historico; transicoes seguem permissoes especificas |

## DTO: HistoricoStatusOrdemServicoResponse

### Descricao

Representacao de uma transicao de status da OS retornada na consulta de historico.

### Campos

| Campo | Tipo | Obrigatorio | Regra |
|-------|------|-------------|-------|
| id | UUID | Sim | Identificador do historico |
| idOrdemServico | UUID | Sim | Ordem vinculada |
| statusAnterior | Texto | Sim | Status antes da transicao |
| statusNovo | Texto | Sim | Status depois da transicao |
| dataTransicao | Data/hora | Sim | Momento da transicao |
| idUsuario | UUID | Sim | Usuario responsavel |
| usernameUsuario | Texto | Sim | Nome do usuario responsavel |

## Relacionamentos

- Uma `OrdemServico` possui zero ou muitos `HistoricoStatusOrdemServico`.
- Cada `HistoricoStatusOrdemServico` pertence a exatamente uma `OrdemServico`.
- Cada historico referencia o usuario responsavel pela transicao.

## Persistencia Prevista

| Estrutura | Papel |
|-----------|-------|
| `ordens_servico` | Recebe novas colunas de timestamps do ciclo |
| `historico_status_os` | Armazena a trilha auditavel de transicoes |

## Consultas Necessarias

| Consulta | Uso |
|----------|-----|
| Buscar ordem por id | Executar transicoes e validar consulta de historico |
| Salvar ordem | Persistir novo status e timestamps |
| Salvar historico | Auditar transicao bem-sucedida |
| Listar historico por ordem ordenado por data | Expor auditoria da OS e metricas futuras |

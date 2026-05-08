# Research: Ciclo de Execucao da Ordem de Servico

## Decision: Semantica dos estados pos-aprovacao

**Decision**: Usar `EM_EXECUCAO` para trabalho tecnico iniciado, manter `FINALIZADA` para trabalho tecnico concluido e adicionar `ENTREGUE` para retirada pelo cliente.

**Rationale**: A separacao permite distinguir fila, execucao, veiculo pronto e atendimento encerrado. Essa diferenca e necessaria para acompanhamento operacional e para metricas futuras.

**Alternatives considered**:

- Usar apenas `FINALIZADA`: simples, mas nao diferencia veiculo pronto de veiculo entregue.
- Usar apenas `ENTREGUE`: perde o marco operacional de conclusao tecnica.

## Decision: Timestamps no agregado e historico auditavel separado

**Decision**: Armazenar `dataInicioExecucao`, `dataFinalizacao` e `dataEntrega` na OS e registrar todas as transicoes em `HistoricoStatusOrdemServico`.

**Rationale**: Os timestamps na OS facilitam respostas e consultas operacionais. O historico separado preserva auditoria completa e evita inferir metricas a partir de uma unica data de ultima atualizacao.

**Alternatives considered**:

- Apenas timestamps na OS: nao reconstrui linha do tempo completa nem responsavel por transicao.
- Apenas historico: obriga consultas adicionais para qualquer resposta operacional simples.

## Decision: Historico centralizado em servico de aplicacao

**Decision**: Criar um registrador de historico na camada de aplicacao para ser usado por transicoes novas e existentes.

**Rationale**: A camada de aplicacao ja orquestra repositorios e conhece o usuario autenticado. Centralizar evita duplicacao e mantem o dominio livre de infraestrutura e seguranca.

**Alternatives considered**:

- Registrar historico dentro da entidade de dominio: acoplaria dominio a persistencia ou exigiria eventos ainda inexistentes no projeto.
- Registrar diretamente em cada controller: colocaria regra operacional fora dos casos de uso e duplicaria logica.

## Decision: Responsavel gravado por id e username

**Decision**: Cada historico deve guardar o identificador do usuario e o username usado na transicao.

**Rationale**: O id permite rastreabilidade tecnica, enquanto o username facilita leitura operacional e auditoria sem depender de join em telas simples.

**Alternatives considered**:

- Guardar apenas username: fragil em caso de renomeacao.
- Guardar apenas id: menos legivel em auditoria e troubleshooting.

## Decision: Historico consultavel nesta feature

**Decision**: Expor consulta autenticada de historico por OS para `ADMINISTRADOR`, `MECANICO` e `ATENDENTE`.

**Rationale**: A User Story 4 exige auditoria verificavel pelo usuario. Permitir consulta direta evita depender de inspecao interna de banco e cria contrato claro para validacao do ciclo.

**Alternatives considered**:

- Validar historico apenas internamente: reduziria superficie HTTP, mas enfraqueceria o valor de auditoria para gestores.
- Expor historico apenas dentro da OS: acoplaria resposta principal a uma lista potencialmente crescente.
- Restringir apenas a administrador: mais fechado, mas impediria perfis operacionais de auditar transicoes que eles executam.

## Decision: Sem backfill retroativo

**Decision**: O historico auditavel comeca a partir das transicoes realizadas apos a implantacao desta feature; ordens antigas podem retornar historico vazio ou parcial.

**Rationale**: Criar registros retroativos inventaria responsavel e data de transicao, reduzindo a confianca da auditoria.

**Alternatives considered**:

- Criar registro inicial com usuario sistema: util para sinalizar estado atual, mas nao representa uma transicao real.
- Backfill manual completo: caro e inconsistente sem fonte confiavel dos eventos anteriores.

## Decision: Fluxos existentes tambem registram historico

**Decision**: Encerramento de composicao, aprovacao, rejeicao e cancelamento devem usar o mesmo registrador de historico.

**Rationale**: A feature promete linha do tempo completa da OS para transicoes futuras. Registrar apenas os novos estados criaria historico parcial para ordens novas.

**Alternatives considered**:

- Registrar apenas transicoes novas: menor escopo imediato, mas contradiz a necessidade de auditoria completa.

## Decision: Estoque fora do escopo

**Decision**: O inicio de execucao nesta feature nao altera estoque.

**Rationale**: A politica de reserva e baixa foi separada para o proximo bloco do roadmap. Misturar estoque aqui aumentaria risco e dificultaria validar o ciclo de status isoladamente.

**Alternatives considered**:

- Baixar estoque no inicio da execucao agora: antecipa decisao ainda planejada para feature propria.
- Baixar estoque na finalizacao: conflita com a direcao ja documentada de reserva/baixa.

## Decision: Manter politica HTTP atual para regra de negocio

**Decision**: Transicoes invalidas continuarao seguindo a traducao atual de `RegraDeNegocioException` para resposta de regra violada.

**Rationale**: O projeto e seus testes ja usam esse comportamento. Corrigir eventual divergencia com a constituicao deve ser uma mudanca transversal separada.

**Alternatives considered**:

- Alterar `GlobalExceptionHandler` nesta feature: alto risco de regressao em controllers existentes.

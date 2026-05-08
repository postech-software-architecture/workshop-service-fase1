# Research: Estoque Integrado ao Ciclo da Ordem

## Decision: Vincular movimentacoes a OS e orcamento

**Rationale**: A especificacao exige rastreabilidade operacional e o roadmap aponta que depender do texto do motivo e fragil. A OS e o contexto minimo para auditoria do ciclo; o orcamento complementa a origem comercial da reserva/liberacao. Usar ambos evita buscas por string e permite consultas por OS sem perder a referencia do orcamento que gerou o compromisso.

**Alternatives considered**:

- Apenas `ordem_servico_id`: atende auditoria por OS, mas perde relacao direta com o orcamento quando houver historico comercial mais rico.
- Apenas `orcamento_id`: vincula a decisao comercial, mas dificulta consultas operacionais por OS.
- Manter filtro por motivo textual: rejeitado por fragilidade, duplicidade possivel e baixa confiabilidade para auditoria.

## Decision: Lock otimista usando a versao existente de estoque

**Rationale**: `EstoqueJpaEntity` ja possui `@Version`, e o dominio `Estoque` ja carrega `versao`. O fluxo deve validar saldo, alterar quantidade e salvar estoque dentro da mesma transacao. Quando duas aprovacoes disputarem o mesmo saldo, somente uma confirmara a versao; a outra deve ser convertida em erro de negocio claro para o usuario revisar a disponibilidade.

**Alternatives considered**:

- Lock pessimista na leitura: reduz conflitos tardios, mas aumenta acoplamento a consultas especificas e bloqueios no banco.
- Sem lock adicional: rejeitado porque aprovacoes concorrentes poderiam exceder o saldo disponivel.

## Decision: Reserva criada na aprovacao e consumida no inicio da execucao

**Rationale**: A aprovacao e o momento de compromisso com o cliente, enquanto o inicio da execucao e o momento de consumo real. Isso preserva a diferenca de negocio entre saldo comprometido e item efetivamente usado.

**Alternatives considered**:

- Consumir direto na aprovacao: simplifica estoque, mas perde visibilidade entre compromisso futuro e consumo real.
- Reservar apenas no inicio da execucao: permite vender estoque ja prometido para outra OS entre aprovacao e execucao.

## Decision: Consumo idempotente baseado em reservas ativas por OS

**Rationale**: O inicio de execucao pode ser chamado mais de uma vez por erro operacional ou retry. O sistema deve localizar reservas ativas da OS e gerar saidas somente para o saldo ainda reservado. A existencia de saida ja criada para a OS/item impede consumo duplicado.

**Alternatives considered**:

- Bloquear apenas pelo status da OS: ajuda, mas nao protege completamente retries em falhas parciais.
- Consumir todos os itens da composicao novamente: rejeitado porque recria consumo e distorce estoque.

## Decision: Liberacao limitada ao saldo reservado ainda nao consumido

**Rationale**: Rejeicao e cancelamento antes da execucao devem devolver ao disponivel somente o que ainda esta reservado. Se a reserva ja virou saida, o fluxo nao deve gerar liberacao automatica.

**Alternatives considered**:

- Liberar sempre pelo total do item de composicao: rejeitado porque pode devolver itens ja consumidos.
- Ignorar liberacao quando houver qualquer saida na OS: rejeitado porque uma OS pode ter multiplos itens com estados diferentes.

## Decision: Movimentacoes RESERVA, LIBERACAO e AJUSTE ficam fora do endpoint manual

**Rationale**: O endpoint manual de movimentacao deve aceitar apenas entradas e saidas operacionais do usuario, mantendo reserva/liberacao sob controle dos fluxos de orcamento e OS e deixando ajuste fora deste escopo. Isso evita manipulacao manual de compromissos de estoque e mudancas absolutas de saldo nao cobertas pela feature.

**Alternatives considered**:

- Expor RESERVA/LIBERACAO no request manual: rejeitado por risco de inconsistencia do ciclo da OS.
- Expor AJUSTE no request manual: rejeitado porque o roadmap delimitou o endpoint manual a ENTRADA e SAIDA.
- Criar endpoints especificos para reserva/liberacao: desnecessario neste escopo, pois os eventos ja pertencem a aprovar/rejeitar/cancelar/iniciar execucao.

## Decision: Consulta de movimentacoes de estoque filtrada por OS

**Rationale**: A auditoria operacional precisa mostrar reservas, saidas e liberacoes de uma ordem sem misturar com historico de status. Uma consulta de movimentacoes filtrada por OS atende a historia de auditoria e aproveita o novo vinculo tecnico em `MovimentacaoEstoque`.

**Alternatives considered**:

- Incluir movimentacoes no historico de status da OS: rejeitado porque mistura eventos de natureza diferente.
- Persistir rastreabilidade sem consulta de usuario: rejeitado porque nao satisfaz a historia de auditoria da especificacao.

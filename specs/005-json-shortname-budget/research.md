# Research: Encerramento de Composicao Tecnica e Fluxo de Orcamento

## Decision 1: Migrar `OrdemServico` para o novo ciclo de vida do MVP

- **Decision**: Substituir a semantica atual de estados da ordem por um fluxo centrado em composicao tecnica e resposta do cliente, com destaque para `EM_COMPOSICAO`, `AGUARDANDO_RESPOSTA_CLIENTE`, `AGUARDANDO_EXECUCAO` e `CANCELADA`.
- **Rationale**: Os casos de uso pedidos dependem explicitamente desses estados, e manter a nomenclatura antiga causaria inconsistencias entre dominio, testes e regras de aprovacao do orcamento.
- **Alternatives considered**:
  - Mapear os estados novos sobre os antigos sem alterar o enum: rejeitado por gerar linguagem ubqua ambigua e regras indiretas.
  - Tratar a mudanca apenas na aplicacao: rejeitado porque as invariantes pertencem ao dominio.

## Decision 2: Modelar itens da composicao tecnica da ordem como estrutura propria de dominio

- **Decision**: Introduzir um tipo de dominio para item de composicao tecnica da ordem, com classificacao explicita entre `SERVICO`, `PECA` e `INSUMO`.
- **Rationale**: O encerramento da composicao depende da existencia desses itens, e o dominio atual de `OrdemServico` nao possui como representar nem validar essa condicao.
- **Alternatives considered**:
  - Reutilizar `ItemOrcamento` diretamente dentro da ordem: rejeitado porque o item da ordem e editavel, enquanto o item do orcamento e fotografia.
  - Persistir apenas valor total na ordem: rejeitado porque a feature exige copia item a item para o orcamento.

## Decision 3: Usar o orcamento como agregado independente com fotografia imutavel dos itens

- **Decision**: Manter `Orcamento` como agregado proprio, armazenando sua propria colecao de `ItemOrcamento` copiada a partir da ordem no instante do encerramento da composicao.
- **Rationale**: A spec exige historico da proposta enviada ao cliente, preservado mesmo que a ordem volte para composicao e os itens sejam alterados depois.
- **Alternatives considered**:
  - Referenciar os itens da ordem por associacao mutavel: rejeitado por quebrar a regra de fotografia.
  - Embutir orcamento dentro da ordem como subobjeto sem historico: rejeitado por dificultar controle de estados e auditoria de multiplas propostas.

## Decision 4: A aplicacao coordena ordem e orcamento em uma unica transacao

- **Decision**: Os casos de uso devem buscar agregados, validar pre-condicoes, acionar comportamentos de dominio e persistir ordem e orcamento na mesma transacao.
- **Rationale**: As transicoes de status de ordem e orcamento precisam permanecer consistentes, sem estados intermediarios visiveis em caso de falha.
- **Alternatives considered**:
  - Atualizar apenas o orcamento e corrigir a ordem em etapa posterior: rejeitado por risco de inconsistencia.
  - Empurrar toda a logica para entidades de dominio cruzadas: rejeitado porque a orquestracao de repositorios e notificacoes pertence aplicacao.

## Decision 5: Criar portas de notificacao simples com implementacao por log

- **Decision**: Definir `ClienteNotificationService` e `MecanicoNotificationService` como dependencias da camada de aplicacao, com implementacao inicial que apenas registra logs.
- **Rationale**: A necessidade atual e evidenciar o envio sem integrar um provedor externo, mantendo a possibilidade de trocar a implementacao depois.
- **Alternatives considered**:
  - Disparar log diretamente nos use cases: rejeitado por misturar regra de negocio com detalhe operacional.
  - Integrar e-mail real agora: rejeitado por estar explicitamente fora de escopo.

## Decision 6: Introduzir persistencia dedicada para ordem e orcamento nesta feature

- **Decision**: Criar repositorios de dominio, implementacoes JPA, entidades persistentes e migrations Flyway para `OrdemServico`, itens da ordem, `Orcamento` e itens do orcamento.
- **Rationale**: Os casos de uso pedem busca e persistencia de ambos os agregados, e o projeto ainda nao possui infraestrutura para isso.
- **Alternatives considered**:
  - Implementar apenas em memoria: rejeitado por nao atender o fluxo real da aplicacao.
  - Adiar persistencia para outra feature: rejeitado porque inviabiliza os casos de uso solicitados agora.

## Decision 7: Garantir unicidade de orcamento pendente por regra de dominio e por banco

- **Decision**: Bloquear a existencia de mais de um `PENDENTE_APROVACAO` por ordem tanto no caso de uso/repositorio quanto por restricao de banco apropriada.
- **Rationale**: A regra e central para consistencia do fluxo; validacao so em memoria nao protege contra concorrencia ou integracoes futuras.
- **Alternatives considered**:
  - Confiar apenas em teste unitario e validacao no use case: rejeitado por deixar brecha transacional.
  - Bloquear qualquer multiplo orcamento por ordem: rejeitado porque o historico de propostas rejeitadas e permitido.

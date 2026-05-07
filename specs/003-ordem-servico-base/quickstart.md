# Quickstart: Entidade Base de Ordem de Servico

## Objetivo

Implementar a base de dominio de `OrdemServico` sem expor interface externa e sem adicionar fluxo completo de aplicacao.

## Passos de Implementacao

1. Criar o enum `StatusOrdemServico` em `src/main/java/com/postech/workshop_service/domain/entities/` com os valores:
   - `RECEBIDA`
   - `AGUARDANDO_APROVACAO_ORCAMENTO`
   - `EM_EXECUCAO`
   - `CANCELADA`
   - `FINALIZADA`
2. Criar `OrdemServico` em `src/main/java/com/postech/workshop_service/domain/entities/` herdando de `EntidadeBase`.
3. Adicionar os atributos `idCliente`, `idVeiculo` e `status`.
4. Garantir construcao de nova entidade sempre com status `RECEBIDA`.
5. Implementar `podeSerCancelada()` retornando `true` apenas para `RECEBIDA` e `AGUARDANDO_APROVACAO_ORCAMENTO`.
6. Implementar `cancelar()` alterando o status para `CANCELADA` quando permitido e lancando `RegraDeNegocioException` nos demais casos.
7. Atualizar `dataUltimaAtualizacao` quando houver cancelamento efetivo.
8. Adicionar Javadoc nos metodos publicos da entidade.
9. Criar testes unitarios em `src/test/java/com/postech/workshop_service/domain/entities/` cobrindo:
   - criacao com status inicial `RECEBIDA`
   - retorno de `podeSerCancelada()` para cada status relevante
   - cancelamento permitido
   - cancelamento bloqueado sem alterar estado

## Validacao

1. Executar `mvn test`.
2. Confirmar que nao foram criados controller, endpoint, repository, migration ou use case completo para `OrdemServico`.
3. Revisar se o dominio permaneceu sem setters genericos para alteracao de status.

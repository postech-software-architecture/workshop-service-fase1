# Quickstart: Entidade de Dominio Orcamento

## Objetivo

Implementar a entidade de dominio `Orcamento` e sua coordenacao com `OrdemServico`, sem expor interface externa e sem introduzir persistencia dedicada nesta fase.

## Passos de Implementacao

1. Criar `StatusOrcamento` em `src/main/java/com/postech/workshop_service/domain/entities/`.
2. Criar `TipoOrcamento` em `src/main/java/com/postech/workshop_service/domain/entities/`.
3. Criar o tipo de dominio `ItemOrcamento` em `src/main/java/com/postech/workshop_service/domain/entities/`.
4. Criar `Orcamento` em `src/main/java/com/postech/workshop_service/domain/entities/` herdando de `EntidadeBase`.
5. Garantir criacao de nova entidade sempre com status `CRIADO`.
6. Implementar `enviarParaAprovacao()` para transicao de `CRIADO` para `PENDENTE_APROVACAO`.
7. Implementar `aprovar(OrdemServico ordemServico)` para transicao de `PENDENTE_APROVACAO` para `APROVADO`.
8. Ao aprovar um `SERVICO_ORIGINAL`, avancar a `OrdemServico` vinculada para `EM_EXECUCAO` por comportamento de dominio apropriado.
9. Implementar `rejeitar()` para transicao de `PENDENTE_APROVACAO` para `REJEITADO`.
10. Implementar `cancelar(OrdemServico ordemServico)` para transicao aos casos permitidos, cancelando a ordem somente quando o tipo for `SERVICO_ORIGINAL` e a ordem ainda permitir cancelamento.
11. Ajustar `OrdemServico` com o comportamento de entrar em execucao, preservando a regra de cancelamento ja existente.
12. Adicionar Javadoc nos metodos publicos novos.
13. Criar testes unitarios cobrindo:
    - criacao do orcamento com status inicial `CRIADO`
    - envio para aprovacao permitido e bloqueado
    - aprovacao permitida e bloqueada
    - rejeicao permitida e bloqueada
    - cancelamento do `SERVICO_ORIGINAL` com e sem cancelamento da ordem
    - cancelamento do `ADICAO_SERVICO` sem impacto na ordem
    - aprovacao do `SERVICO_ORIGINAL` avancando a ordem para `EM_EXECUCAO`

## Validacao

1. Executar `mvn test`.
2. Confirmar que o dominio continua sem setters genericos para alteracao de estado.
3. Confirmar que nao foram criados controller, endpoint, repository, migration ou caso de uso completo do orcamento.

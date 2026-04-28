# Research: Entidade de Dominio Orcamento

## Decision 1: Usar um agregado de dominio proprio para o orcamento

- **Decision**: `Orcamento` sera modelado como entidade de dominio propria, vinculada a `OrdemServico` por identificador e por coordenacao comportamental.
- **Rationale**: O orcamento possui ciclo de vida, tipos e regras de aprovacao/cancelamento suficientes para justificar um agregado proprio e rico.
- **Alternatives considered**:
  - Tratar orcamento como atributo simples da ordem de servico: rejeitado por concentrar regras comerciais demais em `OrdemServico`.
  - Modelar apenas DTO ou estrutura anemica: rejeitado por violar a constituicao de dominio rico.

## Decision 2: Reutilizar `RegraDeNegocioException` para transicoes invalidas

- **Decision**: Regras invalidas de envio, aprovacao, rejeicao ou cancelamento do orcamento devem lancar `RegraDeNegocioException`.
- **Rationale**: O projeto ja usa essa excecao como semantica padrao para violacao de regra de negocio.
- **Alternatives considered**:
  - `IllegalStateException`: rejeitado por ser menos expressivo para o dominio.
  - Criar uma excecao nova por operacao: rejeitado por ampliar complexidade sem necessidade imediata.

## Decision 3: Introduzir um comportamento explicito em `OrdemServico` para entrar em execucao

- **Decision**: `OrdemServico` precisa expor um comportamento de negocio explicito para avancar para `EM_EXECUCAO` quando o orcamento inicial for aprovado.
- **Rationale**: A especificacao do orcamento exige esse efeito colateral e a constituicao proibe setters genericos e dominio anemico.
- **Alternatives considered**:
  - Alterar status da ordem diretamente via setter: rejeitado por violar encapsulamento.
  - Ignorar a transicao e deixar para outra etapa: rejeitado porque a regra e parte do comportamento atual do orcamento.

## Decision 4: Representar itens do orcamento como tipo de dominio proprio

- **Decision**: Os itens do orcamento devem existir como colecao de um tipo de dominio proprio, mesmo com comportamento inicial simples.
- **Rationale**: A entidade precisa carregar itens desde esta etapa e a criacao de um tipo dedicado reduz acoplamento e prepara evolucao futura sem rejeicao parcial agora.
- **Alternatives considered**:
  - Usar apenas texto livre ou estrutura generica: rejeitado por empobrecer a semantica do dominio.
  - Adiar os itens para uma fase posterior: rejeitado porque a especificacao ja os exige.

## Decision 5: Manter esta fase sem persistencia e sem contrato externo

- **Decision**: Nao criar migration, entidade JPA, repository, controller, endpoint ou caso de uso completo do orcamento nesta fase.
- **Rationale**: O objetivo desta entrega e consolidar as regras de dominio do orcamento e sua interacao com `OrdemServico`.
- **Alternatives considered**:
  - Criar persistencia e endpoints agora: rejeitado por ampliar escopo alem do necessario para a fase.

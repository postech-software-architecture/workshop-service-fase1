# Research: Entidade Base de Ordem de Servico

## Decision 1: Definir `RECEBIDA` como status inicial

- **Decision**: Toda nova `OrdemServico` deve nascer com o status `RECEBIDA`.
- **Rationale**: A clarificacao do requisito remove ambiguidade sobre o estado inicial e alinha a nomenclatura da entidade com o processo operacional desejado.
- **Alternatives considered**:
  - `CRIADA` como status inicial: rejeitado apos clarificacao explicita do requisito.
  - Permitir status inicial variavel: rejeitado por aumentar risco de uso inconsistente da entidade base.

## Decision 2: Centralizar a regra de cancelamento no dominio

- **Decision**: O comportamento de cancelamento sera implementado dentro da propria `OrdemServico`, com o metodo `podeSerCancelada()` e o metodo `cancelar()`.
- **Rationale**: A constituicao exige dominio rico e encapsulado; colocar a regra na entidade evita anemizacao e mantem a decisao proxima do estado que ela protege.
- **Alternatives considered**:
  - Validar cancelamento em service/use case: rejeitado porque esta etapa nao inclui casos de uso completos e isso enfraqueceria o dominio.
  - Expor setters para troca direta de status: rejeitado por violar encapsulamento.

## Decision 3: Reutilizar `RegraDeNegocioException` para cancelamento invalido

- **Decision**: Tentativas de cancelamento em estados nao permitidos devem lancar `RegraDeNegocioException`.
- **Rationale**: O projeto ja possui uma excecao de aplicacao para sinalizar violacoes de regra de negocio; reutiliza-la reduz duplicacao e mantem semantica consistente.
- **Alternatives considered**:
  - `IllegalStateException`: rejeitado por ser menos expressivo para o dominio do projeto.
  - Criar nova excecao especifica nesta etapa: rejeitado por adicionar complexidade sem ganho claro no escopo atual.

## Decision 4: Manter esta entrega sem persistencia ou contrato externo

- **Decision**: Nao criar migration, entidade JPA, repository, controller, endpoint ou contrato publico para `OrdemServico` nesta fase.
- **Rationale**: O objetivo declarado e preparar a entidade base de dominio para agregados futuros, especialmente orcamento, sem antecipar infraestrutura ou API.
- **Alternatives considered**:
  - Criar tabela e mapeamento JPA agora: rejeitado por ampliar escopo sem necessidade funcional imediata.
  - Criar endpoint placeholder: rejeitado por conflitar com a restricao explicita da spec.

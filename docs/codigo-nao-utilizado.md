# Codigo nao utilizado em producao

Analise feita por busca de referencias em `src/main/java` e revisao manual do contexto do MVP.

## Sem chamada em fluxo de producao atual

- `Usuario.possuiRole(Role)`: usado apenas em teste unitario; roles sao lidas via principal/autorizacao Spring.
- `Usuario.atualizarSenha(String)`: nao ha caso de uso de troca/reset de senha no MVP atual.
- `Usuario.bloquear()` e `Usuario.desbloquear()`: nao ha caso de uso administrativo de bloqueio/desbloqueio no MVP atual.
- `MovimentacaoEstoque.isEntrada()`, `isSaida()` e `isAjuste()`: consultas auxiliares sem chamada em adapters, usecases ou controllers.
- `ListarClientesUseCase.contarTotal()`: usado apenas em teste unitario; controller de listagem retorna lista simples e nao usa total.

## Ja removido no worktree

- `Estoque.atualizarLocalizacao(String)`: sem chamada em producao.
- `PecaInsumo.incrementarVersao()`: sem chamada em producao; versionamento fica no JPA/optimistic lock.
- `PecaInsumo.getSkuNormalizado()`: sem chamada em producao; normalizacao ocorre nos usecases/repository queries.
- `JpaMovimentacaoEstoqueRepository.findByEstoqueIdAndTipoOrderByDataMovimentacaoDesc(...)`: sem chamada apos simplificacao do adapter.
- `JpaMovimentacaoEstoqueRepository.findByEstoqueIdAndPeriodo(...)`: sem chamada e query opcional quebrava no PostgreSQL.
- `JpaMovimentacaoEstoqueRepository.findByEstoqueIdWithFilters(...)`: sem chamada e query opcional quebrava no PostgreSQL.
- `JpaMovimentacaoEstoqueRepository.findByPecaInsumoIdWithFilters(...)`: sem chamada e query opcional quebrava no PostgreSQL.

## Observacao

Endpoints de controllers, beans de configuracao, handlers Spring Security e metodos de interface/override nao foram marcados como mortos mesmo sem chamada direta por `rg`, pois sao acionados por framework.

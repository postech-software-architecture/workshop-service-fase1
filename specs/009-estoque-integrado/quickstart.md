# Quickstart: Estoque Integrado ao Ciclo da Ordem

## Prerequisites

- Java 21
- Maven
- Docker available if running integration tests with Testcontainers
- Current branch: `009-estoque-integrado`

## Implementation Order

1. Add the Flyway migration linking `movimentacoes_estoque` to OS and orcamento.
2. Extend JPA entity, domain entity, mapper and repositories for `MovimentacaoEstoque`.
3. Add repository queries for active reservations by OS/orcamento/stock item.
4. Update `AprovarOrcamentoUseCase` to validate stock, reserve items and persist all state in one transaction.
5. Update `IniciarExecucaoUseCase` to consume active reservations exactly once.
6. Update `RejeitarOrcamentoUseCase` and `CancelarOrcamentoUseCase` to release reservations by technical link instead of motivo text.
7. Update controllers/OpenAPI schemas so manual stock movement accepts only ENTRADA and SAIDA.
8. Add a stock movement query filtered by service order.
9. Add/extend unit and integration tests for the scenarios in the spec.

## Validation Commands

Run focused tests while implementing:

```powershell
mvn "-Dtest=AprovarOrcamentoUseCaseTest,RejeitarOrcamentoUseCaseTest,CancelarOrcamentoUseCaseTest,IniciarExecucaoUseCaseTest,EstoqueTest,MovimentacaoEstoqueTest" test
```

Run repository/controller integration coverage for this feature:

```powershell
mvn "-Dtest=EstoqueRepositoryImplIT,MovimentacaoEstoqueRepositoryImplIT,OrcamentoControllerIT,OrdemServicoControllerIT,EstoqueControllerIT" test
```

Run the full test suite before closing the feature:

```powershell
mvn test
```

## Expected Manual Checks

- Approving a budget with enough stock creates one reservation per stock-controlled item.
- Approving a budget with insufficient stock fails without partial changes.
- Rejecting a budget releases only active reservations for that OS.
- Cancelling a budget releases only active reservations for that OS.
- Starting execution consumes active reservations exactly once.
- Repeating approval or execution start does not duplicate stock movement.
- Concurrent approvals for the same limited stock leave the final reserved quantity within the available quantity.
- Manual stock movement documentation excludes reservation, release and adjustment usage.
- Stock movements can be queried by service order and do not include movements from other orders.

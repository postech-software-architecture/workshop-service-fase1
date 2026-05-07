# Quickstart: Ciclo de Execucao da Ordem de Servico

## Objetivo

Implementar o ciclo operacional completo da ordem de servico apos aprovacao do orcamento: iniciar execucao, finalizar execucao tecnica, entregar veiculo e consultar historico auditavel de status.

## Passos de Implementacao

1. Criar migration Flyway para adicionar em `ordens_servico`:
   - `data_inicio_execucao`
   - `data_finalizacao`
   - `data_entrega`
2. Criar migration Flyway para `historico_status_os` com UUID, referencia para OS, status anterior, status novo, data da transicao, usuario responsavel, auditoria e comentarios SQL.
3. Atualizar `StatusOrdemServico` com `EM_EXECUCAO` e `ENTREGUE`.
4. Evoluir `OrdemServico` para guardar os tres timestamps e expor:
   - `iniciarExecucao()`
   - `finalizarExecucao()`
   - `entregar()`
5. Atualizar `OrdemServicoJpaEntity` e `OrdemServicoMapper` para os novos timestamps.
6. Criar `HistoricoStatusOrdemServico` no dominio.
7. Criar `HistoricoStatusOrdemServicoRepository` no dominio.
8. Criar entidade JPA, mapper, Spring Data repository e adapter para historico.
9. Criar um componente de aplicacao para registrar historico recebendo ordem, status anterior, status novo e usuario autenticado.
10. Implementar:
    - `IniciarExecucaoUseCase`
    - `FinalizarExecucaoUseCase`
    - `EntregarVeiculoUseCase`
    - `ConsultarHistoricoOrdemServicoUseCase`
11. Ajustar fluxos existentes para registrar historico:
    - `EncerrarComposicaoTecnicaUseCase`
    - `AprovarOrcamentoUseCase`
    - `RejeitarOrcamentoUseCase`
    - `CancelarOrcamentoUseCase`
12. Atualizar `OrdemServicoController` com as tres transicoes e a consulta de historico.
13. Criar `HistoricoStatusOrdemServicoResponse` e atualizar `OrdemServicoResponse` para expor timestamps do ciclo.
14. Atualizar `src/main/java/com/postech/workshop_service/api/controllers/openapi.yaml`.

## Validacao

1. Executar testes de dominio:

```powershell
mvn "-Dtest=OrdemServicoTest" test
```

2. Executar testes dos casos de uso da feature:

```powershell
mvn "-Dtest=IniciarExecucaoUseCaseTest,FinalizarExecucaoUseCaseTest,EntregarVeiculoUseCaseTest,ConsultarHistoricoOrdemServicoUseCaseTest" test
```

3. Executar testes de controller e fluxo completo:

```powershell
mvn "-Dtest=OrdemServicoControllerIT" test
```

4. Executar validacao final recomendada:

```powershell
mvn test
```

## Fluxo Manual Esperado

1. Criar uma OS com servico e orcamento pendente.
2. Aprovar o orcamento.
3. Iniciar execucao da OS.
4. Finalizar execucao tecnica.
5. Entregar veiculo.
6. Consultar historico da OS como `ADMINISTRADOR`, `MECANICO` ou `ATENDENTE`.
7. Confirmar que a OS apresenta status `ENTREGUE`, datas do ciclo preenchidas e historico com todas as transicoes registradas apos a implantacao da feature.

## Fora do Escopo

- Baixa, reserva ou liberacao de estoque no inicio da execucao.
- KPI de tempo medio.
- Reabertura de OS finalizada.
- Backfill retroativo de historico para ordens existentes antes desta feature.

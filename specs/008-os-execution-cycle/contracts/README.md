# Contracts

Esta feature expoe novas transicoes HTTP para o ciclo operacional da ordem de servico, adiciona consulta de historico por OS e atualiza o contrato de resposta da OS.

## Endpoints de Ordem de Servico

Todos os endpoints exigem usuario autenticado.

| Metodo | Caminho | Perfis | Resultado |
|--------|---------|--------|-----------|
| PATCH | `/api/v1/ordens-servico/{id}/iniciar-execucao` | `ADMINISTRADOR`, `MECANICO` | OS em `EM_EXECUCAO` |
| PATCH | `/api/v1/ordens-servico/{id}/finalizar` | `ADMINISTRADOR`, `MECANICO` | OS em `FINALIZADA` |
| PATCH | `/api/v1/ordens-servico/{id}/entregar` | `ADMINISTRADOR`, `ATENDENTE` | OS em `ENTREGUE` |
| GET | `/api/v1/ordens-servico/{id}/historico-status` | `ADMINISTRADOR`, `MECANICO`, `ATENDENTE` | Lista cronologica de historico |

## Resposta de Transicoes

Os tres endpoints de transicao retornam `OrdemServicoResponse`.

Campos novos esperados na resposta:

- `dataInicioExecucao`
- `dataFinalizacao`
- `dataEntrega`

## Resposta de Historico

A consulta de historico retorna uma lista de `HistoricoStatusOrdemServicoResponse`.

Campos esperados por item:

- `id`
- `idOrdemServico`
- `statusAnterior`
- `statusNovo`
- `dataTransicao`
- `idUsuario`
- `usernameUsuario`

Regra de ordenacao:

- A lista deve vir em ordem cronologica crescente por `dataTransicao`.

Regra para ordens antigas:

- Nao ha backfill retroativo. Ordens existentes antes desta feature podem retornar historico vazio ou parcial.

## Erros Esperados

| Condicao | Resultado esperado |
|----------|--------------------|
| OS inexistente | Recurso nao encontrado |
| Usuario autenticado sem perfil permitido | Acesso negado |
| Transicao fora da ordem permitida | Regra de negocio violada |
| Identificador da OS com formato invalido | Requisicao invalida |

## OpenAPI

Atualizar `src/main/java/com/postech/workshop_service/api/controllers/openapi.yaml` com:

- os quatro caminhos novos
- enum `StatusOrdemServico` incluindo `EM_EXECUCAO` e `ENTREGUE`
- novos campos de data em `OrdemServicoResponse`
- schema `HistoricoStatusOrdemServicoResponse`

## Contratos Internos

Casos de uso esperados:

- `IniciarExecucaoUseCase`
- `FinalizarExecucaoUseCase`
- `EntregarVeiculoUseCase`
- `ConsultarHistoricoOrdemServicoUseCase`

Contrato de persistencia novo:

- `HistoricoStatusOrdemServicoRepository`

Servico de aplicacao esperado:

- registrador de historico de status para transicoes bem-sucedidas

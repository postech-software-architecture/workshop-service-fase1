# Contract: OpenAPI e README

## Fonte de rotas

A implementacao deve revisar todas as controllers publicas em:

```text
src/main/java/com/postech/workshop_service/api/controllers
```

Controllers esperadas:

- `AuthController`
- `ClienteController`
- `VeiculoController`
- `ServicoController`
- `PecaInsumoController`
- `EstoqueController`
- `OrdemServicoController`
- `OrcamentoController`
- `MetricaController`

`GlobalExceptionHandler` deve ser usado para conferir respostas de erro e formatos comuns.

## Cobertura minima por operacao

Cada operacao documentada em `openapi.yaml` deve conter:

- metodo HTTP e caminho iguais aos expostos pela controller
- tag funcional consistente com o dominio
- resumo objetivo em pt-BR
- parametros de path e query, com obrigatoriedade e tipo
- corpo de requisicao, quando houver
- respostas de sucesso com schema ou descricao de ausencia de corpo
- respostas de erro previsiveis, incluindo validacao, autenticacao, autorizacao e regra de negocio quando aplicavel
- exemplos de request e response quando forem uteis para consumo externo

## Inventario de endpoints a conferir

```text
POST   /api/auth/login
POST   /api/auth/refresh
POST   /api/auth/logout
GET    /api/auth/me

POST   /api/v1/clientes
PUT    /api/v1/clientes/{id}
GET    /api/v1/clientes/{id}
GET    /api/v1/clientes/documento/{documento}
GET    /api/v1/clientes
GET    /api/v1/clientes/me
DELETE /api/v1/clientes/{id}

POST   /api/v1/veiculos
PUT    /api/v1/veiculos/{id}
POST   /api/v1/veiculos/{id}/clientes/{clienteId}
DELETE /api/v1/veiculos/{id}/clientes/{clienteId}
GET    /api/v1/veiculos/{id}
GET    /api/v1/veiculos/placa/{placa}
GET    /api/v1/veiculos
GET    /api/v1/veiculos/cliente/{clienteId}
DELETE /api/v1/veiculos/{id}

POST   /api/v1/servicos
GET    /api/v1/servicos
GET    /api/v1/servicos/{id}
GET    /api/v1/servicos/categoria/{categoria}
PUT    /api/v1/servicos/{id}
DELETE /api/v1/servicos/{id}
POST   /api/v1/servicos/{id}/reativar

POST   /api/v1/pecas
PUT    /api/v1/pecas/{id}
GET    /api/v1/pecas/{id}
GET    /api/v1/pecas/sku/{sku}
GET    /api/v1/pecas
DELETE /api/v1/pecas/{id}
POST   /api/v1/pecas/estoques

POST   /api/v1/estoques/movimentacoes
GET    /api/v1/estoques/{id}
GET    /api/v1/estoques/peca/{pecaInsumoId}
GET    /api/v1/estoques/movimentacoes/ordem-servico/{ordemServicoId}

POST   /api/v1/ordens-servico
GET    /api/v1/ordens-servico/{id}
GET    /api/v1/ordens-servico
GET    /api/v1/ordens-servico/minhas
GET    /api/v1/ordens-servico/{id}/status
PATCH  /api/v1/ordens-servico/{id}/iniciar-diagnostico
PATCH  /api/v1/ordens-servico/{id}/encerrar-diagnostico
POST   /api/v1/ordens-servico/{id}/itens
PATCH  /api/v1/ordens-servico/{id}/encerrar-composicao
PATCH  /api/v1/ordens-servico/{id}/iniciar-execucao
PATCH  /api/v1/ordens-servico/{id}/finalizar-execucao
PATCH  /api/v1/ordens-servico/{id}/itens/{idItem}/iniciar-servico
PATCH  /api/v1/ordens-servico/{id}/itens/{idItem}/finalizar-servico
PATCH  /api/v1/ordens-servico/{id}/entregar
GET    /api/v1/ordens-servico/{id}/historico-status

GET    /api/v1/orcamentos/ordem-servico/{idOrdemServico}
PATCH  /api/v1/orcamentos/{id}/aprovar
PATCH  /api/v1/orcamentos/{id}/rejeitar
PATCH  /api/v1/orcamentos/{id}/cancelar

GET    /api/v1/metricas/tempo-medio-execucao
GET    /api/v1/metricas/tempo-medio-execucao/por-tipo-servico
```

## README

O README deve:

- indicar a URL local do Swagger UI
- indicar a existencia do arquivo `openapi.yaml` como contrato fonte versionado, se aplicavel
- explicar pre-requisitos minimos para subir a aplicacao e acessar a documentacao
- resumir dominios funcionais cobertos pela API
- evitar duplicar o contrato completo de endpoints

## Validacao

Antes de concluir a implementacao, conferir:

- inventario acima contra controllers atuais
- `openapi.yaml` contra padrao de tags, schemas e respostas existentes
- README contra informacoes reais de execucao e documentacao
- `mvn test` ou subconjunto justificado quando a suite completa nao for viavel

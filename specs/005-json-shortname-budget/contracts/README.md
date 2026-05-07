# Contracts

O escopo desta feature passou a expor o fluxo de ordem de servico e orcamento via API REST,
alem dos casos de uso e contratos de repositorio internos.

Casos de uso e services internos:

- `CriarOrdemServicoUseCase`
- `EncerrarComposicaoTecnicaUseCase`
- `AprovarOrcamentoUseCase`
- `RejeitarOrcamentoUseCase`
- `CancelarOrcamentoUseCase`
- `ClienteNotificationService` com implementacao `LogClienteNotificationService`
- `MecanicoNotificationService` com implementacao `LogMecanicoNotificationService`
- `OrdemServicoRepository` e `OrcamentoRepository` com adaptadores JPA

Contornos HTTP introduzidos nesta fase:

- `OrdemServicoController` (POST `/ordens-servico`) consumindo `CriarOrdemServicoRequest` e
  retornando `OrdemServicoResponse`;
- `OrcamentoController` com endpoints de aprovacao, rejeicao e cancelamento, retornando
  `OrcamentoResponse`;
- DTOs em `api/dtos/` cobrem os contornos de entrada e saida.

A documentacao OpenAPI e gerada via springdoc a partir das anotacoes `@Schema` aplicadas aos
DTOs, sem necessidade de manter um `openapi.yaml` estatico nesta pasta.

## Premissas e fluxo de estoque do MVP

- **Momento da reserva**: a reserva de estoque acontece na criacao da OS
  (`CriarOrdemServicoUseCase`), simultaneamente ao envio do orcamento para aprovacao. Essa
  decisao acompanha o event storming da fase: a OS sai da composicao com saldo ja
  comprometido. Reserva apos aprovacao fica para incrementos futuros.
- **Liberacao**: em caso de rejeicao (`RejeitarOrcamentoUseCase`) ou cancelamento
  (`CancelarOrcamentoUseCase`) do orcamento, a movimentacao RESERVA correspondente e
  estornada via LIBERACAO 1:1.
- **Estoque unico**: o MVP assume um unico registro de `Estoque` ativo por peca/insumo. A
  reserva consome integralmente o saldo de UM estoque, e a validacao em `construirItensPeca`
  confere se ha estoque com saldo suficiente. Suporte a multiplas localizacoes/distribuicao
  fica para incremento posterior.
- **Rastreabilidade**: cada movimentacao RESERVA e LIBERACAO carrega no campo `motivo` o
  numero da OS (`Reserva para OS X`, `Liberacao de reserva — orcamento rejeitado/cancelado
  OS X`), permitindo correlacionar reservas e liberacoes 1:1.
- **Notificacoes resilientes**: falhas no envio de notificacoes para cliente ou mecanico
  (`ClienteNotificationService` / `MecanicoNotificationService`) sao capturadas e logadas em
  nivel WARN nos casos de uso, sem reverter a transacao da decisao do orcamento.

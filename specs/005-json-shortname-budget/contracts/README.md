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

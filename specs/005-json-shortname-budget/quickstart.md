# Quickstart: Encerramento de Composicao Tecnica e Fluxo de Orcamento

## Objetivo

Implementar os casos de uso de encerramento da composicao tecnica da ordem e de decisao do orcamento, preservando a arquitetura atual em camadas e adicionando a persistencia faltante de ordem/orcamento.

## Passos de Implementacao

1. Ajustar `StatusOrdemServico` para refletir o novo fluxo do MVP.
2. Evoluir `OrdemServico` em `src/main/java/com/postech/workshop_service/domain/entities/` para manter itens de composicao tecnica e os metodos `encerrarComposicao()`, `voltarParaComposicao()`, `marcarComoAguardandoExecucao()` e `cancelar()`.
3. Criar o tipo `TipoItemComposicaoTecnica` e o objeto `ItemComposicaoTecnica` no dominio.
4. Ajustar `Orcamento` para manter a fotografia dos itens e limitar `aprovar()`, `rejeitar()` e `cancelar()` a partir de `PENDENTE_APROVACAO`.
5. Criar `OrdemServicoRepository` e `OrcamentoRepository` em `src/main/java/com/postech/workshop_service/domain/repositories/`.
6. Criar `ClienteNotificationService` e `MecanicoNotificationService` como portas de aplicacao e implementacoes concretas baseadas em log.
7. Implementar `EncerrarComposicaoTecnicaUseCase`:
   - buscar a ordem
   - validar itens
   - validar ausencia de orcamento pendente
   - criar orcamento
   - copiar itens
   - enviar para aprovacao
   - encerrar composicao da ordem
   - persistir ambos
   - notificar cliente
8. Implementar `AprovarOrcamentoUseCase`, `RejeitarOrcamentoUseCase` e `CancelarOrcamentoUseCase`, sempre buscando o orcamento, a ordem vinculada, validando estados, persistindo ambos os agregados e notificando mecanico.
9. Criar as migrations Flyway para tabelas de ordem, itens da ordem, orcamento e itens do orcamento com UUID, auditoria e comentarios.
10. Criar entidades JPA, repositorios Spring Data, mapeadores MapStruct e implementacoes de repositrio para os dois agregados.
11. Atualizar `OrdemServicoTest` e `OrcamentoTest` para o novo comportamento.
12. Criar testes unitarios dos quatro casos de uso cobrindo:
   - encerramento com item
   - bloqueio sem item
   - geracao de orcamento pendente
   - copia de itens
   - bloqueio de segundo orcamento pendente
   - aprovacao valida e bloqueios
   - rejeicao valida e retorno da ordem para composicao
   - cancelamento valido e cancelamento da ordem
   - notificacao de cliente e mecanico

## Validacao

1. Executar `mvn test`.
2. Confirmar que as entidades de dominio continuam sem setters genericos para status.
3. Confirmar que existe validacao dupla para orcamento pendente unico: caso de uso/repositorio e banco.
4. Confirmar que os services de notificacao apenas registram logs e nao disparam integracao externa.
5. Confirmar que nao foram adicionados endpoints, PDF, pagamento, aprovacao parcial ou estoque nesta etapa.

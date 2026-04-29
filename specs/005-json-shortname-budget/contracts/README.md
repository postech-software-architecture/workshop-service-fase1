# Contracts

Esta fase nao adiciona contrato HTTP novo.

O escopo desta feature e interno a aplicacao e compreende:

- casos de uso transacionais para encerrar composicao tecnica e decidir orcamento;
- contratos de repositorio de dominio para ordem de servico e orcamento;
- services de notificacao por log para cliente e mecanico.

Como nao ha controller nem endpoint solicitados nesta etapa:

- nenhum `openapi.yaml` novo sera criado agora;
- nenhuma alteracao de request/response publica e obrigatoria nesta fase;
- a exposicao externa do fluxo pode ser planejada em um incremento posterior, reaproveitando os casos de uso definidos aqui.

# Research: Atualizacao da Documentacao OpenAPI e README

## Decision: Usar controllers como fonte primaria do inventario de endpoints

**Rationale**: A especificacao exige que o contrato reflita o comportamento exposto hoje. As controllers em `src/main/java/com/postech/workshop_service/api/controllers` declaram os caminhos base, metodos HTTP, parametros, corpos de requisicao e restricoes de autorizacao mais proximos do contrato publico.

**Alternatives considered**:

- Usar apenas o `openapi.yaml` atual: rejeitado porque a feature existe justamente para identificar omissoes e divergencias.
- Usar apenas testes de integracao: rejeitado porque testes podem cobrir cenarios sem representar todo o contrato publico.

## Decision: Preservar o arquivo OpenAPI manual no caminho atual

**Rationale**: O usuario indicou explicitamente o arquivo `src/main/java/com/postech/workshop_service/api/controllers/openapi.yaml` e pediu manutencao do padrao existente. Mover ou substituir a fonte de documentacao ampliaria o escopo.

**Alternatives considered**:

- Gerar OpenAPI exclusivamente por anotacoes nas controllers: rejeitado por alterar a estrategia de documentacao e exigir mudancas de codigo alem do pedido.
- Criar novo arquivo em `docs/`: rejeitado para evitar duas fontes de verdade.

## Decision: Documentar erros a partir do handler global e dos testes de integracao

**Rationale**: A constituicao define padroes HTTP relevantes, especialmente 422 para validacao estrutural e 400 para regra de negocio. `GlobalExceptionHandler` e os testes de controllers ajudam a identificar os formatos e codigos efetivamente expostos.

**Alternatives considered**:

- Documentar respostas genericas para todos os endpoints: rejeitado porque reduziria utilidade para integradores.
- Documentar apenas respostas de sucesso: rejeitado porque a especificacao exige erros previsiveis.

## Decision: Complementar README com orientacao, nao com contrato completo

**Rationale**: O README deve permitir descoberta rapida da API, mas o contrato detalhado deve permanecer no OpenAPI. Duplicar rotas e schemas no README aumenta risco de divergencia.

**Alternatives considered**:

- Copiar lista completa de endpoints para README: rejeitado por duplicacao e manutencao dificil.
- Remover mencoes ao Swagger/OpenAPI do README: rejeitado porque prejudica onboarding.

## Decision: Validacao da documentacao por comparacao e testes existentes

**Rationale**: A feature nao altera comportamento. A verificacao deve confirmar que o contrato YAML cobre as controllers e que o projeto continua compilando/testando. Testes de integracao existentes por controller sao suficientes como base de regressao para a documentacao planejada.

**Alternatives considered**:

- Criar testes automatizados especificos para parse do YAML: considerar apenas se a execucao de tarefas identificar risco alto de YAML invalido ou divergencia recorrente.
- Executar somente revisao manual: rejeitado porque nao confirma regressao basica do projeto.

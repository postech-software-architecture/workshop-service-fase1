# Research: Gestao de Veiculos de Clientes

## Decision 1: Expor a funcionalidade em `/api/v1/veiculos` com busca por ID, placa, cliente e listagem filtrada

- **Decision**: Manter a feature sob o prefixo versionado `/api/v1/veiculos`, com `POST /veiculos`, `GET /veiculos/{id}`, `GET /veiculos/placa/{placa}`, `GET /veiculos/cliente/{clienteId}`, `GET /veiculos` para listagem paginada, `PUT /veiculos/{id}` e `DELETE /veiculos/{id}`.
- **Rationale**: O projeto ja usa `/api/v1` em clientes. Centralizar a API em torno do agregado `Veiculo` atende aos requisitos de consulta e simplifica a documentacao e o consumo interno.
- **Alternatives considered**:
  - Usar rotas sem versao: rejeitado por divergir do padrao existente.
  - Aninhar toda a API em `/clientes/{clienteId}/veiculos`: rejeitado porque o veiculo pode pertencer a varios clientes e tambem precisa ser consultado por placa e ID.

## Decision 2: Modelar `Veiculo` com relacionamento muitos-para-muitos obrigatorio com clientes

- **Decision**: Representar os vinculos entre veiculos e clientes por uma tabela associativa `veiculos_clientes`, exigindo pelo menos um `clienteId` valido no cadastro e em qualquer atualizacao de vinculos.
- **Rationale**: A especificacao confirma que o mesmo veiculo pode estar ligado a varios clientes e que nao pode ficar sem clientes vinculados. A tabela associativa preserva flexibilidade, rastreabilidade e aderencia ao dominio sem inventar cliente principal.
- **Alternatives considered**:
  - Campo unico `cliente_id` em `veiculos`: rejeitado por contrariar a clarificacao de varios clientes por veiculo.
  - Entidade de vinculacao com hierarquia de proprietario principal: rejeitada porque o negocio afirma que todos os clientes sao equivalentes.

## Decision 3: Aplicar remocao logica no agregado `Veiculo`, preservando historico e permitindo reutilizacao futura da placa

- **Decision**: Implementar soft delete com `ativo` e `data_remocao` em `veiculos`, mantendo os vinculos historicos e permitindo reutilizacao da mesma placa apenas quando o registro anterior estiver inativo.
- **Rationale**: O requisito exige retirada das consultas operacionais sem perda de historico. A combinacao de flag operacional e timestamp de remocao simplifica filtros, auditoria e regras de reutilizacao.
- **Alternatives considered**:
  - Exclusao fisica: rejeitada por comprometer rastreabilidade.
  - Arquivamento em tabela separada: rejeitado por adicionar complexidade desnecessaria para o problema atual.

## Decision 4: Normalizar placa em value object e persistir valor canonico para busca e unicidade

- **Decision**: Criar um value object `Placa` que sanitize entrada, remova separadores, converta para caixa alta e valide os formatos antigo (`ABC1234`) e Mercosul (`ABC1D23`), persistindo o valor normalizado para comparacao.
- **Rationale**: A placa e a chave de negocio principal e precisa sustentar busca consistente, comparacao sem ambiguidade e feedback de erro adequado.
- **Alternatives considered**:
  - Validar apenas no DTO: rejeitado por nao proteger invariantes internas.
  - Confiar somente em `UNIQUE` do banco: rejeitado por nao tratar normalizacao nem mensagens de negocio.

## Decision 5: Adotar faixa de ano de 1900 ate o ano corrente e validar identificadores opcionais de forma defensiva

- **Decision**: Considerar valido o ano do veiculo entre `1900` e o ano corrente do processamento; validar `quilometragemAtual >= 0`, `chassi` com 17 caracteres alfanumericos sem I/O/Q e `renavam` com 11 digitos apos sanitizacao.
- **Rationale**: A spec exige bloqueio de anos futuros e fora de faixa historicamente plausivel. O limite inferior de 1900 cobre a frota automotiva relevante sem aceitar valores absurdos, e as demais validacoes evitam sujeira operacional em campos identificadores.
- **Alternatives considered**:
  - Permitir ano seguinte para modelos novos: rejeitado porque o texto explicita rejeicao de ano futuro.
  - Deixar chassi e renavam sem validacao de formato: rejeitado por aumentar inconsistencias.

## Decision 6: Reutilizar a arquitetura DDD existente com validacao explicita de todos os clientes vinculados

- **Decision**: Implementar controller -> use case -> dominio -> repositorio, consultando o repositorio de clientes para garantir que todos os `clienteIds` existam antes de criar, atualizar ou consultar por cliente.
- **Rationale**: A feature depende do cadastro de clientes e precisa devolver erros de negocio controlados quando houver IDs inexistentes. O padrao ja esta estabelecido no modulo de clientes.
- **Alternatives considered**:
  - Validar apenas por chave estrangeira em banco: rejeitado por nao produzir erros funcionais claros.
  - Acessar JPA direto do controller: rejeitado por violar a arquitetura definida na constituicao.

## Decision 7: Manter os endpoints liberados no MVP, preparados para futura restricao de acesso

- **Decision**: Implementar a feature com `SecurityConfig` permissivo no MVP, preservando o contrato e a estrutura necessaria para futura restricao de operacoes administrativas.
- **Rationale**: A iteracao atual prioriza a entrega funcional do cadastro e das consultas de veiculos. A preparacao na configuracao evita retrabalho estrutural quando a autenticacao for introduzida.
- **Alternatives considered**:
  - Bloquear os endpoints ja nesta iteracao: rejeitado por estar fora do escopo atual do MVP.
  - Remover qualquer preparacao de seguranca: rejeitado por gerar retrabalho desnecessario na evolucao da feature.

## Decision 8: Cobrir a feature com testes unitarios e integracao usando PostgreSQL real via Testcontainers

- **Decision**: Criar testes unitarios para entidade `Veiculo`, value objects e casos de uso; complementar com testes de integracao para controller e repositorio em PostgreSQL/Testcontainers.
- **Rationale**: A feature combina regras de dominio, filtros de consulta, relacionamento muitos-para-muitos e constraints de persistencia. A combinacao de testes curtos e testes de ponta a ponta reduz risco de regressao.
- **Alternatives considered**:
  - Apenas testes de controller com mocks: rejeitado por nao validar persistencia real.
  - Apenas testes de integracao: rejeitado por reduzir feedback rapido sobre regras de dominio.

# Feature Specification: Gestao de Veiculos de Clientes

**Feature Branch**: `002-vehicle-management`  
**Created**: 2026-04-26  
**Status**: Draft  
**Input**: User description: "Implementar gestao completa de veiculos vinculados aos clientes da oficina, permitindo rastreamento do historico de manutencoes."

## Clarifications

### Session 2026-04-26

- Q: A placa pode ser reutilizada apos a remocao logica do veiculo anterior? → A: Sim, a placa pode ser reutilizada apos remocao logica do veiculo anterior.
- Q: O mesmo veiculo pode estar ligado a mais de um cliente? → A: Sim, o mesmo veiculo pode estar vinculado a multiplos clientes.
- Q: O veiculo pode ficar sem clientes vinculados? → A: Nao, o veiculo deve sempre ter pelo menos um cliente vinculado.
- Q: Deve existir um cliente principal entre os vinculados ao veiculo? → A: Nao, nao existe cliente principal; todos os clientes vinculados sao equivalentes.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Cadastrar e manter veiculo do cliente (Priority: P1)

Como colaborador da oficina, quero cadastrar e atualizar os dados de um veiculo vinculado a um ou mais clientes existentes para manter os responsaveis associados e a identificacao do veiculo corretos antes de qualquer atendimento.

**Why this priority**: Sem cadastro confiavel do veiculo nao existe base para consultas, historico de manutencao nem abertura segura de atendimentos futuros.

**Independent Test**: Pode ser testada de forma independente ao cadastrar um veiculo com dados obrigatorios validos, vinculando-o a um ou mais clientes existentes, e depois atualizando seus dados opcionais sem perder as associacoes.

**Acceptance Scenarios**:

1. **Given** um ou mais clientes existentes e dados obrigatorios validos do veiculo, **When** o colaborador cadastra o veiculo, **Then** o sistema registra o veiculo com identificacao unica e o vincula a todos os clientes informados.
2. **Given** um veiculo ja cadastrado, **When** o colaborador atualiza campos permitidos como cor, quilometragem, observacoes ou vinculacoes de clientes, **Then** o sistema salva as alteracoes e preserva a identidade do veiculo e suas associacoes validas.
3. **Given** uma tentativa de cadastro ou atualizacao com placa invalida, ano fora da faixa permitida ou algum cliente inexistente, **When** o colaborador envia os dados, **Then** o sistema rejeita a operacao com mensagem clara de validacao.

---

### User Story 2 - Consultar veiculos com rapidez (Priority: P2)

Como colaborador da oficina, quero localizar veiculos por proprietario, placa ou identificador para recuperar rapidamente os dados corretos durante atendimento, triagem ou acompanhamento.

**Why this priority**: A localizacao rapida reduz erros operacionais e acelera atividades do dia a dia, mas depende da existencia de cadastros confiaveis.

**Independent Test**: Pode ser testada criando veiculos para clientes diferentes e confirmando que o usuario consegue localizar um unico veiculo por placa ou identificador e listar todos os veiculos de um cliente.

**Acceptance Scenarios**:

1. **Given** varios veiculos cadastrados, **When** o colaborador consulta por placa valida e existente, **Then** o sistema retorna o veiculo correspondente sem ambiguidade.
2. **Given** um cliente com multiplos veiculos ativos, **When** o colaborador consulta os veiculos desse cliente, **Then** o sistema apresenta a lista completa dos veiculos vinculados a esse cliente, mesmo quando compartilhados com outros clientes.
3. **Given** um veiculo vinculado a varios clientes, **When** o colaborador consulta seus detalhes, **Then** o sistema apresenta todos os clientes vinculados sem destacar cliente principal.
3. **Given** um conjunto grande de cadastros, **When** o colaborador navega pela listagem geral com filtros, **Then** o sistema entrega resultados paginados e consistentes.

---

### User Story 3 - Remover veiculo sem perder historico (Priority: P3)

Como gestor da oficina, quero desativar um veiculo que nao deve mais aparecer como ativo para preservar o historico de manutencoes e manter rastreabilidade dos relacionamentos anteriores com os clientes.

**Why this priority**: A preservacao do historico protege a memoria operacional da oficina e evita perda de contexto sobre servicos realizados.

**Independent Test**: Pode ser testada removendo logicamente um veiculo ja cadastrado, verificando que ele deixa de aparecer nas consultas ativas e continua elegivel para consulta historica e associacoes existentes.

**Acceptance Scenarios**:

1. **Given** um veiculo com historico associado, **When** o gestor solicita sua remocao, **Then** o sistema marca o veiculo como inativo sem apagar os registros historicos relacionados.
2. **Given** um veiculo removido logicamente, **When** o colaborador consulta apenas registros ativos, **Then** o veiculo nao aparece na listagem operacional padrao.
3. **Given** uma tentativa de remover novamente um veiculo ja inativo, **When** a solicitacao e enviada, **Then** o sistema responde de forma consistente sem recriar nem apagar historico.

### Edge Cases

- Tentativa de cadastrar um veiculo com placa ja utilizada por outro veiculo ativo deve ser rejeitada para evitar duplicidade operacional.
- Tentativa de reutilizar a placa de um veiculo inativo deve ser permitida somente apos a remocao logica do registro anterior, preservando a rastreabilidade entre o cadastro historico e o novo cadastro ativo.
- Anos muito antigos, anos futuros ou combinacoes incoerentes entre ano de fabricacao e modelo devem ser rejeitados.
- Consultas por placa devem tratar diferencas de formatacao, caixa alta ou caracteres separadores sem gerar resultados divergentes.
- Veiculos sem campos opcionais preenchidos devem continuar validos para cadastro e consulta.
- Operacoes de atualizacao nao podem remover o ultimo cliente vinculado de um veiculo ativo ou inativo.
- Operacoes de consulta e alteracao devem tratar com erro claro identificadores inexistentes ou listas contendo clientes inexistentes.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: O sistema MUST permitir cadastrar um veiculo vinculando-o obrigatoriamente a pelo menos um cliente existente.
- **FR-002**: O sistema MUST exigir no cadastro do veiculo os campos placa, marca, modelo, ano e ao menos um identificador de cliente vinculado.
- **FR-003**: O sistema MUST aceitar opcionalmente cor, chassi, renavam, quilometragem atual, tipo de combustivel e observacoes.
- **FR-004**: O sistema MUST validar a placa informada aceitando apenas os formatos de placa antiga e Mercosul definidos pelo negocio.
- **FR-005**: O sistema MUST impedir a existencia de placa duplicada entre veiculos ativos e permitir reutilizacao da placa somente quando o registro anterior estiver removido logicamente.
- **FR-006**: O sistema MUST validar o ano do veiculo para rejeitar valores futuros ou fora de uma faixa historicamente plausivel para a operacao da oficina.
- **FR-007**: O sistema MUST verificar a existencia de todos os clientes vinculados antes de concluir cadastro, atualizacao ou consulta por cliente.
- **FR-008**: O sistema MUST permitir consultar veiculos por identificador unico, por placa e por cliente vinculado.
- **FR-009**: O sistema MUST oferecer listagem paginada de veiculos com filtros suficientes para localizar registros sem percorrer toda a base manualmente.
- **FR-010**: O sistema MUST permitir atualizar os dados cadastrais do veiculo e sua lista de clientes vinculados, mantendo a identidade do veiculo no sistema.
- **FR-010A**: O sistema MUST impedir que um veiculo permaneça sem nenhum cliente vinculado em qualquer operacao de cadastro, atualizacao ou manutencao de vinculos.
- **FR-010B**: O sistema MUST tratar todos os clientes vinculados ao veiculo como equivalentes, sem exigir ou derivar um cliente principal.
- **FR-011**: O sistema MUST realizar remocao logica do veiculo, impedindo exclusao fisica quando isso comprometer historico ou rastreabilidade.
- **FR-012**: O sistema MUST excluir veiculos removidos logicamente das consultas operacionais padrao, salvo quando a consulta solicitar contexto historico.
- **FR-013**: O sistema MUST preservar o historico de manutencoes e demais referencias existentes quando um veiculo for removido logicamente.
- **FR-014**: O sistema MUST registrar mensagens de erro claras para dados invalidos, duplicados, inexistentes ou inacessiveis.
- **FR-015**: O sistema MUST sanitizar os dados de entrada textuais antes de persisti-los ou exibi-los para reduzir risco de conteudo malicioso.
- **FR-016**: O sistema MUST manter a funcionalidade preparada para futura restricao de operacoes administrativas de cadastro, alteracao e remocao conforme a politica da oficina, sem exigir autenticacao/autorizacao nesta iteracao MVP.
- **FR-017**: O sistema MUST disponibilizar descricao atualizada das operacoes e campos da funcionalidade para consulta pelos consumidores internos do servico.

### Key Entities *(include if feature involves data)*

- **Veiculo**: Automovel atendido pela oficina, identificado por placa unica no contexto operacional, com dados de marca, modelo, ano e atributos opcionais de identificacao e uso, podendo estar vinculado a um ou mais clientes.
- **Cliente**: Pessoa ou organizacao vinculada ao veiculo para fins de atendimento, podendo compartilhar o mesmo veiculo com outros clientes e sem hierarquia de principalidade nessa vinculacao.
- **Historico de Manutencao**: Conjunto de registros de servicos executados para um veiculo ao longo do tempo, que deve permanecer acessivel mesmo apos a inativacao do cadastro.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% dos cadastros de veiculos concluidos com sucesso apresentam placa valida, pelo menos um cliente existente vinculado e campos obrigatorios preenchidos.
- **SC-002**: 100% das tentativas com placa duplicada, placa invalida, ano fora da faixa ou qualquer cliente inexistente sao bloqueadas com retorno explicativo ao usuario.
- **SC-003**: Colaboradores conseguem localizar um veiculo por placa, identificador ou proprietario em ate 3 interacoes na interface consumidora do servico.
- **SC-004**: 100% das remocoes de veiculos preservam o historico de manutencoes e retiram o cadastro das consultas operacionais padrao.
- **SC-005**: Pelo menos 95% das consultas retornam os registros esperados sem necessidade de correcoes manuais causadas por inconsistencias cadastrais.

## Assumptions

- A oficina ja possui cadastro de clientes disponivel e confiavel para validacao de todos os clientes vinculados ao veiculo.
- A placa e tratada como principal chave de negocio do veiculo no contexto operacional da oficina.
- A faixa historicamente plausivel para o ano do veiculo sera definida de modo a aceitar a frota normalmente atendida pela oficina sem permitir valores claramente inconsistentes.
- O historico de manutencao sera mantido em registros relacionados ja existentes ou planejados e nao faz parte do escopo detalhar sua estrutura nesta feature.
- A restricao efetiva por autenticacao e autorizacao das operacoes administrativas sera tratada em iteracao posterior ao MVP, sem impedir a exposicao atual dos endpoints planejados.

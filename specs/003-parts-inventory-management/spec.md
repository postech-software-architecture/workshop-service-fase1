# Feature Specification: Gestao de Pecas, Insumos e Estoques

**Feature Branch**: `003-parts-inventory-management`  
**Created**: 2026-04-29  
**Status**: Draft  
**Input**: User description: "Gestao de pecas e insumos utilizados nos servicos da oficina, incluindo cadastro, controle de estoque por localizacao e movimentacoes de entrada, saida e ajuste."

## Clarifications

### Session 2026-04-29

- Q: O SKU pode ser reutilizado apos a remocao logica da peca anterior? → A: Sim, o SKU pode ser reutilizado apos remocao logica da peca anterior.
- Q: Uma peca pode ter quantidade negativa em situacoes de reserva antecipada? → A: Nao, o estoque nunca pode ficar negativo; reservas devem verificar disponibilidade previa.
- Q: A exclusao de uma peca com movimentacoes historicas deve ser permitida? → A: Nao realizar exclusao fisica; pecas devem ser desativadas por remocao logica.
- Q: O sistema deve suportar multiplas localizacoes de estoque (ex: armarios diferentes)? → A: Sim, cada localizacao fisica deve ser representada por um registro em Estoques.
- Q: CategoriaPeca e uma entidade separada com CRUD ou campo de classificacao? → A: Campo de texto livre, o usuario digita qualquer categoria.
- Q: Qual estrategia para tratar movimentacoes simultaneas sobre a mesma peca? → A: Otimistic locking com version (detecta conflito e rejeita operacao concorrente).
- Q: Como registrar o responsavel pela movimentacao sem autenticacao no MVP? → A: Nao registrar responsavel no MVP; adicionar quando autenticacao existir.
- Q: O alerta de validade e necessario? → A: Nao, remover alerta de validade e campo data de validade do escopo.
- Q: Ajuste de estoque e delta (+/-) ou valor absoluto? → A: Ajuste como valor absoluto, substituindo o estoque atual.
- Q: Estrutura de dados para estoque - quantidade na peca ou entidade separada? → A: Criar entidade Estoques: PecaInsumo (1) → (N) Estoques → (N) Movimentacoes.
- Q: Atributos da entidade Estoques? → A: peca_insumo_id (FK), localizacao (texto), quantidade (decimal), ativo (boolean), campos de auditoria.
- Q: Como diferenciar peca de insumo no cadastro? → A: Campo obrigatorio `tipoItem` com valores `PECA` e `INSUMO`.
- Q: Nome de tabelas no banco? → A: Plural, seguindo padrao existente (clientes, veiculos, etc).

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Cadastrar e manter pecas e insumos (Priority: P1)

Como colaborador da oficina, quero cadastrar pecas e insumos com seus dados obrigatorios e opcionais para manter o catalogo de materiais disponiveis para uso nos servicos.

**Why this priority**: Sem um catalogo confiavel de pecas, nao existe base para controle de estoque, movimentacoes ou integracao com ordens de servico.

**Independent Test**: Pode ser testada de forma independente ao cadastrar uma peca com SKU unico, nome, valor unitario, estoque minimo e unidade de medida, verificando que os dados sao persistidos corretamente.

**Acceptance Scenarios**:

1. **Given** dados obrigatorios validos (SKU, nome, valor unitario, estoque minimo, unidade), **When** o colaborador cadastra a peca, **Then** o sistema registra a peca com identificador unico e todos os campos informados.
2. **Given** uma peca ja cadastrada, **When** o colaborador atualiza campos permitidos como valor ou observacoes, **Then** o sistema salva as alteracoes.
3. **Given** uma tentativa de cadastro com SKU duplicado ativo, **When** o colaborador envia os dados, **Then** o sistema rejeita a operacao com mensagem clara de validacao.
4. **Given** uma tentativa de cadastro com quantidade ou valor negativo, **When** o colaborador envia os dados, **Then** o sistema rejeita a operacao com erro de validacao.

---

### User Story 2 - Controlar movimentacoes de estoque (Priority: P1)

Como colaborador da oficina, quero registrar entradas, saidas e ajustes de estoque para manter o controle preciso da quantidade disponivel de cada peca.

**Why this priority**: O controle de movimentacoes e o coracao do sistema de estoque, permitindo manter o saldo operacional correto.

**Independent Test**: Pode ser testada realizando uma entrada de estoque, uma saida e um ajuste, verificando que as quantidades sao atualizadas corretamente.

**Acceptance Scenarios**:

1. **Given** uma peca existente com estoque atual, **When** o colaborador registra uma entrada com quantidade positiva e justificativa, **Then** o sistema incrementa o estoque e registra a movimentacao.
2. **Given** uma peca com estoque suficiente, **When** o colaborador registra uma saida com quantidade e motivo, **Then** o sistema decrementa o estoque e registra a movimentacao.
3. **Given** uma peca com estoque insuficiente, **When** o colaborador tenta registrar uma saida maior que o disponivel, **Then** o sistema rejeita a operacao com erro de estoque insuficiente.
4. **Given** uma peca existente, **When** o colaborador registra um ajuste com justificativa obrigatoria, **Then** o sistema atualiza o estoque para o valor informado e registra a movimentacao como ajuste.

---

### User Story 3 - Consultar pecas e verificar disponibilidade (Priority: P2)

Como colaborador da oficina, quero consultar pecas por SKU, nome ou categoria e verificar sua disponibilidade em estoque para tomar decisoes rapidas durante atendimentos.

**Why this priority**: A consulta rapida apoia operacoes do dia a dia, mas depende da existencia de cadastros e movimentacoes confiaveis.

**Independent Test**: Pode ser testada cadastrando varias pecas e verificando que a busca por SKU retorna a peca correta, a listagem paginada funciona e a verificacao de disponibilidade retorna o status correto.

**Acceptance Scenarios**:

1. **Given** varias pecas cadastradas, **When** o colaborador consulta por SKU valido e existente, **Then** o sistema retorna a peca correspondente com dados completos.
2. **Given** um conjunto grande de pecas, **When** o colaborador navega pela listagem com filtros de categoria ou nome, **Then** o sistema entrega resultados paginados e consistentes.
3. **Given** uma peca com estoques cadastrados em uma ou mais localizacoes, **When** o colaborador verifica sua disponibilidade, **Then** o sistema retorna a quantidade total atual.

---

### User Story 4 - Remover peca do catalogo (Priority: P3)

Como gestor da oficina, quero desativar pecas descontinuadas para que nao aparecam mais nas consultas operacionais.

**Why this priority**: A remocao e necessaria para manter o catalogo limpo, mas e menos critica que as operacoes de controle de estoque.

**Independent Test**: Pode ser testada removendo uma peca e verificando que a peca desaparece das consultas ativas.

**Acceptance Scenarios**:

1. **Given** uma peca ativa, **When** o gestor solicita sua remocao, **Then** o sistema remove logicamente a peca do catalogo ativo.
2. **Given** uma peca removida logicamente, **When** o colaborador consulta apenas pecas ativas, **Then** a peca nao aparece na listagem.

### Edge Cases

- Tentativa de cadastrar peca com SKU ja utilizado por outra peca ativa deve ser rejeitada.
- Tentativa de reutilizar o SKU de uma peca inativa deve ser permitida.
- Quantidade em estoque nunca pode ser negativa, mesmo com saidas simultaneas.
- Valor unitario deve ser sempre positivo e maior que zero.
- Ajustes manuais de estoque exigem justificativa obrigatoria.
- Consultas por SKU devem tratar diferencas de caixa alta/baixa sem gerar resultados divergentes.
- Movimentacoes simultaneas sobre a mesma peca devem ser tratadas com optimistic locking (version), detectando conflito e rejeitando operacao concorrente.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: O sistema MUST permitir cadastrar uma peca/insumo com campos obrigatorios: SKU, nome, valor unitario, estoque minimo, unidade de medida e `tipoItem`.
- **FR-002**: O sistema MUST exigir que `tipoItem` assuma apenas os valores `PECA` ou `INSUMO`.
- **FR-003**: O sistema MUST aceitar campos opcionais: fornecedor, codigo de barras, marca, categoria (texto livre), aplicacao e observacoes.
- **FR-004**: O sistema MUST validar o SKU como identificador unico entre pecas ativas, permitindo reutilizacao apenas apos remocao logica.
- **FR-005**: O sistema MUST impedir quantidade em estoque negativa em qualquer operacao.
- **FR-006**: O sistema MUST exigir valor unitario positivo e maior que zero.
- **FR-007**: O sistema MUST permitir consultar pecas por identificador unico, por SKU e por filtros de nome/categoria.
- **FR-008**: O sistema MUST oferecer listagem paginada de pecas sem percorrer toda a base.
- **FR-009**: O sistema MUST permitir atualizar dados cadastrais da peca, incluindo valor, `tipoItem` e campos opcionais.
- **FR-010**: O sistema MUST realizar remocao logica da peca.
- **FR-011**: O sistema MUST excluir pecas removidas logicamente das consultas operacionais padrao, salvo quando solicitado incluir inativos.
- **FR-012**: O sistema MUST registrar movimentacoes de estoque do tipo entrada (incrementa), saida (decrementa) e ajuste (valor absoluto substitui estoque atual).
- **FR-013**: O sistema MUST exigir justificativa obrigatoria para ajustes manuais de estoque.
- **FR-014**: O sistema MUST verificar estoque disponivel antes de permitir uma saida.
- **FR-015**: O sistema MUST registrar movimentacoes com: data/hora, tipo, quantidade e motivo/justificativa.
- **FR-016**: O sistema MUST permitir criar multiplos estoques por peca, um para cada localizacao fisica.
- **FR-017**: O sistema MUST exigir localizacao obrigatoria na criacao do estoque.
- **FR-018**: O sistema MUST validar a unidade de medida entre os valores permitidos: UN, L, KG, M, ML, CX, PC.
- **FR-019**: O sistema MUST implementar optimistic locking com campo de versao para tratar concurrencia em movimentacoes de estoque.
- **FR-020**: O sistema MUST sanitizar dados textuais de entrada antes de persistir ou exibir.
- **FR-021**: O sistema MUST registrar mensagens de erro claras para dados invalidos, duplicados ou inexistentes.
- **FR-022**: O sistema MUST disponibilizar documentacao atualizada das operacoes via OpenAPI/Swagger.

### Key Entities *(include if feature involves data)*

- **PecaInsumo**: Item utilizado nos servicos da oficina, identificado por SKU unico, com `tipoItem` (`PECA` ou `INSUMO`), dados de nome, valor unitario, estoque minimo, unidade de medida e campos opcionais de cadastro. Controle de concurrencia via optimistic locking. Nao armazena quantidade diretamente (delegado a Estoques).
- **Estoque**: Registro de quantidade de uma peca em uma localizacao especifica. Uma mesma peca pode ter varios estoques.
- **MovimentacaoEstoque**: Registro de alteracao na quantidade de um estoque especifico, contendo tipo (entrada/saida/ajuste), quantidade, motivo/justificativa e data/hora.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% dos cadastros de pecas concluidos com sucesso apresentam SKU valido, valor positivo e unidade de medida valida.
- **SC-002**: 100% das tentativas com SKU duplicado ativo, quantidade negativa ou valor invalido sao bloqueadas com retorno explicativo.
- **SC-003**: Colaboradores conseguem localizar uma peca por SKU ou identificador em ate 3 interacoes na interface consumidora do servico.
- **SC-004**: 100% das movimentacoes de estoque sao registradas no historico com data, tipo, quantidade e motivo.
- **SC-004A**: 100% das operacoes concorrentes sobre a mesma peca sao detectadas e rejeitadas com erro de conflito.
- **SC-005**: 100% das tentativas de saida com quantidade maior que o estoque disponivel sao bloqueadas.
- **SC-006**: 100% das remocoes logicas retiram o item das consultas ativas.
- **SC-007**: Testes unitarios e de integracao com cobertura minima de 80%.

## Assumptions

- A oficina ja possui cadastro de clientes e veiculos disponivel para futura integracao com ordens de servico.
- O SKU e tratado como principal chave de negocio da peca no contexto operacional da oficina.
- A unidade de medida sera validada contra uma lista predefinida de valores permitidos.
- A integracao com ordens de servico (baixa automatica, reserva de pecas) sera tratada em feature futura.
- A autenticacao e autorizacao das operacoes administrativas sera tratada em iteracao posterior ao MVP.
- Alertas de estoque baixo e consulta historica via API ficam fora do escopo atual.

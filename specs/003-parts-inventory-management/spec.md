# Feature Specification: Gestao de Pecas, Insumos e Estoques

**Feature Branch**: `003-parts-inventory-management`  
**Created**: 2026-04-29  
**Status**: Draft  
**Input**: User description: "Gestao completa de pecas e insumos utilizados nos servicos da oficina, incluindo controle rigoroso de estoque, alertas de reposicao e rastreabilidade de movimentacoes."

## Clarifications

### Session 2026-04-29

- Q: O SKU pode ser reutilizado apos a remocao logica da peca anterior? → A: Sim, o SKU pode ser reutilizado apos remocao logica da peca anterior.
- Q: Uma peca pode ter quantidade negativa em situacoes de reserva antecipada? → A: Nao, o estoque nunca pode ficar negativo; reservas devem verificar disponibilidade previa.
- Q: A exclusao de uma peca com movimentacoes historicas deve ser permitida? → A: Nao, pecas com historico de movimentacoes nao podem ser excluidas, apenas desativadas (soft delete).
- Q: O sistema deve suportar multiplas localizacoes de estoque (ex: armarios diferentes)? → A: Sim, o sistema deve suportar localizacao fisica no estoque como campo opcional, mas sem controle de transferencia entre locais neste MVP.
- Q: CategoriaPeca e uma entidade separada com CRUD ou campo de classificacao? → A: Campo de texto livre, o usuario digita qualquer categoria.
- Q: Qual estrategia para tratar movimentacoes simultaneas sobre a mesma peca? → A: Otimistic locking com version (detecta conflito e rejeita operacao concorrente).
- Q: Como registrar o responsavel pela movimentacao sem autenticacao no MVP? → A: Nao registrar responsavel no MVP; adicionar quando autenticacao existir.
- Q: O alerta de validade e necessario? → A: Nao, remover alerta de validade e campo data de validade do escopo.
- Q: Ajuste de estoque e delta (+/-) ou valor absoluto? → A: Ajuste como valor absoluto, substituindo o estoque atual.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Cadastrar e manter pecas e insumos (Priority: P1)

Como colaborador da oficina, quero cadastrar pecas e insumos com seus dados obrigatorios e opcionais para manter o catalogo de materiais disponiveis para uso nos servicos.

**Why this priority**: Sem um catalogo confiavel de pecas, nao existe base para controle de estoque, movimentacoes ou integracao com ordens de servico.

**Independent Test**: Pode ser testada de forma independente ao cadastrar uma peca com SKU unico, nome, quantidade inicial, valor unitario, estoque minimo e unidade de medida, verificando que os dados sao persistidos corretamente.

**Acceptance Scenarios**:

1. **Given** dados obrigatorios validos (SKU, nome, quantidade, valor unitario, estoque minimo, unidade), **When** o colaborador cadastra a peca, **Then** o sistema registra a peca com identificador unico e todos os campos informados.
2. **Given** uma peca ja cadastrada, **When** o colaborador atualiza campos permitidos como valor, localizacao ou observacoes, **Then** o sistema salva as alteracoes preservando o historico.
3. **Given** uma tentativa de cadastro com SKU duplicado ativo, **When** o colaborador envia os dados, **Then** o sistema rejeita a operacao com mensagem clara de validacao.
4. **Given** uma tentativa de cadastro com quantidade ou valor negativo, **When** o colaborador envia os dados, **Then** o sistema rejeita a operacao com erro de validacao.

---

### User Story 2 - Controlar movimentacoes de estoque (Priority: P1)

Como colaborador da oficina, quero registrar entradas, saidas e ajustes de estoque para manter o controle preciso da quantidade disponivel de cada peca.

**Why this priority**: O controle de movimentacoes e o coracao do sistema de estoque, permitindo rastreabilidade e prevencao de faltas ou excessos.

**Independent Test**: Pode ser testada realizando uma entrada de estoque, uma saida e um ajuste, verificando que as quantidades sao atualizadas corretamente e o historico registra cada operacao.

**Acceptance Scenarios**:

1. **Given** uma peca existente com estoque atual, **When** o colaborador registra uma entrada com quantidade positiva e justificativa, **Then** o sistema incrementa o estoque e registra a movimentacao no historico.
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
3. **Given** uma peca com estoque acima do minimo, **When** o colaborador verifica sua disponibilidade, **Then** o sistema retorna status "disponivel" com quantidade atual.
4. **Given** uma peca com estoque abaixo do minimo, **When** o colaborador verifica sua disponibilidade, **Then** o sistema retorna status "estoque baixo" com alerta.

---

### User Story 4 - Receber alertas de estoque baixo (Priority: P2)

Como gestor da oficina, quero ser notificado quando pecas atingirem o nivel minimo de estoque para providenciar reposicao em tempo habil.

**Why this priority**: Os alertas sao criticos para evitar faltas de materiais, mas dependem de cadastros e movimentacoes funcionando.

**Independent Test**: Pode ser testada cadastrando pecas com estoque minimo definido, reduzindo o estoque abaixo do limite e verificando que a consulta de itens com estoque baixo retorna as pecas corretas.

**Acceptance Scenarios**:

1. **Given** pecas com estoque abaixo do minimo, **When** o gestor consulta a lista de estoque baixo, **Then** o sistema retorna todas as pecas que atendem ao criterio.
2. **Given** uma peca com estoque zerado, **When** o gestor consulta a lista de estoque baixo, **Then** a peca aparece na lista com indicacao de estoque zerado.

---

### User Story 5 - Consultar historico de movimentacoes (Priority: P3)

Como colaborador ou gestor da oficina, quero consultar o historico completo de movimentacoes de uma peca para rastrear entradas, saidas e ajustes realizados.

**Why this priority**: A rastreabilidade e importante para auditoria e resolucao de problemas, mas e secundaria em relacao ao controle operacional do estoque.

**Independent Test**: Pode ser testada realizando algumas movimentacoes em uma peca e verificando que o historico lista todas as operacoes com data, tipo, quantidade e motivo.

**Acceptance Scenarios**:

1. **Given** uma peca com movimentacoes registradas, **When** o colaborador consulta o historico, **Then** o sistema retorna todas as movimentacoes ordenadas por data decrescente.
2. **Given** uma consulta de historico, **When** o colaborador aplica filtros por tipo ou periodo, **Then** o sistema retorna apenas as movimentacoes que atendem aos criterios.
3. **Given** uma movimentacao especifica, **When** o colaborador consulta seus detalhes, **Then** o sistema exibe quando, quantidade, tipo e motivo/justificativa.

---

### User Story 6 - Remover peca do catalogo (Priority: P3)

Como gestor da oficina, quero desativar pecas descontinuadas para que nao aparecam mais nas consultas operacionais, preservando o historico de movimentacoes.

**Why this priority**: A remocao e necessaria para manter o catalogo limpo, mas e menos critica que as operacoes de controle de estoque.

**Independent Test**: Pode ser testada tentando remover uma peca sem movimentacoes (sucesso) e uma com movimentacoes (erro ou soft delete), verificando que a peca desaparece das consultas ativas mas o historico permanece acessivel.

**Acceptance Scenarios**:

1. **Given** uma peca sem movimentacoes historicas, **When** o gestor solicita sua remocao, **Then** o sistema remove logicamente a peca do catalogo ativo.
2. **Given** uma peca com movimentacoes historicas, **When** o gestor solicita sua remocao, **Then** o sistema aplica soft delete preservando todo o historico.
3. **Given** uma peca removida logicamente, **When** o colaborador consulta apenas pecas ativas, **Then** a peca nao aparece na listagem.

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

- **FR-001**: O sistema MUST permitir cadastrar uma peca/insumo com campos obrigatorios: SKU, nome, quantidade em estoque, valor unitario, estoque minimo e unidade de medida.
- **FR-002**: O sistema MUST aceitar campos opcionais: fornecedor, codigo de barras, localizacao no estoque, marca, categoria (texto livre), aplicacao e observacoes.
- **FR-003**: O sistema MUST validar o SKU como identificador unico entre pecas ativas, permitindo reutilizacao apenas apos remocao logica.
- **FR-004**: O sistema MUST impedir quantidade em estoque negativa em qualquer operacao.
- **FR-005**: O sistema MUST exigir valor unitario positivo e maior que zero.
- **FR-006**: O sistema MUST permitir consultar pecas por identificador unico, por SKU e por filtros de nome/categoria.
- **FR-007**: O sistema MUST oferecer listagem paginada de pecas com filtros para localizacao sem percorrer toda a base.
- **FR-008**: O sistema MUST permitir atualizar dados cadastrais da peca, incluindo valor, localizacao e campos opcionais.
- **FR-009**: O sistema MUST realizar remocao logica da peca, impedindo exclusao fisica quando houver movimentacoes historicas.
- **FR-010**: O sistema MUST excluir pecas removidas logicamente das consultas operacionais padrao, salvo quando solicitado contexto historico.
- **FR-011**: O sistema MUST registrar movimentacoes de estoque do tipo entrada (incrementa), saida (decrementa) e ajuste (valor absoluto substitui estoque atual).
- **FR-012**: O sistema MUST exigir justificativa obrigatoria para ajustes manuais de estoque.
- **FR-013**: O sistema MUST verificar estoque disponivel antes de permitir uma saida.
- **FR-014**: O sistema MUST manter historico completo de todas as movimentacoes com: data/hora, tipo, quantidade e motivo/justificativa.
- **FR-015**: O sistema MUST permitir consultar o historico de movimentacoes de uma peca com filtros por tipo e periodo.
- **FR-016**: O sistema MUST fornecer lista de pecas com estoque abaixo do minimo configurado.
- **FR-017**: O sistema MUST sinalizar pecas com estoque zerado na lista de estoque baixo.
- **FR-018**: O sistema MUST validar a unidade de medida entre os valores permitidos: UN, L, KG, M, ML, CX, PC.
- **FR-019**: O sistema MUST implementar optimistic locking com campo de versao para tratar concurrencia em movimentacoes de estoque.
- **FR-020**: O sistema MUST sanitizar dados textuais de entrada antes de persistir ou exibir.
- **FR-021**: O sistema MUST registrar mensagens de erro claras para dados invalidos, duplicados ou inexistentes.
- **FR-022**: O sistema MUST disponibilizar documentacao atualizada das operacoes via OpenAPI/Swagger.

### Key Entities *(include if feature involves data)*

- **PecaInsumo**: Item utilizado nos servicos da oficina, identificado por SKU unico, com dados de nome, quantidade em estoque, valor unitario, estoque minimo, unidade de medida e campos opcionais de rastreabilidade. Controle de concurrencia via optimistic locking.
- **MovimentacaoEstoque**: Registro de alteracao na quantidade de uma peca, contendo tipo (entrada/saida/ajuste), quantidade, motivo/justificativa e data/hora.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% dos cadastros de pecas concluidos com sucesso apresentam SKU valido, quantidade nao negativa, valor positivo e unidade de medida valida.
- **SC-002**: 100% das tentativas com SKU duplicado ativo, quantidade negativa ou valor invalido sao bloqueadas com retorno explicativo.
- **SC-003**: Colaboradores conseguem localizar uma peca por SKU ou identificador em ate 3 interacoes na interface consumidora do servico.
- **SC-004**: 100% das movimentacoes de estoque sao registradas no historico com data, tipo, quantidade e motivo.
- **SC-004A**: 100% das operacoes concorrentes sobre a mesma peca sao detectadas e rejeitadas com erro de conflito.
- **SC-005**: 100% das tentativas de saida com quantidade maior que o estoque disponivel sao bloqueadas.
- **SC-006**: 100% das remocoes de pecas preservam o historico de movimentacoes e retiram o item das consultas ativas.
- **SC-007**: A lista de pecas com estoque baixo retorna corretamente todos os itens abaixo do minimo configurado.
- **SC-008**: Testes unitarios e de integracao com cobertura minima de 80%.

## Assumptions

- A oficina ja possui cadastro de clientes e veiculos disponivel para futura integracao com ordens de servico.
- O SKU e tratado como principal chave de negocio da peca no contexto operacional da oficina.
- A unidade de medida sera validada contra uma lista predefinida de valores permitidos.
- A integracao com ordens de servico (baixa automatica, reserva de pecas) sera tratada em feature futura.
- A autenticacao e autorizacao das operacoes administrativas sera tratada em iteracao posterior ao MVP.
- O controle de multiplas localizacoes de estoque (transferencias) fica fora do escopo deste MVP.

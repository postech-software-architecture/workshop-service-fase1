# Data Model: Gestao de Veiculos de Clientes

## Entidades

### Veiculo

- **Descricao**: Aggregate root que representa o automovel atendido pela oficina.
- **Campos**:
  - `id`: UUID, obrigatorio, identificador tecnico.
  - `placa`: `Placa`, obrigatoria, unica entre veiculos ativos.
  - `marca`: string, obrigatoria, maximo sugerido de 60 caracteres.
  - `modelo`: string, obrigatoria, maximo sugerido de 80 caracteres.
  - `ano`: inteiro, obrigatorio, entre 1900 e o ano corrente.
  - `cor`: string, opcional, maximo sugerido de 30 caracteres.
  - `chassi`: `Chassi`, opcional.
  - `renavam`: `Renavam`, opcional.
  - `quilometragemAtual`: long, opcional, nao negativo.
  - `tipoCombustivel`: enumeracao opcional com valores `GASOLINA`, `ETANOL`, `FLEX`, `DIESEL`, `GNV`, `ELETRICO`, `HIBRIDO`, `OUTRO`.
  - `observacoes`: string, opcional, maximo sugerido de 2000 caracteres.
  - `clientesVinculados`: colecao obrigatoria de `UUID`, com cardinalidade minima 1.
  - `ativo`: booleano, obrigatorio.
  - `dataCriacao`: timestamp, obrigatorio.
  - `dataUltimaAtualizacao`: timestamp, obrigatorio.
  - `dataRemocao`: timestamp, opcional.
- **Regras de validacao**:
  - Deve existir pelo menos um cliente vinculado em qualquer estado valido do agregado.
  - Todos os `clienteIds` informados devem existir antes da persistencia.
  - Nao pode haver IDs de cliente duplicados na mesma colecao.
  - A placa deve aceitar apenas padroes antigo e Mercosul apos normalizacao.
  - Nao pode haver placa duplicada entre registros ativos.
  - `ano` nao pode ser futuro nem inferior a 1900.
  - `quilometragemAtual`, quando informada, deve ser maior ou igual a zero.
  - `chassi`, quando informado, deve conter 17 caracteres alfanumericos validos.
  - `renavam`, quando informado, deve conter 11 digitos.
- **Transicoes de estado**:
  - `Ativo` -> `Ativo`: atualizacao de dados cadastrais ou substituicao de clientes vinculados, desde que a cardinalidade minima seja preservada.
  - `Ativo` -> `Inativo`: remocao logica com preenchimento de `dataRemocao`.
  - `Inativo` -> `Inativo`: consultas historicas e remocao idempotente sem exclusao fisica.

### Cliente

- **Descricao**: Entidade existente que representa pessoa ou organizacao relacionada ao veiculo.
- **Campos relevantes para a feature**:
  - `id`: UUID.
  - `nome`: string.
  - `documento`: value object existente.
- **Regras de relacionamento**:
  - Um cliente pode estar vinculado a zero ou muitos veiculos.
  - Um veiculo pode estar vinculado a um ou muitos clientes.
  - Nao existe atributo de cliente principal no relacionamento.

### VeiculoCliente

- **Descricao**: Relacionamento persistido entre `Veiculo` e `Cliente` para suportar cardinalidade muitos-para-muitos sem carregar semantica adicional de hierarquia.
- **Campos**:
  - `veiculoId`: UUID, obrigatorio.
  - `clienteId`: UUID, obrigatorio.
- **Regras**:
  - A combinacao (`veiculoId`, `clienteId`) deve ser unica.
  - Um veiculo ativo ou inativo deve manter pelo menos um vinculo registrado.

### HistoricoDeManutencao

- **Descricao**: Conjunto de registros de servicos associados a um veiculo.
- **Campos relevantes para a feature**:
  - `veiculoId`: UUID do veiculo relacionado.
  - `identificadorOrdemServico`: UUID ou identificador equivalente.
  - `datasOperacionais`: abertura/fechamento conforme modulo responsavel.
- **Regras de relacionamento**:
  - Um veiculo pode possuir zero ou muitos registros de manutencao.
  - A remocao logica do veiculo nao remove nem invalida seu historico.

## Value Objects

### Placa

- **Responsabilidades**:
  - Sanitizar entrada removendo espacos e separadores.
  - Converter para caixa alta.
  - Validar padroes `ABC1234` e `ABC1D23`.
  - Expor valor normalizado para comparacao e persistencia.

### Chassi

- **Responsabilidades**:
  - Sanitizar entrada textual.
  - Validar tamanho fixo de 17 caracteres.
  - Restringir caracteres invalidos para identificacao veicular.

### Renavam

- **Responsabilidades**:
  - Remover mascaras.
  - Aceitar apenas 11 digitos.
  - Expor valor normalizado para persistencia.

## Persistencia

### Tabela `veiculos`

- **Colunas principais**:
  - `id` UUID PK
  - `placa` VARCHAR(7) ou equivalente, com valor normalizado
  - `marca`, `modelo`, `ano`, `cor`, `chassi`, `renavam`, `quilometragem_atual`, `tipo_combustivel`, `observacoes`
  - `ativo` BOOLEAN NOT NULL
  - `data_remocao` TIMESTAMP NULL
  - `data_criacao` TIMESTAMP NOT NULL
  - `data_ultima_atualizacao` TIMESTAMP NOT NULL
- **Indices**:
  - `UNIQUE` parcial ou estrategia equivalente para `placa` entre veiculos ativos.
  - Indices auxiliares para filtros por `ativo` e `placa`.

### Tabela `veiculos_clientes`

- **Colunas principais**:
  - `veiculo_id` UUID NOT NULL
  - `cliente_id` UUID NOT NULL
- **Restricoes**:
  - PK composta (`veiculo_id`, `cliente_id`) ou `UNIQUE` equivalente.
  - FK `fk_veiculos_clientes_veiculos`
  - FK `fk_veiculos_clientes_clientes`
- **Indices**:
  - Indice por `cliente_id` para consulta rapida dos veiculos de um cliente.
  - Indice por `veiculo_id` para carregamento dos vinculos do agregado.

## Consultas Operacionais

- Buscar por ID: retorna um unico veiculo com todos os clientes vinculados.
- Buscar por placa: utiliza valor normalizado e ignora diferencas de formatacao.
- Buscar por cliente: retorna todos os veiculos vinculados ao cliente, inclusive compartilhados.
- Listagem geral: suporta filtros por placa, cliente e `incluirInativos`, sempre com paginacao.

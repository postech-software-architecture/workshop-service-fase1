# Data Model: CRUD Completo de Clientes

## Entidades de Domínio

### Cliente (Aggregate Root)

Representa um cliente da oficina.

**Atributos**:
- `id` (UUID): Identificador único do cliente.
- `nome` (String): Nome completo ou razão social.
- `documento` (ValueObject: Documento): CPF ou CNPJ validado e padronizado (Imutável).
- `email` (String, nullable): Endereço de e-mail de contato.
- `telefone` (String, nullable): Telefone de contato.
- `endereco` (ValueObject: Endereco, nullable): Endereço completo.
- `dataNascimentoFundacao` (LocalDate, nullable): Data de nascimento (PF) ou fundação (PJ).
- `observacoes` (String, nullable): Notas adicionais.
- `dataCriacao` (LocalDateTime): Auditoria.
- `dataUltimaAtualizacao` (LocalDateTime): Auditoria.

**Invariantes / Validações**:
- O `documento` deve ser válido (dígitos verificadores corretos).
- Ao menos um contato (email ou telefone) é obrigatório.
- Documento é único e não pode ser alterado após a criação.

### Endereco (Value Object)

**Atributos**:
- `logradouro` (String)
- `numero` (String)
- `complemento` (String, nullable)
- `bairro` (String)
- `cidade` (String)
- `estado` (String)
- `cep` (String)

### Documento (Value Object)

**Atributos**:
- `valor` (String): Apenas números.
- `tipo` (Enum): CPF ou CNPJ.

**Comportamentos**:
- `mascarado()`: Ofuscação para logs/visualização.
- `validar()`: Aplica os algoritmos oficiais de CPF/CNPJ.

## Mapeamento de Persistência (JPA)

Tabela: `clientes`
- `id`: UUID (PK)
- `nome`: VARCHAR(255)
- `documento`: VARCHAR(20) (Unique Index)
- `email`: VARCHAR(255)
- `telefone`: VARCHAR(20)
- `logradouro`, `numero`, `complemento`, `bairro`, `cidade`, `estado`, `cep`: Colunas prefixadas (Embedded).
- `data_nascimento_fundacao`: DATE
- `observacoes`: TEXT
- `data_criacao`: TIMESTAMP
- `data_ultima_atualizacao`: TIMESTAMP

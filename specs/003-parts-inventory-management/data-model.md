# Data Model: Gestao de Pecas, Insumos e Estoques

**Feature**: 003-parts-inventory-management  
**Date**: 2026-04-29  
**Updated**: 2026-04-29 (adicao da entidade Estoques)

## Entities

### PecaInsumo (Aggregate Root)

**Description**: Item utilizado nos servicos da oficina, identificado por SKU unico. Nao armazena quantidade total (delegado a entidade Estoques).

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id | UUID | PK, NOT NULL | Identificador tecnico |
| sku | VARCHAR(50) | NOT NULL, UNIQUE (ativo) | Codigo unico da peca |
| nome | VARCHAR(200) | NOT NULL | Nome/descricao da peca |
| valorUnitario | DECIMAL(10,2) | NOT NULL, > 0 | Preco unitario |
| estoqueMinimo | DECIMAL(10,3) | NOT NULL, >= 0 | Nivel de alerta para reposicao (soma de todos os estoques) |
| unidadeMedida | VARCHAR(10) | NOT NULL | Unidade: UN, L, KG, M, ML, CX, PC |
| fornecedor | VARCHAR(200) | NULLABLE | Nome do fornecedor |
| codigoBarras | VARCHAR(50) | NULLABLE | Codigo de barras |
| marca | VARCHAR(100) | NULLABLE | Marca/fabricante |
| categoria | VARCHAR(100) | NULLABLE | Categoria (texto livre) |
| aplicacao | VARCHAR(500) | NULLABLE | Modelos compativeis |
| observacoes | TEXT | NULLABLE | Observacoes gerais |
| ativo | BOOLEAN | NOT NULL, DEFAULT true | Indicador de ativo |
| versao | INTEGER | NOT NULL, DEFAULT 0 | Versao para optimistic locking |
| dataCriacao | TIMESTAMP | NOT NULL | Data de criacao |
| dataUltimaAtualizacao | TIMESTAMP | NOT NULL | Data da ultima atualizacao |
| dataRemocao | TIMESTAMP | NULLABLE | Data da remocao logica |

**Business Rules**:
- SKU deve ser unico entre pecas ativas
- Valor unitario deve ser positivo
- Soft delete: ao remover, setar ativo=false e dataRemocao=now()
- Quantidade total = soma das quantidades em todos os Estoques vinculados

---

### Estoque (Entity)

**Description**: Registro de quantidade de uma peca em uma localizacao especifica. Permite multiplas localizacoes por peca.

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id | UUID | PK, NOT NULL | Identificador tecnico |
| pecaInsumoId | UUID | FK, NOT NULL | Referencia a peca |
| localizacao | VARCHAR(100) | NOT NULL | Localizacao fisica (ex: "Prateleira A2") |
| quantidade | DECIMAL(10,3) | NOT NULL, >= 0 | Quantidade nesta localizacao |
| ativo | BOOLEAN | NOT NULL, DEFAULT true | Indicador de estoque ativo |
| versao | INTEGER | NOT NULL, DEFAULT 0 | Versao para optimistic locking |
| dataCriacao | TIMESTAMP | NOT NULL | Data de criacao |
| dataUltimaAtualizacao | TIMESTAMP | NOT NULL | Data da ultima atualizacao |

**Business Rules**:
- Uma peca pode ter multiplos estoques (locais diferentes)
- Quantidade nunca pode ser negativa
- Localizacao deve ser unica por peca (uma peca so pode ter um estoque por localizacao)
- Soft delete: ao remover, setar ativo=false

---

### MovimentacaoEstoque (Entity)

**Description**: Registro de alteracao na quantidade de um estoque especifico.

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id | UUID | PK, NOT NULL | Identificador tecnico |
| estoqueId | UUID | FK, NOT NULL | Referencia ao estoque |
| tipo | VARCHAR(20) | NOT NULL | Tipo: ENTRADA, SAIDA, AJUSTE |
| quantidade | DECIMAL(10,3) | NOT NULL | Quantidade movimentada |
| quantidadeAnterior | DECIMAL(10,3) | NOT NULL | Quantidade antes da movimentacao |
| quantidadePosterior | DECIMAL(10,3) | NOT NULL | Quantidade apos a movimentacao |
| motivo | VARCHAR(500) | NULLABLE | Motivo/justificativa |
| dataMovimentacao | TIMESTAMP | NOT NULL | Data/hora da movimentacao |
| dataCriacao | TIMESTAMP | NOT NULL | Data de criacao do registro |

**Business Rules**:
- ENTRADA: incrementa quantidade do estoque
- SAIDA: decrementa quantidade do estoque (validar disponibilidade)
- AJUSTE: substitui quantidade do estoque pelo valor informado
- AJUSTE exige motivo obrigatorio
- SAIDA rejeitada se quantidade > quantidade do estoque

---

## Relationships

```
PecaInsumo (1) ──────< (N) Estoque
    │                        │
    │                        └──────< (N) MovimentacaoEstoque
    │
    └── has many estoques (cascade persist)
    
Estoque (1) ──────< (N) MovimentacaoEstoque
    │
    └── has many movimentacoes (cascade persist)
```

## Value Objects

### TipoMovimentacao

```java
public enum TipoMovimentacao {
    ENTRADA,  // Incrementa estoque
    SAIDA,    // Decrementa estoque
    AJUSTE    // Substitui valor absoluto
}
```

### UnidadeMedida

```java
public enum UnidadeMedida {
    UN,   // Unidade
    L,    // Litro
    KG,   // Quilograma
    M,    // Metro
    ML,   // Mililitro
    CX,   // Caixa
    PC    // Peca
}
```

## Database Schema (PostgreSQL)

```sql
-- Tabela de pecas/insumos
CREATE TABLE pecas_insumos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sku VARCHAR(50) NOT NULL,
    nome VARCHAR(200) NOT NULL,
    valor_unitario DECIMAL(10,2) NOT NULL,
    estoque_minimo DECIMAL(10,3) NOT NULL DEFAULT 0,
    unidade_medida VARCHAR(10) NOT NULL,
    fornecedor VARCHAR(200),
    codigo_barras VARCHAR(50),
    marca VARCHAR(100),
    categoria VARCHAR(100),
    aplicacao VARCHAR(500),
    observacoes TEXT,
    ativo BOOLEAN NOT NULL DEFAULT true,
    versao INTEGER NOT NULL DEFAULT 0,
    data_criacao TIMESTAMP NOT NULL DEFAULT now(),
    data_ultima_atualizacao TIMESTAMP NOT NULL DEFAULT now(),
    data_remocao TIMESTAMP,
    
    CONSTRAINT uk_pecas_insumos_sku_ativo UNIQUE (sku) WHERE (ativo = true),
    CONSTRAINT chk_estoque_minimo_nao_negativo CHECK (estoque_minimo >= 0),
    CONSTRAINT chk_valor_positivo CHECK (valor_unitario > 0)
);

COMMENT ON TABLE pecas_insumos IS 'Cadastro de pecas e insumos utilizados nos servicos da oficina';
COMMENT ON COLUMN pecas_insumos.id IS 'Identificador unico da peca';
COMMENT ON COLUMN pecas_insumos.sku IS 'Codigo SKU unico entre pecas ativas';
COMMENT ON COLUMN pecas_insumos.nome IS 'Nome ou descricao da peca';
COMMENT ON COLUMN pecas_insumos.valor_unitario IS 'Preco unitario da peca';
COMMENT ON COLUMN pecas_insumos.estoque_minimo IS 'Nivel minimo para alerta de reposicao (soma de todos os estoques)';
COMMENT ON COLUMN pecas_insumos.unidade_medida IS 'Unidade de medida: UN, L, KG, M, ML, CX, PC';
COMMENT ON COLUMN pecas_insumos.ativo IS 'Indicador de peca ativa no catalogo';
COMMENT ON COLUMN pecas_insumos.versao IS 'Versao para controle de concurrencia (optimistic locking)';

-- Tabela de estoques (localizacoes)
CREATE TABLE estoques (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    peca_insumo_id UUID NOT NULL,
    localizacao VARCHAR(100) NOT NULL,
    quantidade DECIMAL(10,3) NOT NULL DEFAULT 0,
    ativo BOOLEAN NOT NULL DEFAULT true,
    versao INTEGER NOT NULL DEFAULT 0,
    data_criacao TIMESTAMP NOT NULL DEFAULT now(),
    data_ultima_atualizacao TIMESTAMP NOT NULL DEFAULT now(),
    
    CONSTRAINT fk_estoques_pecas_insumos FOREIGN KEY (peca_insumo_id) 
        REFERENCES pecas_insumos(id) ON DELETE RESTRICT,
    CONSTRAINT uk_estoques_peca_localizacao UNIQUE (peca_insumo_id, localizacao),
    CONSTRAINT chk_quantidade_nao_negativa CHECK (quantidade >= 0)
);

CREATE INDEX idx_estoques_peca ON estoques(peca_insumo_id);
CREATE INDEX idx_estoques_ativo ON estoques(ativo);

COMMENT ON TABLE estoques IS 'Registro de quantidade de pecas por localizacao fisica';
COMMENT ON COLUMN estoques.peca_insumo_id IS 'Referencia a peca do estoque';
COMMENT ON COLUMN estoques.localizacao IS 'Localizacao fisica no estoque (ex: Prateleira A2)';
COMMENT ON COLUMN estoques.quantidade IS 'Quantidade disponivel nesta localizacao';
COMMENT ON COLUMN estoques.ativo IS 'Indicador de estoque ativo';
COMMENT ON COLUMN estoques.versao IS 'Versao para controle de concurrencia (optimistic locking)';

-- Tabela de movimentacoes de estoque
CREATE TABLE movimentacoes_estoque (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    estoque_id UUID NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    quantidade DECIMAL(10,3) NOT NULL,
    quantidade_anterior DECIMAL(10,3) NOT NULL,
    quantidade_posterior DECIMAL(10,3) NOT NULL,
    motivo VARCHAR(500),
    data_movimentacao TIMESTAMP NOT NULL DEFAULT now(),
    data_criacao TIMESTAMP NOT NULL DEFAULT now(),
    
    CONSTRAINT fk_movimentacoes_estoques FOREIGN KEY (estoque_id) 
        REFERENCES estoques(id) ON DELETE RESTRICT,
    CONSTRAINT chk_tipo_valido CHECK (tipo IN ('ENTRADA', 'SAIDA', 'AJUSTE'))
);

CREATE INDEX idx_movimentacoes_estoque ON movimentacoes_estoque(estoque_id);
CREATE INDEX idx_movimentacoes_data ON movimentacoes_estoque(data_movimentacao DESC);

COMMENT ON TABLE movimentacoes_estoque IS 'Historico de movimentacoes de estoque';
COMMENT ON COLUMN movimentacoes_estoque.estoque_id IS 'Referencia ao estoque movimentado';
COMMENT ON COLUMN movimentacoes_estoque.tipo IS 'Tipo de movimentacao: ENTRADA, SAIDA, AJUSTE';
COMMENT ON COLUMN movimentacoes_estoque.quantidade IS 'Quantidade movimentada';
COMMENT ON COLUMN movimentacoes_estoque.quantidade_anterior IS 'Quantidade em estoque antes da movimentacao';
COMMENT ON COLUMN movimentacoes_estoque.quantidade_posterior IS 'Quantidade em estoque apos a movimentacao';
COMMENT ON COLUMN movimentacoes_estoque.motivo IS 'Motivo ou justificativa da movimentacao';

-- Indices para consultas frequentes
CREATE INDEX idx_pecas_insumos_sku ON pecas_insumos(sku);
CREATE INDEX idx_pecas_insumos_ativo ON pecas_insumos(ativo);
CREATE INDEX idx_pecas_insumos_categoria ON pecas_insumos(categoria);

-- View para consulta de pecas com quantidade total
CREATE VIEW vw_pecas_com_estoque_total AS
SELECT 
    p.id,
    p.sku,
    p.nome,
    p.valor_unitario,
    p.estoque_minimo,
    p.unidade_medida,
    p.categoria,
    p.ativo,
    COALESCE(SUM(e.quantidade), 0) AS quantidade_total
FROM pecas_insumos p
LEFT JOIN estoques e ON e.peca_insumo_id = p.id AND e.ativo = true
GROUP BY p.id;

COMMENT ON VIEW vw_pecas_com_estoque_total IS 'View para consulta de pecas com quantidade total calculada';
```

## Index Strategy

| Index | Purpose |
|-------|---------|
| uk_pecas_insumos_sku_ativo | Garantir unicidade de SKU entre pecas ativas |
| idx_pecas_insumos_sku | Busca rapida por SKU |
| idx_pecas_insumos_ativo | Filtrar pecas ativas nas consultas |
| idx_pecas_insumos_categoria | Filtro por categoria |
| uk_estoques_peca_localizacao | Garantir localizacao unica por peca |
| idx_estoques_peca | Buscar estoques de uma peca |
| idx_estoques_ativo | Filtrar estoques ativos |
| idx_movimentacoes_estoque | Historico de movimentacoes por estoque |
| idx_movimentacoes_data | Ordenacao por data decrescente |

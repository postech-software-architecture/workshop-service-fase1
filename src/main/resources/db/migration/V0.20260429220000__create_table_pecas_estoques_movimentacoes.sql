-- Tabela de pecas/insumos
CREATE TABLE pecas_insumos (
    id UUID PRIMARY KEY,
    sku VARCHAR(50) NOT NULL,
    nome VARCHAR(200) NOT NULL,
    valor_unitario DECIMAL(10,2) NOT NULL,
    estoque_minimo DECIMAL(10,3) NOT NULL DEFAULT 0,
    unidade_medida VARCHAR(10) NOT NULL,
    tipo_item VARCHAR(20) NOT NULL,
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

    CONSTRAINT chk_estoque_minimo_nao_negativo CHECK (estoque_minimo >= 0),
    CONSTRAINT chk_valor_positivo CHECK (valor_unitario > 0)
);

-- Partial unique index for SKU (only active parts)
CREATE UNIQUE INDEX uk_pecas_insumos_sku_ativo ON pecas_insumos(sku) WHERE ativo = true;

COMMENT ON TABLE pecas_insumos IS 'Cadastro de pecas e insumos utilizados nos servicos da oficina';
COMMENT ON COLUMN pecas_insumos.id IS 'Identificador unico da peca';
COMMENT ON COLUMN pecas_insumos.sku IS 'Codigo SKU unico entre pecas ativas';
COMMENT ON COLUMN pecas_insumos.nome IS 'Nome ou descricao da peca';
COMMENT ON COLUMN pecas_insumos.valor_unitario IS 'Preco unitario da peca';
COMMENT ON COLUMN pecas_insumos.estoque_minimo IS 'Nivel minimo para alerta de reposicao (soma de todos os estoques)';
COMMENT ON COLUMN pecas_insumos.unidade_medida IS 'Unidade de medida: UN, L, KG, M, ML, CX, PC';
COMMENT ON COLUMN pecas_insumos.tipo_item IS 'Tipo principal do item: PECA ou INSUMO';
COMMENT ON COLUMN pecas_insumos.ativo IS 'Indicador de peca ativa no catalogo';
COMMENT ON COLUMN pecas_insumos.versao IS 'Versao para controle de concurrencia (optimistic locking)';

-- Tabela de estoques (localizacoes)
CREATE TABLE estoques (
    id UUID PRIMARY KEY,
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
    id UUID PRIMARY KEY,
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

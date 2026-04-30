CREATE TABLE servicos (
    id UUID PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    descricao TEXT NOT NULL,
    valor NUMERIC(10,2) NOT NULL,
    tempo_estimado_minutos INTEGER NOT NULL,
    categoria VARCHAR(20),
    nivel_complexidade VARCHAR(10),
    garantia_dias INTEGER,
    observacoes_tecnicas TEXT,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    data_criacao TIMESTAMP NOT NULL,
    data_ultima_atualizacao TIMESTAMP NOT NULL,
    data_remocao TIMESTAMP,
    CONSTRAINT ck_servicos_valor_positivo CHECK (valor > 0),
    CONSTRAINT ck_servicos_tempo_estimado_positivo CHECK (tempo_estimado_minutos > 0),
    CONSTRAINT ck_servicos_garantia_dias_positivo CHECK (garantia_dias > 0)
);

CREATE UNIQUE INDEX ux_servicos_nome_ativo ON servicos (nome) WHERE ativo = TRUE;
CREATE INDEX ix_servicos_ativo ON servicos (ativo);
CREATE INDEX ix_servicos_categoria ON servicos (categoria);

COMMENT ON TABLE servicos IS 'Tabela que armazena os servicos oferecidos pela oficina.';
COMMENT ON COLUMN servicos.id IS 'Identificador unico do servico em formato UUID.';
COMMENT ON COLUMN servicos.nome IS 'Nome do servico, unico entre os servicos ativos.';
COMMENT ON COLUMN servicos.descricao IS 'Descricao detalhada do servico prestado.';
COMMENT ON COLUMN servicos.valor IS 'Valor cobrado pelo servico, deve ser maior que zero.';
COMMENT ON COLUMN servicos.tempo_estimado_minutos IS 'Tempo estimado de execucao do servico em minutos, deve ser maior que zero.';
COMMENT ON COLUMN servicos.categoria IS 'Categoria do servico: MECANICA, ELETRICA, ESTETICA ou PREVENTIVA.';
COMMENT ON COLUMN servicos.nivel_complexidade IS 'Nivel de complexidade do servico: BAIXA, MEDIA ou ALTA.';
COMMENT ON COLUMN servicos.garantia_dias IS 'Prazo de garantia do servico em dias, quando aplicavel.';
COMMENT ON COLUMN servicos.observacoes_tecnicas IS 'Observacoes tecnicas livres sobre a execucao do servico.';
COMMENT ON COLUMN servicos.ativo IS 'Indica se o servico esta ativo e disponivel para uso.';
COMMENT ON COLUMN servicos.data_criacao IS 'Data e hora de criacao do registro.';
COMMENT ON COLUMN servicos.data_ultima_atualizacao IS 'Data e hora da ultima atualizacao do registro.';
COMMENT ON COLUMN servicos.data_remocao IS 'Data e hora em que o servico foi removido logicamente.';

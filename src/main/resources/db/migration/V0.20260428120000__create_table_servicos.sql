CREATE TABLE servicos (
    id UUID PRIMARY KEY,
    nome VARCHAR(120) NOT NULL,
    descricao TEXT NOT NULL,
    valor NUMERIC(10,2) NOT NULL,
    categoria VARCHAR(30),
    nivel_complexidade VARCHAR(20),
    garantia_dias INTEGER,
    observacoes_tecnicas TEXT,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    data_criacao TIMESTAMP NOT NULL,
    data_ultima_atualizacao TIMESTAMP NOT NULL,
    data_remocao TIMESTAMP,
    CONSTRAINT ck_servicos_valor_positivo CHECK (valor > 0),
    CONSTRAINT ck_servicos_garantia_dias_positivo CHECK (garantia_dias IS NULL OR garantia_dias > 0)
);

CREATE UNIQUE INDEX ux_servicos_nome_ativo ON servicos (nome) WHERE ativo = TRUE;
CREATE INDEX ix_servicos_ativo ON servicos (ativo);
CREATE INDEX ix_servicos_categoria ON servicos (categoria);

COMMENT ON TABLE servicos IS 'Tabela que armazena os serviços oferecidos pela oficina.';
COMMENT ON COLUMN servicos.id IS 'Identificador único do serviço em formato UUID.';
COMMENT ON COLUMN servicos.nome IS 'Nome do serviço, único entre os serviços ativos.';
COMMENT ON COLUMN servicos.descricao IS 'Descrição detalhada do serviço prestado.';
COMMENT ON COLUMN servicos.valor IS 'Valor cobrado pelo serviço, deve ser maior que zero.';
COMMENT ON COLUMN servicos.categoria IS 'Categoria do serviço: MECANICA, ELETRICA, ESTETICA ou PREVENTIVA.';
COMMENT ON COLUMN servicos.nivel_complexidade IS 'Nível de complexidade do serviço: BAIXA, MEDIA ou ALTA.';
COMMENT ON COLUMN servicos.garantia_dias IS 'Prazo de garantia do serviço em dias, quando aplicável.';
COMMENT ON COLUMN servicos.observacoes_tecnicas IS 'Observações técnicas livres sobre a execução do serviço.';
COMMENT ON COLUMN servicos.ativo IS 'Indica se o serviço está ativo e disponível para uso.';
COMMENT ON COLUMN servicos.data_criacao IS 'Data e hora de criação do registro.';
COMMENT ON COLUMN servicos.data_ultima_atualizacao IS 'Data e hora da última atualização do registro.';
COMMENT ON COLUMN servicos.data_remocao IS 'Data e hora em que o serviço foi removido logicamente.';

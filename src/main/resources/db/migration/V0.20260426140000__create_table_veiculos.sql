CREATE TABLE veiculos (
    id UUID PRIMARY KEY,
    placa VARCHAR(7) NOT NULL,
    marca VARCHAR(60) NOT NULL,
    modelo VARCHAR(80) NOT NULL,
    ano INTEGER NOT NULL,
    cor VARCHAR(30),
    observacoes TEXT,
    ativo BOOLEAN NOT NULL,
    data_remocao TIMESTAMP,
    data_criacao TIMESTAMP NOT NULL,
    data_ultima_atualizacao TIMESTAMP NOT NULL
);

CREATE TABLE veiculos_clientes (
    veiculo_id UUID NOT NULL,
    cliente_id UUID NOT NULL,
    data_criacao TIMESTAMP NOT NULL,
    data_ultima_atualizacao TIMESTAMP NOT NULL,
    CONSTRAINT pk_veiculos_clientes PRIMARY KEY (veiculo_id, cliente_id),
    CONSTRAINT fk_veiculos_clientes_veiculos FOREIGN KEY (veiculo_id) REFERENCES veiculos(id),
    CONSTRAINT fk_veiculos_clientes_clientes FOREIGN KEY (cliente_id) REFERENCES clientes(id)
);

CREATE UNIQUE INDEX ux_veiculos_placa_ativa ON veiculos (placa) WHERE ativo = TRUE;
CREATE INDEX ix_veiculos_ativo ON veiculos (ativo);
CREATE INDEX ix_veiculos_clientes_cliente_id ON veiculos_clientes (cliente_id);

COMMENT ON TABLE veiculos IS 'Tabela que armazena os veiculos atendidos pela oficina.';
COMMENT ON COLUMN veiculos.id IS 'Identificador unico do veiculo em formato UUID.';
COMMENT ON COLUMN veiculos.placa IS 'Placa normalizada do veiculo, sem separadores e em caixa alta.';
COMMENT ON COLUMN veiculos.marca IS 'Marca comercial do veiculo.';
COMMENT ON COLUMN veiculos.modelo IS 'Modelo comercial do veiculo.';
COMMENT ON COLUMN veiculos.ano IS 'Ano do veiculo validado dentro da faixa operacional da oficina.';
COMMENT ON COLUMN veiculos.cor IS 'Cor predominante do veiculo.';
COMMENT ON COLUMN veiculos.observacoes IS 'Observacoes operacionais livres sobre o veiculo.';
COMMENT ON COLUMN veiculos.ativo IS 'Indica se o veiculo esta ativo nas consultas operacionais padrao.';
COMMENT ON COLUMN veiculos.data_remocao IS 'Data e hora em que o veiculo foi removido logicamente.';
COMMENT ON COLUMN veiculos.data_criacao IS 'Data e hora de criacao do registro.';
COMMENT ON COLUMN veiculos.data_ultima_atualizacao IS 'Data e hora da ultima atualizacao do registro.';

COMMENT ON TABLE veiculos_clientes IS 'Tabela de associacao entre veiculos e clientes vinculados.';
COMMENT ON COLUMN veiculos_clientes.veiculo_id IS 'Identificador do veiculo associado.';
COMMENT ON COLUMN veiculos_clientes.cliente_id IS 'Identificador do cliente associado.';
COMMENT ON COLUMN veiculos_clientes.data_criacao IS 'Data e hora de criacao do vinculo.';
COMMENT ON COLUMN veiculos_clientes.data_ultima_atualizacao IS 'Data e hora da ultima atualizacao do vinculo.';

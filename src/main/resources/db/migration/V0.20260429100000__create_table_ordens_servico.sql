CREATE TABLE ordens_servico (
    id UUID PRIMARY KEY,
    id_cliente UUID NOT NULL,
    id_veiculo UUID NOT NULL,
    status VARCHAR(40) NOT NULL,
    data_remocao TIMESTAMP,
    data_criacao TIMESTAMP NOT NULL,
    data_ultima_atualizacao TIMESTAMP NOT NULL
);

CREATE TABLE ordens_servico_itens (
    ordem_servico_id UUID NOT NULL,
    ordem_item INTEGER NOT NULL,
    descricao VARCHAR(255) NOT NULL,
    valor NUMERIC(19, 2) NOT NULL,
    tipo VARCHAR(30) NOT NULL,
    CONSTRAINT pk_ordens_servico_itens PRIMARY KEY (ordem_servico_id, ordem_item),
    CONSTRAINT fk_ordens_servico_itens_ordem_servico FOREIGN KEY (ordem_servico_id) REFERENCES ordens_servico(id)
);

CREATE INDEX ix_ordens_servico_cliente ON ordens_servico (id_cliente);
CREATE INDEX ix_ordens_servico_veiculo ON ordens_servico (id_veiculo);
CREATE INDEX ix_ordens_servico_status ON ordens_servico (status);

COMMENT ON TABLE ordens_servico IS 'Tabela que armazena as ordens de servico da oficina.';
COMMENT ON COLUMN ordens_servico.id IS 'Identificador unico da ordem de servico em formato UUID.';
COMMENT ON COLUMN ordens_servico.id_cliente IS 'Identificador do cliente vinculado a ordem de servico.';
COMMENT ON COLUMN ordens_servico.id_veiculo IS 'Identificador do veiculo vinculado a ordem de servico.';
COMMENT ON COLUMN ordens_servico.status IS 'Estado atual do fluxo operacional da ordem de servico.';
COMMENT ON COLUMN ordens_servico.data_remocao IS 'Data e hora de remocao logica da ordem de servico.';
COMMENT ON COLUMN ordens_servico.data_criacao IS 'Data e hora de criacao do registro da ordem de servico.';
COMMENT ON COLUMN ordens_servico.data_ultima_atualizacao IS 'Data e hora da ultima atualizacao do registro da ordem de servico.';

COMMENT ON TABLE ordens_servico_itens IS 'Tabela que armazena os itens de composicao tecnica de cada ordem de servico.';
COMMENT ON COLUMN ordens_servico_itens.ordem_servico_id IS 'Identificador da ordem de servico dona do item de composicao.';
COMMENT ON COLUMN ordens_servico_itens.ordem_item IS 'Posicao ordenada do item dentro da composicao tecnica da ordem.';
COMMENT ON COLUMN ordens_servico_itens.descricao IS 'Descricao do item de composicao tecnica.';
COMMENT ON COLUMN ordens_servico_itens.valor IS 'Valor monetario do item de composicao tecnica.';
COMMENT ON COLUMN ordens_servico_itens.tipo IS 'Classificacao do item de composicao tecnica entre servico, peca e insumo.';

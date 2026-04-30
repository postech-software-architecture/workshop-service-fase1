CREATE TABLE orcamentos (
    id UUID PRIMARY KEY,
    id_ordem_servico UUID NOT NULL,
    valor NUMERIC(19, 2) NOT NULL,
    status VARCHAR(40) NOT NULL,
    tipo VARCHAR(40) NOT NULL,
    data_remocao TIMESTAMP,
    data_criacao TIMESTAMP NOT NULL,
    data_ultima_atualizacao TIMESTAMP NOT NULL,
    CONSTRAINT fk_orcamentos_ordens_servico FOREIGN KEY (id_ordem_servico) REFERENCES ordens_servico(id)
);

CREATE TABLE orcamentos_itens (
    orcamento_id UUID NOT NULL,
    ordem_item INTEGER NOT NULL,
    descricao VARCHAR(255) NOT NULL,
    valor NUMERIC(19, 2) NOT NULL,
    CONSTRAINT pk_orcamentos_itens PRIMARY KEY (orcamento_id, ordem_item),
    CONSTRAINT fk_orcamentos_itens_orcamento FOREIGN KEY (orcamento_id) REFERENCES orcamentos(id)
);

CREATE INDEX ix_orcamentos_ordem_servico ON orcamentos (id_ordem_servico);
CREATE INDEX ix_orcamentos_status ON orcamentos (status);
CREATE UNIQUE INDEX ux_orcamentos_ordem_pendente ON orcamentos (id_ordem_servico) WHERE status = 'PENDENTE_APROVACAO';

COMMENT ON TABLE orcamentos IS 'Tabela que armazena os orcamentos vinculados as ordens de servico.';
COMMENT ON COLUMN orcamentos.id IS 'Identificador unico do orcamento em formato UUID.';
COMMENT ON COLUMN orcamentos.id_ordem_servico IS 'Identificador da ordem de servico vinculada ao orcamento.';
COMMENT ON COLUMN orcamentos.valor IS 'Valor total do orcamento calculado a partir dos itens fotografados.';
COMMENT ON COLUMN orcamentos.status IS 'Estado atual do fluxo de aprovacao do orcamento.';
COMMENT ON COLUMN orcamentos.tipo IS 'Tipo do orcamento dentro do fluxo da ordem de servico.';
COMMENT ON COLUMN orcamentos.data_remocao IS 'Data e hora de remocao logica do orcamento.';
COMMENT ON COLUMN orcamentos.data_criacao IS 'Data e hora de criacao do registro do orcamento.';
COMMENT ON COLUMN orcamentos.data_ultima_atualizacao IS 'Data e hora da ultima atualizacao do registro do orcamento.';

COMMENT ON TABLE orcamentos_itens IS 'Tabela que armazena a fotografia dos itens enviados no orcamento.';
COMMENT ON COLUMN orcamentos_itens.orcamento_id IS 'Identificador do orcamento dono do item fotografado.';
COMMENT ON COLUMN orcamentos_itens.ordem_item IS 'Posicao ordenada do item dentro da fotografia do orcamento.';
COMMENT ON COLUMN orcamentos_itens.descricao IS 'Descricao do item fotografado no orcamento.';
COMMENT ON COLUMN orcamentos_itens.valor IS 'Valor monetario do item fotografado no orcamento.';

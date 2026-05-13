-- Adiciona ciclo de vida individual aos itens de servico da ordem de servico.
-- Substitui a PK composta (ordem_servico_id, ordem_item) por uma PK UUID propria,
-- mantendo ordem_item como indicador de prioridade de execucao.

ALTER TABLE ordens_servico_itens ADD COLUMN id UUID;
UPDATE ordens_servico_itens SET id = gen_random_uuid() WHERE id IS NULL;
ALTER TABLE ordens_servico_itens ALTER COLUMN id SET NOT NULL;

ALTER TABLE ordens_servico_itens DROP CONSTRAINT pk_ordens_servico_itens;
ALTER TABLE ordens_servico_itens ADD CONSTRAINT pk_ordens_servico_itens PRIMARY KEY (id);
ALTER TABLE ordens_servico_itens
    ADD CONSTRAINT uq_ordens_servico_itens_ordem UNIQUE (ordem_servico_id, ordem_item);

ALTER TABLE ordens_servico_itens ADD COLUMN servico_id UUID;
ALTER TABLE ordens_servico_itens
    ADD CONSTRAINT fk_ordens_servico_itens_servico FOREIGN KEY (servico_id) REFERENCES servicos(id);

ALTER TABLE ordens_servico_itens ADD COLUMN status_execucao VARCHAR(20);
ALTER TABLE ordens_servico_itens ADD COLUMN data_inicio_execucao TIMESTAMP;
ALTER TABLE ordens_servico_itens ADD COLUMN data_finalizacao TIMESTAMP;

UPDATE ordens_servico_itens SET status_execucao = 'PENDENTE' WHERE tipo = 'SERVICO';

CREATE INDEX ix_ordens_servico_itens_servico ON ordens_servico_itens (servico_id);
CREATE INDEX ix_ordens_servico_itens_status_execucao ON ordens_servico_itens (status_execucao);

COMMENT ON COLUMN ordens_servico_itens.id IS 'Identificador unico do item de composicao tecnica em formato UUID.';
COMMENT ON COLUMN ordens_servico_itens.ordem_item IS 'Prioridade de execucao do item dentro da composicao tecnica da ordem.';
COMMENT ON COLUMN ordens_servico_itens.servico_id IS 'Identificador do servico do catalogo vinculado ao item, preenchido apenas para itens do tipo SERVICO.';
COMMENT ON COLUMN ordens_servico_itens.status_execucao IS 'Estado do ciclo de execucao do item de servico (PENDENTE, EM_EXECUCAO, FINALIZADO). Apenas para itens do tipo SERVICO.';
COMMENT ON COLUMN ordens_servico_itens.data_inicio_execucao IS 'Data e hora em que a execucao tecnica do servico foi iniciada pelo mecanico.';
COMMENT ON COLUMN ordens_servico_itens.data_finalizacao IS 'Data e hora em que a execucao tecnica do servico foi finalizada pelo mecanico.';


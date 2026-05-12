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

-- Ordens de servico de demonstracao com itens datados, para alimentar as metricas
-- de tempo medio de execucao por tipo de servico.

INSERT INTO ordens_servico (
    id, id_cliente, id_veiculo, status, numero, observacoes,
    data_inicio_execucao, data_finalizacao, data_entrega,
    data_criacao, data_ultima_atualizacao
) VALUES
    ('70000000-0000-0000-0000-000000000001',
     '10000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000001',
     'ENTREGUE', 'OS-2026-00001', 'Revisao preventiva com servicos sequenciais.',
     now() - interval '6 hours', now() - interval '3 hours', now() - interval '2 hours',
     now() - interval '8 hours', now() - interval '2 hours'),
    ('70000000-0000-0000-0000-000000000002',
     '10000000-0000-0000-0000-000000000002', '20000000-0000-0000-0000-000000000002',
     'FINALIZADA', 'OS-2026-00002', 'Diagnostico e alinhamento.',
     now() - interval '5 hours', now() - interval '90 minutes', NULL,
     now() - interval '7 hours', now() - interval '90 minutes'),
    ('70000000-0000-0000-0000-000000000003',
     '10000000-0000-0000-0000-000000000003', '20000000-0000-0000-0000-000000000003',
     'EM_COMPOSICAO', 'OS-2026-00003', 'Levantamento tecnico em andamento.',
     NULL, NULL, NULL, now(), now())
ON CONFLICT (id) DO NOTHING;

INSERT INTO orcamentos (
    id, id_ordem_servico, valor, status, tipo, data_criacao, data_ultima_atualizacao
) VALUES
    ('80000000-0000-0000-0000-000000000001', '70000000-0000-0000-0000-000000000001',
     400.00, 'APROVADO', 'PRINCIPAL', now() - interval '7 hours', now() - interval '6 hours'),
    ('80000000-0000-0000-0000-000000000002', '70000000-0000-0000-0000-000000000002',
     370.00, 'APROVADO', 'PRINCIPAL', now() - interval '6 hours', now() - interval '5 hours')
ON CONFLICT (id) DO NOTHING;

INSERT INTO ordens_servico_itens (
    id, ordem_servico_id, ordem_item, descricao, valor, tipo,
    servico_id, peca_insumo_id, status_execucao,
    data_inicio_execucao, data_finalizacao
) VALUES
    -- OS 1 (ENTREGUE): troca de oleo (60 min) + alinhamento (90 min)
    ('71000000-0000-0000-0000-000000000001', '70000000-0000-0000-0000-000000000001',
     0, 'Troca de oleo e filtro', 180.00, 'SERVICO',
     '30000000-0000-0000-0000-000000000001', NULL, 'FINALIZADO',
     now() - interval '6 hours', now() - interval '5 hours'),
    ('71000000-0000-0000-0000-000000000002', '70000000-0000-0000-0000-000000000001',
     1, 'Alinhamento e balanceamento', 220.00, 'SERVICO',
     '30000000-0000-0000-0000-000000000004', NULL, 'FINALIZADO',
     now() - interval '5 hours', now() - interval '210 minutes'),
    -- OS 2 (FINALIZADA): diagnostico (45 min) + revisao de freios (105 min)
    ('71000000-0000-0000-0000-000000000003', '70000000-0000-0000-0000-000000000002',
     0, 'Diagnostico eletronico', 150.00, 'SERVICO',
     '30000000-0000-0000-0000-000000000003', NULL, 'FINALIZADO',
     now() - interval '5 hours', now() - interval '255 minutes'),
    ('71000000-0000-0000-0000-000000000004', '70000000-0000-0000-0000-000000000002',
     1, 'Revisao de freios', 320.00, 'SERVICO',
     '30000000-0000-0000-0000-000000000002', NULL, 'FINALIZADO',
     now() - interval '255 minutes', now() - interval '90 minutes'),
    -- OS 3 (EM_COMPOSICAO): servico pendente + peca
    ('71000000-0000-0000-0000-000000000005', '70000000-0000-0000-0000-000000000003',
     0, 'Higienizacao interna', 260.00, 'SERVICO',
     '30000000-0000-0000-0000-000000000005', NULL, 'PENDENTE', NULL, NULL),
    ('71000000-0000-0000-0000-000000000006', '70000000-0000-0000-0000-000000000003',
     1, 'Filtro de ar de cabine', 69.90, 'PECA',
     NULL, '40000000-0000-0000-0000-000000000005', NULL, NULL, NULL)
ON CONFLICT (id) DO NOTHING;

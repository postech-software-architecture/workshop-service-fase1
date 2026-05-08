ALTER TABLE movimentacoes_estoque
    ADD COLUMN ordem_servico_id UUID,
    ADD COLUMN orcamento_id UUID;

COMMENT ON COLUMN movimentacoes_estoque.ordem_servico_id IS 'Identificador da ordem de servico relacionada a movimentacao operacional de estoque.';
COMMENT ON COLUMN movimentacoes_estoque.orcamento_id IS 'Identificador do orcamento que originou a movimentacao de reserva ou liberacao.';

ALTER TABLE movimentacoes_estoque
    ADD CONSTRAINT fk_movimentacoes_estoque_ordem_servico
        FOREIGN KEY (ordem_servico_id) REFERENCES ordens_servico (id);

ALTER TABLE movimentacoes_estoque
    ADD CONSTRAINT fk_movimentacoes_estoque_orcamento
        FOREIGN KEY (orcamento_id) REFERENCES orcamentos (id);

CREATE INDEX idx_movimentacoes_estoque_ordem_servico_id
    ON movimentacoes_estoque (ordem_servico_id);

CREATE INDEX idx_movimentacoes_estoque_orcamento_id
    ON movimentacoes_estoque (orcamento_id);

COMMENT ON CONSTRAINT fk_movimentacoes_estoque_ordem_servico ON movimentacoes_estoque
    IS 'Garante rastreabilidade de movimentacoes operacionais por ordem de servico.';

COMMENT ON CONSTRAINT fk_movimentacoes_estoque_orcamento ON movimentacoes_estoque
    IS 'Garante rastreabilidade comercial de reservas e liberacoes por orcamento.';

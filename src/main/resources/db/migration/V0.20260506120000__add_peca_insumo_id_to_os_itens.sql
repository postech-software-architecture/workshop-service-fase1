ALTER TABLE ordens_servico_itens
    ADD COLUMN peca_insumo_id UUID NULL;

COMMENT ON COLUMN ordens_servico_itens.peca_insumo_id IS 'Identificador da peca do catalogo. Preenchido apenas para itens do tipo PECA para possibilitar rastreio de reservas de estoque.';

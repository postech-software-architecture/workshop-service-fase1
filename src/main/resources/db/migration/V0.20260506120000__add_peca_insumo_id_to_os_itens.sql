ALTER TABLE ordens_servico_itens
    ADD COLUMN peca_insumo_id UUID NULL;

COMMENT ON COLUMN ordens_servico_itens.peca_insumo_id IS 'Identificador da peca ou insumo do catalogo. Preenchido para itens dos tipos PECA e INSUMO para possibilitar rastreio de reservas de estoque.';

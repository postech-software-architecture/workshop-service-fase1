ALTER TABLE ordens_servico
    ADD CONSTRAINT fk_ordens_servico_clientes
        FOREIGN KEY (id_cliente) REFERENCES clientes(id) ON DELETE RESTRICT,
    ADD CONSTRAINT fk_ordens_servico_veiculos
        FOREIGN KEY (id_veiculo) REFERENCES veiculos(id) ON DELETE RESTRICT;

ALTER TABLE ordens_servico_itens
    ADD CONSTRAINT fk_ordens_servico_itens_pecas_insumos
        FOREIGN KEY (peca_insumo_id) REFERENCES pecas_insumos(id) ON DELETE RESTRICT;

ALTER TABLE historico_status_os
    ADD CONSTRAINT fk_historico_status_os_usuarios
        FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE RESTRICT;

CREATE INDEX ix_ordens_servico_itens_peca_insumo
    ON ordens_servico_itens (peca_insumo_id);

CREATE INDEX ix_historico_status_os_usuario
    ON historico_status_os (usuario_id);

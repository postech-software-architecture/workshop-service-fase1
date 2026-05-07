ALTER TABLE movimentacoes_estoque
    DROP CONSTRAINT chk_tipo_valido;

ALTER TABLE movimentacoes_estoque
    ADD CONSTRAINT chk_tipo_valido
        CHECK (tipo IN ('ENTRADA', 'SAIDA', 'AJUSTE', 'RESERVA', 'LIBERACAO'));

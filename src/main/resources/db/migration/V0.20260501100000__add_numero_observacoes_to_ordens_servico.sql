ALTER TABLE ordens_servico
    ADD COLUMN observacoes TEXT,
    ADD COLUMN numero VARCHAR(20);

WITH numerados AS (
    SELECT id,
           'OS-' || EXTRACT(YEAR FROM data_criacao)::TEXT || '-' ||
           LPAD(ROW_NUMBER() OVER (PARTITION BY EXTRACT(YEAR FROM data_criacao) ORDER BY data_criacao)::TEXT,
                5, '0') AS num
    FROM ordens_servico
    WHERE numero IS NULL
)
UPDATE ordens_servico
SET numero = numerados.num
FROM numerados
WHERE ordens_servico.id = numerados.id;

ALTER TABLE ordens_servico
    ALTER COLUMN numero SET NOT NULL;

CREATE UNIQUE INDEX ux_ordens_servico_numero ON ordens_servico (numero);

COMMENT ON COLUMN ordens_servico.numero IS 'Numero sequencial unico da OS no formato OS-{ANO}-{NNNNN}.';
COMMENT ON COLUMN ordens_servico.observacoes IS 'Observacoes registradas pelo atendente na recepcao do veiculo.';

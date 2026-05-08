ALTER TABLE ordens_servico
	ADD COLUMN data_inicio_execucao TIMESTAMP,
	ADD COLUMN data_finalizacao TIMESTAMP,
	ADD COLUMN data_entrega TIMESTAMP;

COMMENT ON COLUMN ordens_servico.data_inicio_execucao IS 'Data e hora em que a execucao tecnica da ordem de servico foi iniciada.';
COMMENT ON COLUMN ordens_servico.data_finalizacao IS 'Data e hora em que a execucao tecnica da ordem de servico foi finalizada.';
COMMENT ON COLUMN ordens_servico.data_entrega IS 'Data e hora em que o veiculo foi entregue ao cliente.';

CREATE TABLE historico_status_os (
	id UUID PRIMARY KEY,
	ordem_servico_id UUID NOT NULL,
	status_anterior VARCHAR(40) NOT NULL,
	status_novo VARCHAR(40) NOT NULL,
	data_transicao TIMESTAMP NOT NULL,
	usuario_id UUID NOT NULL,
	usuario_username VARCHAR(120) NOT NULL,
	data_criacao TIMESTAMP NOT NULL,
	data_ultima_atualizacao TIMESTAMP NOT NULL,
	data_remocao TIMESTAMP,
	CONSTRAINT fk_historico_status_os_ordem_servico
		FOREIGN KEY (ordem_servico_id) REFERENCES ordens_servico(id),
	CONSTRAINT chk_historico_status_os_status_distinto
		CHECK (status_anterior <> status_novo)
);

CREATE INDEX ix_historico_status_os_ordem_data
	ON historico_status_os (ordem_servico_id, data_transicao);

COMMENT ON TABLE historico_status_os IS 'Linha do tempo auditavel de transicoes de status da ordem de servico.';
COMMENT ON COLUMN historico_status_os.ordem_servico_id IS 'Identificador da ordem de servico vinculada ao historico.';
COMMENT ON COLUMN historico_status_os.status_anterior IS 'Status da ordem de servico antes da transicao.';
COMMENT ON COLUMN historico_status_os.status_novo IS 'Status da ordem de servico apos a transicao.';
COMMENT ON COLUMN historico_status_os.data_transicao IS 'Data e hora em que a transicao foi registrada.';
COMMENT ON COLUMN historico_status_os.usuario_id IS 'Identificador do usuario responsavel pela transicao.';
COMMENT ON COLUMN historico_status_os.usuario_username IS 'Username do usuario responsavel pela transicao.';

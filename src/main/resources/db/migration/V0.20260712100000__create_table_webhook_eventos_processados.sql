CREATE TABLE webhook_eventos_processados (
	id_evento VARCHAR(120) PRIMARY KEY,
	origem VARCHAR(120),
	data_processamento TIMESTAMP NOT NULL
);

COMMENT ON TABLE webhook_eventos_processados IS 'Eventos de webhook ja processados, para idempotencia (rejeitar reentregas do mesmo idEvento).';
COMMENT ON COLUMN webhook_eventos_processados.id_evento IS 'Identificador unico do evento enviado pelo sistema externo.';
COMMENT ON COLUMN webhook_eventos_processados.origem IS 'Origem/canal do evento (informativo).';
COMMENT ON COLUMN webhook_eventos_processados.data_processamento IS 'Momento em que o evento foi registrado como processado.';

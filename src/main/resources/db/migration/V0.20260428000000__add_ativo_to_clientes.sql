ALTER TABLE clientes ADD COLUMN ativo BOOLEAN NOT NULL DEFAULT true;

COMMENT ON COLUMN clientes.ativo IS 'Indica se o cliente esta ativo (true) ou removido logicamente (false).';

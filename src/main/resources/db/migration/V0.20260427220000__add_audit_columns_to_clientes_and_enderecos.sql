-- Adiciona coluna de remocao logica na tabela clientes
ALTER TABLE clientes ADD COLUMN IF NOT EXISTS data_remocao TIMESTAMP;

COMMENT ON COLUMN clientes.data_remocao IS 'Data e hora em que o cliente foi removido logicamente.';

-- Adiciona colunas de auditoria na tabela enderecos
ALTER TABLE enderecos ADD COLUMN IF NOT EXISTS data_criacao TIMESTAMP NOT NULL DEFAULT NOW();
ALTER TABLE enderecos ADD COLUMN IF NOT EXISTS data_ultima_atualizacao TIMESTAMP NOT NULL DEFAULT NOW();
ALTER TABLE enderecos ADD COLUMN IF NOT EXISTS data_remocao TIMESTAMP;

COMMENT ON COLUMN enderecos.data_criacao IS 'Data e hora de criacao do registro.';
COMMENT ON COLUMN enderecos.data_ultima_atualizacao IS 'Data e hora da ultima atualizacao do registro.';
COMMENT ON COLUMN enderecos.data_remocao IS 'Data e hora em que o endereco foi removido logicamente.';

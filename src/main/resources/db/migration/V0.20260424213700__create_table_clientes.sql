CREATE TABLE clientes (
    id UUID PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    documento VARCHAR(20) NOT NULL UNIQUE,
    email VARCHAR(255),
    telefone VARCHAR(20),
    data_nascimento_fundacao DATE,
    observacoes TEXT,
    data_criacao TIMESTAMP NOT NULL,
    data_ultima_atualizacao TIMESTAMP NOT NULL
);

COMMENT ON TABLE clientes IS 'Tabela que armazena os clientes da oficina (Pessoa Física ou Jurídica).';
COMMENT ON COLUMN clientes.id IS 'Identificador único do cliente UUID.';
COMMENT ON COLUMN clientes.nome IS 'Nome completo ou Razão Social.';
COMMENT ON COLUMN clientes.documento IS 'CPF ou CNPJ apenas em formato numérico.';
COMMENT ON COLUMN clientes.email IS 'Endereço de e-mail (opcional, desde que tenha telefone).';
COMMENT ON COLUMN clientes.telefone IS 'Telefone de contato (opcional, desde que tenha email).';
COMMENT ON COLUMN clientes.data_nascimento_fundacao IS 'Data de nascimento para PF ou fundação para PJ.';
COMMENT ON COLUMN clientes.observacoes IS 'Observações gerais sobre o cliente.';
COMMENT ON COLUMN clientes.data_criacao IS 'Data e hora da criação do registro.';
COMMENT ON COLUMN clientes.data_ultima_atualizacao IS 'Data e hora da última modificação do registro.';

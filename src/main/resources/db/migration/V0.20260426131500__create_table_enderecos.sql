CREATE TABLE enderecos (
    id UUID PRIMARY KEY,
    cliente_id UUID NOT NULL UNIQUE,
    logradouro VARCHAR(255) NOT NULL,
    numero VARCHAR(20),
    complemento VARCHAR(100),
    bairro VARCHAR(100),
    cidade VARCHAR(100) NOT NULL,
    estado VARCHAR(50) NOT NULL,
    cep VARCHAR(10),
    CONSTRAINT fk_endereco_cliente FOREIGN KEY (cliente_id) REFERENCES clientes(id) ON DELETE CASCADE
);

COMMENT ON TABLE enderecos IS 'Tabela que armazena os endereços dos clientes da oficina.';
COMMENT ON COLUMN enderecos.id IS 'ID único do endereço.';
COMMENT ON COLUMN enderecos.cliente_id IS 'ID do cliente associado (FK).';
COMMENT ON COLUMN enderecos.logradouro IS 'Nome da rua/avenida.';
COMMENT ON COLUMN enderecos.numero IS 'Número do endereço.';
COMMENT ON COLUMN enderecos.complemento IS 'Complemento do endereço.';
COMMENT ON COLUMN enderecos.bairro IS 'Bairro.';
COMMENT ON COLUMN enderecos.cidade IS 'Cidade.';
COMMENT ON COLUMN enderecos.estado IS 'Estado (UF).';
COMMENT ON COLUMN enderecos.cep IS 'CEP (apenas números).';

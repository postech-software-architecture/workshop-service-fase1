CREATE TABLE usuarios (
    id UUID PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    email VARCHAR(255),
    senha_hash VARCHAR(255) NOT NULL,
    cliente_id UUID,
    ativo BOOLEAN NOT NULL DEFAULT true,
    bloqueado BOOLEAN NOT NULL DEFAULT false,
    data_criacao TIMESTAMP NOT NULL DEFAULT now(),
    data_ultima_atualizacao TIMESTAMP NOT NULL DEFAULT now(),
    data_remocao TIMESTAMP,

    CONSTRAINT uk_usuarios_username UNIQUE (username),
    CONSTRAINT uk_usuarios_email UNIQUE (email),
    CONSTRAINT uk_usuarios_cliente UNIQUE (cliente_id),
    CONSTRAINT fk_usuarios_clientes FOREIGN KEY (cliente_id) REFERENCES clientes(id) ON DELETE RESTRICT
);

CREATE TABLE usuarios_roles (
    usuario_id UUID NOT NULL,
    role VARCHAR(30) NOT NULL,

    CONSTRAINT pk_usuarios_roles PRIMARY KEY (usuario_id, role),
    CONSTRAINT fk_usuarios_roles_usuarios FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    CONSTRAINT chk_usuarios_roles_validos CHECK (role IN ('ADMINISTRADOR', 'ATENDENTE', 'MECANICO', 'CLIENTE'))
);

CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY,
    token VARCHAR(255) NOT NULL,
    usuario_id UUID NOT NULL,
    data_expiracao TIMESTAMP NOT NULL,
    revogado BOOLEAN NOT NULL DEFAULT false,
    data_revogacao TIMESTAMP,
    data_criacao TIMESTAMP NOT NULL DEFAULT now(),
    data_ultima_atualizacao TIMESTAMP NOT NULL DEFAULT now(),
    data_remocao TIMESTAMP,

    CONSTRAINT uk_refresh_tokens_token UNIQUE (token),
    CONSTRAINT fk_refresh_tokens_usuarios FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
);

CREATE INDEX idx_usuarios_email ON usuarios(email);
CREATE INDEX idx_refresh_tokens_usuario ON refresh_tokens(usuario_id);
CREATE INDEX idx_refresh_tokens_token ON refresh_tokens(token);

COMMENT ON TABLE usuarios IS 'Contas autenticaveis do sistema de oficina';
COMMENT ON COLUMN usuarios.id IS 'Identificador unico da conta de usuario';
COMMENT ON COLUMN usuarios.username IS 'Identificador principal de login do usuario';
COMMENT ON COLUMN usuarios.email IS 'Email opcional utilizado como identificador alternativo de login';
COMMENT ON COLUMN usuarios.senha_hash IS 'Hash BCrypt da senha do usuario';
COMMENT ON COLUMN usuarios.cliente_id IS 'Cliente vinculado de forma obrigatoria para contas com papel CLIENTE';
COMMENT ON COLUMN usuarios.ativo IS 'Indica se a conta esta habilitada para autenticacao';
COMMENT ON COLUMN usuarios.bloqueado IS 'Indica se a conta foi bloqueada administrativamente';
COMMENT ON COLUMN usuarios.data_criacao IS 'Data de criacao do registro';
COMMENT ON COLUMN usuarios.data_ultima_atualizacao IS 'Data da ultima atualizacao do registro';
COMMENT ON COLUMN usuarios.data_remocao IS 'Data da remocao logica da conta, quando aplicavel';

COMMENT ON TABLE usuarios_roles IS 'Tabela de papeis de acesso associados a cada conta autenticavel';
COMMENT ON COLUMN usuarios_roles.usuario_id IS 'Conta autenticavel proprietaria do papel de acesso';
COMMENT ON COLUMN usuarios_roles.role IS 'Papel de acesso atribuido a conta';

COMMENT ON TABLE refresh_tokens IS 'Credenciais persistidas para renovacao de sessao autenticada';
COMMENT ON COLUMN refresh_tokens.id IS 'Identificador unico da credencial de renovacao';
COMMENT ON COLUMN refresh_tokens.token IS 'Valor opaco e unico da credencial de renovacao';
COMMENT ON COLUMN refresh_tokens.usuario_id IS 'Conta autenticavel dona da credencial de renovacao';
COMMENT ON COLUMN refresh_tokens.data_expiracao IS 'Prazo maximo para uso da credencial de renovacao';
COMMENT ON COLUMN refresh_tokens.revogado IS 'Indica se a credencial foi invalidada antes do vencimento';
COMMENT ON COLUMN refresh_tokens.data_revogacao IS 'Data em que a credencial foi revogada';
COMMENT ON COLUMN refresh_tokens.data_criacao IS 'Data de criacao da credencial';
COMMENT ON COLUMN refresh_tokens.data_ultima_atualizacao IS 'Data da ultima atualizacao da credencial';
COMMENT ON COLUMN refresh_tokens.data_remocao IS 'Data de remocao logica da credencial, quando aplicavel';

-- Identidade persistida para transicoes disparadas por integracoes maquina-a-maquina.
-- O BCrypt abaixo nao possui credencial correspondente provisionada. Somado a
-- ativo=false e bloqueado=true, ele mantem esta conta tecnica nao autenticavel.
INSERT INTO usuarios (
    id, username, email, senha_hash, cliente_id, ativo, bloqueado,
    data_criacao, data_ultima_atualizacao
) VALUES (
    '70000000-0000-0000-0000-000000000001', 'system.webhook', NULL,
    '$2a$12$gITEG.iBxvaOrg7TI69EuuOT0vN7dZdWpjRvFF3ohF0RdWyTJpUAB',
    NULL, false, true, now(), now()
);

-- O papel tecnico mantem a conta compativel com o modelo de dominio sem conceder
-- acesso aos endpoints, que autorizam apenas os papeis humanos existentes.
ALTER TABLE usuarios_roles
    DROP CONSTRAINT chk_usuarios_roles_validos,
    ADD CONSTRAINT chk_usuarios_roles_validos
        CHECK (role IN ('ADMINISTRADOR', 'ATENDENTE', 'MECANICO', 'CLIENTE', 'SISTEMA'));

INSERT INTO usuarios_roles (usuario_id, role) VALUES
    ('70000000-0000-0000-0000-000000000001', 'SISTEMA');

-- Versoes anteriores geravam um UUID diferente para cada origem do webhook. A
-- origem continua preservada em usuario_username, enquanto a FK passa a apontar
-- para a identidade tecnica unica.
UPDATE historico_status_os historico
SET usuario_id = '70000000-0000-0000-0000-000000000001'
WHERE historico.usuario_username LIKE 'webhook:%'
  AND NOT EXISTS (
      SELECT 1 FROM usuarios usuario WHERE usuario.id = historico.usuario_id
  );

ALTER TABLE ordens_servico
    ADD CONSTRAINT fk_ordens_servico_clientes
        FOREIGN KEY (id_cliente) REFERENCES clientes(id) ON DELETE RESTRICT,
    ADD CONSTRAINT fk_ordens_servico_veiculos
        FOREIGN KEY (id_veiculo) REFERENCES veiculos(id) ON DELETE RESTRICT;

ALTER TABLE ordens_servico_itens
    ADD CONSTRAINT fk_ordens_servico_itens_pecas_insumos
        FOREIGN KEY (peca_insumo_id) REFERENCES pecas_insumos(id) ON DELETE RESTRICT;

ALTER TABLE historico_status_os
    ADD CONSTRAINT fk_historico_status_os_usuarios
        FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE RESTRICT;

CREATE INDEX ix_ordens_servico_itens_peca_insumo
    ON ordens_servico_itens (peca_insumo_id);

CREATE INDEX ix_historico_status_os_usuario
    ON historico_status_os (usuario_id);

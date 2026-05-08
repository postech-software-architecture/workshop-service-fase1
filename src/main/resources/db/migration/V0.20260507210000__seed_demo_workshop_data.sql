-- Dados de demonstracao para apresentacao das funcionalidades da oficina.
-- Senha de todas as contas: password

INSERT INTO clientes (
    id, nome, documento, email, telefone, data_nascimento_fundacao, observacoes,
    data_criacao, data_ultima_atualizacao, ativo
) VALUES
    ('10000000-0000-0000-0000-000000000001', 'Mariana Souza', '12345678909',
     'mariana.souza@example.com', '11987654321', '1990-04-18',
     'Cliente recorrente. Prefere contato por WhatsApp.',
     now(), now(), true),
    ('10000000-0000-0000-0000-000000000002', 'Carlos Almeida', '98765432100',
     'carlos.almeida@example.com', '11912345678', '1984-09-02',
     'Cliente corporativo com historico de manutencao preventiva.',
     now(), now(), true),
    ('10000000-0000-0000-0000-000000000003', 'TechLog Transportes Ltda', '11222333000181',
     'manutencao@techlog.example.com', '1133334444', '2015-01-12',
     'Frota leve de entregas urbanas.',
     now(), now(), true)
ON CONFLICT (id) DO NOTHING;

INSERT INTO enderecos (
    id, cliente_id, logradouro, numero, complemento, bairro, cidade, estado, cep
) VALUES
    ('11000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001',
     'Rua das Oficinas', '120', 'Apto 42', 'Vila Mariana', 'Sao Paulo', 'SP', '04101000'),
    ('11000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000002',
     'Avenida Paulista', '900', NULL, 'Bela Vista', 'Sao Paulo', 'SP', '01310000'),
    ('11000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000003',
     'Rua Logistica', '55', 'Galpao 3', 'Tambore', 'Barueri', 'SP', '06460000')
ON CONFLICT (id) DO NOTHING;

INSERT INTO veiculos (
    id, placa, marca, modelo, ano, cor, observacoes, ativo,
    data_criacao, data_ultima_atualizacao
) VALUES
    ('20000000-0000-0000-0000-000000000001', 'ABC1D23', 'Toyota', 'Corolla', 2020,
     'Prata', 'Veiculo usado para trajeto urbano diario.', true, now(), now()),
    ('20000000-0000-0000-0000-000000000002', 'BRA2E19', 'Honda', 'Civic', 2019,
     'Preto', 'Cliente relata consumo elevado.', true, now(), now()),
    ('20000000-0000-0000-0000-000000000003', 'FRT5A42', 'Fiat', 'Fiorino', 2022,
     'Branco', 'Veiculo de frota com alto uso diario.', true, now(), now())
ON CONFLICT (id) DO NOTHING;

INSERT INTO veiculos_clientes (
    veiculo_id, cliente_id, data_criacao, data_ultima_atualizacao
) VALUES
    ('20000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001', now(), now()),
    ('20000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000002', now(), now()),
    ('20000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000003', now(), now())
ON CONFLICT DO NOTHING;

INSERT INTO servicos (
    id, nome, descricao, valor, categoria, nivel_complexidade, garantia_dias,
    observacoes_tecnicas, ativo, data_criacao, data_ultima_atualizacao
) VALUES
    ('30000000-0000-0000-0000-000000000001', 'Troca de oleo e filtro',
     'Substituicao de oleo do motor e filtro de oleo conforme especificacao do fabricante.',
     180.00, 'PREVENTIVA', 'BAIXA', 30, 'Conferir viscosidade recomendada no manual.', true, now(), now()),
    ('30000000-0000-0000-0000-000000000002', 'Revisao de freios',
     'Inspecao do sistema de freios, limpeza, regulagem e substituicao de componentes quando necessario.',
     320.00, 'MECANICA', 'MEDIA', 90, 'Medir espessura das pastilhas e estado dos discos.', true, now(), now()),
    ('30000000-0000-0000-0000-000000000003', 'Diagnostico eletronico',
     'Leitura de modulos via scanner, analise de falhas e emissao de diagnostico tecnico.',
     150.00, 'ELETRICA', 'MEDIA', 15, 'Registrar codigos de falha antes de limpar memoria.', true, now(), now()),
    ('30000000-0000-0000-0000-000000000004', 'Alinhamento e balanceamento',
     'Ajuste de geometria da suspensao e balanceamento das rodas.',
     220.00, 'PREVENTIVA', 'BAIXA', 30, 'Conferir calibragem antes dos ajustes.', true, now(), now()),
    ('30000000-0000-0000-0000-000000000005', 'Higienizacao interna',
     'Limpeza tecnica do interior e sistema de ventilacao.',
     260.00, 'ESTETICA', 'BAIXA', 15, 'Evitar excesso de umidade em comandos eletricos.', true, now(), now())
ON CONFLICT (id) DO NOTHING;

INSERT INTO pecas_insumos (
    id, sku, nome, valor_unitario, estoque_minimo, unidade_medida, tipo_item,
    fornecedor, codigo_barras, marca, categoria, aplicacao, observacoes, ativo,
    data_criacao, data_ultima_atualizacao
) VALUES
    ('40000000-0000-0000-0000-000000000001', 'FIL-OLEO-001', 'Filtro de oleo motor 1.8/2.0',
     45.90, 5, 'UN', 'PECA', 'AutoPecas Brasil', '7890000000011', 'Tecfil',
     'Motor', 'Toyota Corolla, Honda Civic e similares', 'Item comum em revisoes preventivas.',
     true, now(), now()),
    ('40000000-0000-0000-0000-000000000002', 'OLEO-5W30-SN',
     'Oleo sintetico 5W30 SN',
     42.50, 20, 'L', 'INSUMO', 'LubriMax', '7890000000028', 'LubriMax',
     'Lubrificantes', 'Motores flex e gasolina compativeis com 5W30',
     'Controlar validade do lote.',
     true, now(), now()),
    ('40000000-0000-0000-0000-000000000003', 'PAST-FREIO-DIANTEIRA',
     'Pastilha de freio dianteira',
     189.90, 4, 'UN', 'PECA', 'Freios Prime', '7890000000035', 'Fras-le',
     'Freios', 'Aplicacao em sedans medios', 'Verificar lado e encaixe antes da baixa.',
     true, now(), now()),
    ('40000000-0000-0000-0000-000000000004', 'FLUIDO-FREIO-DOT4',
     'Fluido de freio DOT 4',
     38.90, 6, 'UN', 'INSUMO', 'Quimica Auto', '7890000000042', 'Varga',
     'Freios', 'Sistemas hidraulicos DOT 4', 'Manter embalagem vedada.',
     true, now(), now()),
    ('40000000-0000-0000-0000-000000000005', 'FILTRO-AR-CABINE',
     'Filtro de ar de cabine',
     69.90, 5, 'UN', 'PECA', 'AutoPecas Brasil', '7890000000059', 'Wega',
     'Ar condicionado', 'Veiculos leves diversos', 'Usado em higienizacao e revisao.',
     true, now(), now()),
    ('40000000-0000-0000-0000-000000000006', 'PALHETA-22-18',
     'Jogo de palhetas 22/18 polegadas',
     95.00, 3, 'UN', 'PECA', 'Acessorios Sul', '7890000000066', 'Bosch',
     'Acessorios', 'Para-brisa de veiculos leves', 'Troca rapida na recepcao.',
     true, now(), now())
ON CONFLICT (id) DO NOTHING;

INSERT INTO estoques (
    id, peca_insumo_id, localizacao, quantidade, ativo, data_criacao, data_ultima_atualizacao
) VALUES
    ('50000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000001', 'Prateleira A1', 12, true, now(), now()),
    ('50000000-0000-0000-0000-000000000002', '40000000-0000-0000-0000-000000000002', 'Tambor L1', 48, true, now(), now()),
    ('50000000-0000-0000-0000-000000000003', '40000000-0000-0000-0000-000000000003', 'Prateleira B2', 8, true, now(), now()),
    ('50000000-0000-0000-0000-000000000004', '40000000-0000-0000-0000-000000000004', 'Armario Quimicos', 10, true, now(), now()),
    ('50000000-0000-0000-0000-000000000005', '40000000-0000-0000-0000-000000000005', 'Prateleira C1', 9, true, now(), now()),
    ('50000000-0000-0000-0000-000000000006', '40000000-0000-0000-0000-000000000006', 'Prateleira A3', 6, true, now(), now())
ON CONFLICT (id) DO NOTHING;

INSERT INTO movimentacoes_estoque (
    id, estoque_id, tipo, quantidade, quantidade_anterior, quantidade_posterior,
    motivo, data_movimentacao, data_criacao
) VALUES
    ('51000000-0000-0000-0000-000000000001', '50000000-0000-0000-0000-000000000001', 'ENTRADA', 12, 0, 12, 'Carga inicial de demonstracao', now(), now()),
    ('51000000-0000-0000-0000-000000000002', '50000000-0000-0000-0000-000000000002', 'ENTRADA', 48, 0, 48, 'Carga inicial de demonstracao', now(), now()),
    ('51000000-0000-0000-0000-000000000003', '50000000-0000-0000-0000-000000000003', 'ENTRADA', 8, 0, 8, 'Carga inicial de demonstracao', now(), now()),
    ('51000000-0000-0000-0000-000000000004', '50000000-0000-0000-0000-000000000004', 'ENTRADA', 10, 0, 10, 'Carga inicial de demonstracao', now(), now()),
    ('51000000-0000-0000-0000-000000000005', '50000000-0000-0000-0000-000000000005', 'ENTRADA', 9, 0, 9, 'Carga inicial de demonstracao', now(), now()),
    ('51000000-0000-0000-0000-000000000006', '50000000-0000-0000-0000-000000000006', 'ENTRADA', 6, 0, 6, 'Carga inicial de demonstracao', now(), now())
ON CONFLICT (id) DO NOTHING;

INSERT INTO usuarios (
    id, username, email, senha_hash, cliente_id, ativo, bloqueado,
    data_criacao, data_ultima_atualizacao
) VALUES
    ('60000000-0000-0000-0000-000000000001', 'admin.demo', 'admin.demo@example.com',
     '$2a$12$gITEG.iBxvaOrg7TI69EuuOT0vN7dZdWpjRvFF3ohF0RdWyTJpUAy',
     NULL, true, false, now(), now()),
    ('60000000-0000-0000-0000-000000000002', 'atendente.demo', 'atendente.demo@example.com',
     '$2a$12$gITEG.iBxvaOrg7TI69EuuOT0vN7dZdWpjRvFF3ohF0RdWyTJpUAy',
     NULL, true, false, now(), now()),
    ('60000000-0000-0000-0000-000000000003', 'mecanico.demo', 'mecanico.demo@example.com',
     '$2a$12$gITEG.iBxvaOrg7TI69EuuOT0vN7dZdWpjRvFF3ohF0RdWyTJpUAy',
     NULL, true, false, now(), now()),
    ('60000000-0000-0000-0000-000000000004', 'cliente.mariana', 'cliente.mariana@example.com',
     '$2a$12$gITEG.iBxvaOrg7TI69EuuOT0vN7dZdWpjRvFF3ohF0RdWyTJpUAy',
     '10000000-0000-0000-0000-000000000001', true, false, now(), now())
ON CONFLICT (id) DO NOTHING;

INSERT INTO usuarios_roles (usuario_id, role) VALUES
    ('60000000-0000-0000-0000-000000000001', 'ADMINISTRADOR'),
    ('60000000-0000-0000-0000-000000000002', 'ATENDENTE'),
    ('60000000-0000-0000-0000-000000000003', 'MECANICO'),
    ('60000000-0000-0000-0000-000000000004', 'CLIENTE')
ON CONFLICT DO NOTHING;

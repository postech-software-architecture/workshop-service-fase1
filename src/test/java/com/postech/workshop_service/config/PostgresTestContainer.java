package com.postech.workshop_service.config;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public abstract class PostgresTestContainer {

	public static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:15-alpine")
		.withDatabaseName("workshop_test")
		.withUsername("test")
		.withPassword("test");

	static {
		postgreSQLContainer.start();
	}

	@Autowired
	private JdbcTemplate jdbcTemplate;

	/** Conta tecnica criada pela migration de integridade da W3. */
	private static final String USUARIO_TECNICO_ID = "70000000-0000-0000-0000-000000000001";

	@BeforeEach
	void cleanDatabase() {
		jdbcTemplate.execute(
				"TRUNCATE TABLE refresh_tokens, usuarios_roles, usuarios, historico_status_os, orcamentos_itens, orcamentos, ordens_servico_itens, ordens_servico, movimentacoes_estoque, estoques, pecas_insumos, veiculos_clientes, veiculos, enderecos, clientes, servicos RESTART IDENTITY CASCADE");
		restaurarUsuarioTecnico();
	}

	/**
	 * O TRUNCATE acima limpa {@code usuarios}, mas a conta tecnica {@code system.webhook}
	 * e um invariante do schema: a partir da W3 ela e o alvo da FK
	 * {@code fk_historico_status_os_usuarios} para toda transicao disparada por
	 * integracao maquina-a-maquina. Recria-la aqui mantem o banco de teste equivalente ao
	 * estado pos-migration, em vez de um estado que o runtime nao aceita.
	 */
	private void restaurarUsuarioTecnico() {
		jdbcTemplate.update("""
				INSERT INTO usuarios (
				    id, username, email, senha_hash, cliente_id, ativo, bloqueado,
				    data_criacao, data_ultima_atualizacao
				) VALUES (?::uuid, 'system.webhook', NULL,
				    '$2a$12$gITEG.iBxvaOrg7TI69EuuOT0vN7dZdWpjRvFF3ohF0RdWyTJpUAB',
				    NULL, false, true, now(), now())
				ON CONFLICT (id) DO NOTHING
				""", USUARIO_TECNICO_ID);
		jdbcTemplate.update("""
				INSERT INTO usuarios_roles (usuario_id, role) VALUES (?::uuid, 'SISTEMA')
				ON CONFLICT DO NOTHING
				""", USUARIO_TECNICO_ID);
	}

	@DynamicPropertySource
	static void registerPgProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
		registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
		registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
		registry.add("spring.flyway.url", postgreSQLContainer::getJdbcUrl);
		registry.add("spring.flyway.user", postgreSQLContainer::getUsername);
		registry.add("spring.flyway.password", postgreSQLContainer::getPassword);
	}

}

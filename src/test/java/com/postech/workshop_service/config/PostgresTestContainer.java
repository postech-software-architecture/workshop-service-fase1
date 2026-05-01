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

	@BeforeEach
	void cleanDatabase() {
		jdbcTemplate.execute(
				"TRUNCATE TABLE movimentacoes_estoque, estoques, pecas_insumos, veiculos_clientes, veiculos, enderecos, clientes RESTART IDENTITY CASCADE");
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

package com.postech.workshop_service.infrastructure.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers
class DatabaseIntegrityMigrationIT {

	private static final List<String> EXPECTED_MIGRATIONS = List.of("0.20260424213700", "0.20260426131500",
			"0.20260426140000", "0.20260427220000", "0.20260428000000", "0.20260428120000", "0.20260429100000",
			"0.20260429101000", "0.20260429220000", "0.20260501100000", "0.20260501190000", "0.20260506120000",
			"0.20260506130000", "0.20260507100000", "0.20260507101000", "0.20260507210000", "0.20260507230000",
			"0.20260511215148", "0.20260712100000", "0.20260912185705");

	private static final Set<String> EXPECTED_INDEXES = Set.of("ix_ordens_servico_cliente", "ix_ordens_servico_veiculo",
			"ix_ordens_servico_itens_servico", "ix_historico_status_os_ordem_data",
			"ix_ordens_servico_itens_peca_insumo", "ix_historico_status_os_usuario");

	@Container
	private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:15-alpine")
		.withDatabaseName("workshop_integrity")
		.withUsername("test")
		.withPassword("test");

	private Connection connection;

	@BeforeAll
	static void migrateDatabase() {
		Flyway flyway = Flyway.configure()
			.dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
			.load();
		flyway.migrate();
		flyway.validate();
	}

	@BeforeEach
	void openTransaction() throws SQLException {
		connection = POSTGRES.createConnection("");
		connection.setAutoCommit(false);
	}

	@AfterEach
	void rollbackFixtures() throws SQLException {
		if (connection != null) {
			connection.rollback();
			connection.close();
		}
	}

	@Test
	void appliesTheCompleteFlywayHistoryAndDemoSeed() throws SQLException {
		assertThat(queryStrings("SELECT version FROM flyway_schema_history WHERE success = true "
				+ "AND type = 'SQL' ORDER BY installed_rank"))
			.containsExactlyElementsOf(EXPECTED_MIGRATIONS);

		assertThat(queryInt("SELECT count(*) FROM clientes WHERE id::text LIKE '10000000-%'")).isEqualTo(3);
		assertThat(queryInt("SELECT count(*) FROM veiculos WHERE id::text LIKE '20000000-%'")).isEqualTo(3);
		assertThat(queryInt("SELECT count(*) FROM servicos WHERE id::text LIKE '30000000-%'")).isEqualTo(5);
		assertThat(queryInt("SELECT count(*) FROM pecas_insumos WHERE id::text LIKE '40000000-%'")).isEqualTo(6);
		assertThat(queryInt("SELECT count(*) FROM usuarios WHERE id::text LIKE '60000000-%'")).isEqualTo(4);
	}

	@Test
	void createsAndValidatesTheFourRestrictiveForeignKeys() throws SQLException {
		Set<ForeignKey> foreignKeys = queryForeignKeys();

		assertThat(foreignKeys).containsExactlyInAnyOrder(
				new ForeignKey("fk_ordens_servico_clientes", "ordens_servico", "id_cliente", "clientes", "id", true,
						"r"),
				new ForeignKey("fk_ordens_servico_veiculos", "ordens_servico", "id_veiculo", "veiculos", "id", true,
						"r"),
				new ForeignKey("fk_ordens_servico_itens_pecas_insumos", "ordens_servico_itens", "peca_insumo_id",
						"pecas_insumos", "id", true, "r"),
				new ForeignKey("fk_historico_status_os_usuarios", "historico_status_os", "usuario_id", "usuarios", "id",
						true, "r"));
	}

	@Test
	void keepsTheFourExistingSupportIndexesAndCreatesTheTwoNewOnes() throws SQLException {
		assertThat(queryStrings("SELECT indexname FROM pg_indexes WHERE schemaname = 'public' " + "AND indexname IN ('"
				+ String.join("','", EXPECTED_INDEXES) + "')"))
			.containsExactlyInAnyOrderElementsOf(EXPECTED_INDEXES);
	}

	@Test
	void rejectsOrphansInAllFourNewRelationships() throws SQLException {
		Fixtures fixtures = insertParentFixtures();
		UUID missingId = UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff");

		expectForeignKeyViolation(
				"INSERT INTO ordens_servico (id, id_cliente, id_veiculo, status, numero, data_criacao, "
						+ "data_ultima_atualizacao) VALUES (?, ?, ?, 'RECEBIDA', 'OS-2099-00002', now(), now())",
				"fk_ordens_servico_clientes", UUID.randomUUID(), missingId, fixtures.vehicleId());
		expectForeignKeyViolation(
				"INSERT INTO ordens_servico (id, id_cliente, id_veiculo, status, numero, data_criacao, "
						+ "data_ultima_atualizacao) VALUES (?, ?, ?, 'RECEBIDA', 'OS-2099-00003', now(), now())",
				"fk_ordens_servico_veiculos", UUID.randomUUID(), fixtures.clientId(), missingId);
		expectForeignKeyViolation(
				"INSERT INTO ordens_servico_itens (id, ordem_servico_id, ordem_item, descricao, valor, tipo, "
						+ "peca_insumo_id) VALUES (?, ?, 1, 'Peca orfa', 10, 'PECA', ?)",
				"fk_ordens_servico_itens_pecas_insumos", UUID.randomUUID(), fixtures.orderId(), missingId);
		expectForeignKeyViolation(
				"INSERT INTO historico_status_os (id, ordem_servico_id, status_anterior, status_novo, "
						+ "data_transicao, usuario_id, usuario_username, data_criacao, data_ultima_atualizacao) "
						+ "VALUES (?, ?, 'RECEBIDA', 'EM_DIAGNOSTICO', now(), ?, 'missing', now(), now())",
				"fk_historico_status_os_usuarios", UUID.randomUUID(), fixtures.orderId(), missingId);
	}

	@Test
	void restrictsDeletionOfParentsReferencedByTheFourNewRelationships() throws SQLException {
		Fixtures fixtures = insertParentFixtures();
		execute("INSERT INTO ordens_servico_itens (id, ordem_servico_id, ordem_item, descricao, valor, tipo, "
				+ "peca_insumo_id) VALUES (?, ?, 1, 'Peca referenciada', 10, 'PECA', ?)", UUID.randomUUID(),
				fixtures.orderId(), fixtures.partId());
		execute("INSERT INTO historico_status_os (id, ordem_servico_id, status_anterior, status_novo, "
				+ "data_transicao, usuario_id, usuario_username, data_criacao, data_ultima_atualizacao) "
				+ "VALUES (?, ?, 'RECEBIDA', 'EM_DIAGNOSTICO', now(), ?, 'w3.user', now(), now())", UUID.randomUUID(),
				fixtures.orderId(), fixtures.userId());

		expectForeignKeyViolation("DELETE FROM clientes WHERE id = ?", "fk_ordens_servico_clientes",
				fixtures.clientId());
		expectForeignKeyViolation("DELETE FROM veiculos WHERE id = ?", "fk_ordens_servico_veiculos",
				fixtures.vehicleId());
		expectForeignKeyViolation("DELETE FROM pecas_insumos WHERE id = ?", "fk_ordens_servico_itens_pecas_insumos",
				fixtures.partId());
		expectForeignKeyViolation("DELETE FROM usuarios WHERE id = ?", "fk_historico_status_os_usuarios",
				fixtures.userId());
	}

	private Fixtures insertParentFixtures() throws SQLException {
		UUID clientId = UUID.randomUUID();
		UUID vehicleId = UUID.randomUUID();
		UUID orderId = UUID.randomUUID();
		UUID partId = UUID.randomUUID();
		UUID userId = UUID.randomUUID();

		execute("INSERT INTO clientes (id, nome, documento, data_criacao, data_ultima_atualizacao) "
				+ "VALUES (?, 'Cliente W3', ?, now(), now())", clientId, numericIdentifier(clientId));
		execute("INSERT INTO veiculos (id, placa, marca, modelo, ano, ativo, data_criacao, data_ultima_atualizacao) "
				+ "VALUES (?, ?, 'Marca', 'Modelo', 2026, true, now(), now())", vehicleId, plate(vehicleId));
		execute("INSERT INTO ordens_servico (id, id_cliente, id_veiculo, status, numero, data_criacao, "
				+ "data_ultima_atualizacao) VALUES (?, ?, ?, 'RECEBIDA', ?, now(), now())", orderId, clientId,
				vehicleId, "OS-W3-" + orderId.toString().substring(0, 8));
		execute("INSERT INTO pecas_insumos (id, sku, nome, valor_unitario, unidade_medida, tipo_item) "
				+ "VALUES (?, ?, 'Peca W3', 10, 'UN', 'PECA')", partId, "W3-" + partId.toString().substring(0, 8));
		execute("INSERT INTO usuarios (id, username, senha_hash) VALUES (?, ?, 'hash')", userId,
				"w3." + userId.toString().substring(0, 8));

		return new Fixtures(clientId, vehicleId, orderId, partId, userId);
	}

	private Set<ForeignKey> queryForeignKeys() throws SQLException {
		String sql = """
				SELECT c.conname,
				       child.relname,
				       child_column.attname,
				       parent.relname,
				       parent_column.attname,
				       c.convalidated,
				       c.confdeltype::text
				FROM pg_constraint c
				JOIN pg_class child ON child.oid = c.conrelid
				JOIN pg_class parent ON parent.oid = c.confrelid
				JOIN pg_attribute child_column
				  ON child_column.attrelid = c.conrelid AND child_column.attnum = c.conkey[1]
				JOIN pg_attribute parent_column
				  ON parent_column.attrelid = c.confrelid AND parent_column.attnum = c.confkey[1]
				WHERE c.contype = 'f'
				  AND c.conname IN ('fk_ordens_servico_clientes', 'fk_ordens_servico_veiculos',
				                    'fk_ordens_servico_itens_pecas_insumos', 'fk_historico_status_os_usuarios')
				""";
		try (Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(sql)) {
			Set<ForeignKey> result = new java.util.HashSet<>();
			while (resultSet.next()) {
				result.add(new ForeignKey(resultSet.getString(1), resultSet.getString(2), resultSet.getString(3),
						resultSet.getString(4), resultSet.getString(5), resultSet.getBoolean(6),
						resultSet.getString(7)));
			}
			return result;
		}
	}

	private List<String> queryStrings(String sql) throws SQLException {
		try (Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(sql)) {
			List<String> result = new java.util.ArrayList<>();
			while (resultSet.next()) {
				result.add(resultSet.getString(1));
			}
			return result;
		}
	}

	private int queryInt(String sql) throws SQLException {
		try (Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(sql)) {
			assertThat(resultSet.next()).isTrue();
			return resultSet.getInt(1);
		}
	}

	private void execute(String sql, Object... parameters) throws SQLException {
		try (PreparedStatement statement = connection.prepareStatement(sql)) {
			for (int index = 0; index < parameters.length; index++) {
				statement.setObject(index + 1, parameters[index]);
			}
			statement.executeUpdate();
		}
	}

	private void expectForeignKeyViolation(String sql, String constraintName, Object... parameters)
			throws SQLException {
		Savepoint savepoint = connection.setSavepoint();
		assertThatThrownBy(() -> execute(sql, parameters)).isInstanceOf(SQLException.class)
			.satisfies(error -> assertThat(((SQLException) error).getSQLState()).isEqualTo("23503"))
			.hasMessageContaining(constraintName);
		connection.rollback(savepoint);
	}

	private String numericIdentifier(UUID id) {
		return id.toString().replace("-", "").substring(0, 14);
	}

	private String plate(UUID id) {
		return id.toString().replace("-", "").substring(0, 7).toUpperCase();
	}

	private record ForeignKey(String name, String childTable, String childColumn, String parentTable,
			String parentColumn, boolean validated, String deleteAction) {
	}

	private record Fixtures(UUID clientId, UUID vehicleId, UUID orderId, UUID partId, UUID userId) {
	}

}

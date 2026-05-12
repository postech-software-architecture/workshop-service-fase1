package com.postech.workshop_service.api.controllers;

import com.postech.workshop_service.config.PostgresTestContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@WithMockUser(roles = "ADMINISTRADOR")
class MetricaControllerIT extends PostgresTestContainer {

	@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@BeforeEach
	void inserirDadosDeExecucao() {
		UUID clienteId = UUID.randomUUID();
		UUID veiculoId = UUID.randomUUID();
		UUID os1Id = UUID.randomUUID();
		UUID os2Id = UUID.randomUUID();

		jdbcTemplate.update(
				"""
						INSERT INTO clientes (id, nome, documento, email, telefone, ativo, data_criacao, data_ultima_atualizacao)
						VALUES (?, 'Cliente Teste', '12345678901', 'cliente@test.com', '11999999999', true, now(), now())
						""",
				clienteId);

		jdbcTemplate.update("""
				INSERT INTO veiculos (id, placa, marca, modelo, ano, cor, ativo, data_criacao, data_ultima_atualizacao)
				VALUES (?, 'TST0001', 'Toyota', 'Corolla', 2022, 'Prata', true, now(), now())
				""", veiculoId);

		jdbcTemplate.update(
				"""
						INSERT INTO ordens_servico (id, id_cliente, id_veiculo, status, numero, data_inicio_execucao, data_finalizacao, data_entrega, data_criacao, data_ultima_atualizacao)
						VALUES (?, ?, ?, 'ENTREGUE', 'OS-2026-00001', now() - interval '90 minutes', now() - interval '30 minutes', now(), now() - interval '2 hours', now())
						""",
				os1Id, clienteId, veiculoId);

		jdbcTemplate.update(
				"""
						INSERT INTO ordens_servico (id, id_cliente, id_veiculo, status, numero, data_inicio_execucao, data_finalizacao, data_entrega, data_criacao, data_ultima_atualizacao)
						VALUES (?, ?, ?, 'FINALIZADA', 'OS-2026-00002', now() - interval '50 minutes', now() - interval '10 minutes', null, now() - interval '1 hour', now())
						""",
				os2Id, clienteId, veiculoId);

		jdbcTemplate.update("""
				INSERT INTO ordens_servico_itens (ordem_servico_id, ordem_item, descricao, valor, tipo)
				VALUES (?, 0, 'Troca de oleo e filtro', 180.00, 'SERVICO')
				""", os1Id);

		jdbcTemplate.update("""
				INSERT INTO ordens_servico_itens (ordem_servico_id, ordem_item, descricao, valor, tipo)
				VALUES (?, 0, 'Revisao de freios', 320.00, 'SERVICO')
				""", os2Id);
	}

	@Test
	void shouldReturnTempoMedioGlobal() throws Exception {
		mockMvc.perform(get("/api/v1/metricas/tempo-medio-execucao"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.totalOrdens").value(2))
			.andExpect(jsonPath("$.tempoMedioExecucaoMinutos").isNumber())
			.andExpect(jsonPath("$.tempoMinimoExecucaoMinutos").isNumber())
			.andExpect(jsonPath("$.tempoMaximoExecucaoMinutos").isNumber());
	}

	@Test
	void shouldReturnTempoMedioPorTipoServico() throws Exception {
		mockMvc.perform(get("/api/v1/metricas/tempo-medio-execucao/por-tipo-servico"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$[0].descricaoServico").isString())
			.andExpect(jsonPath("$[0].totalExecucoes").value(1))
			.andExpect(jsonPath("$[0].tempoMedioExecucaoMinutos").isNumber());
	}

	@Test
	void shouldReturnEmptyMetricsWhenNoFinishedOrders() throws Exception {
		jdbcTemplate.execute("DELETE FROM ordens_servico_itens");
		jdbcTemplate.execute("DELETE FROM ordens_servico");

		mockMvc.perform(get("/api/v1/metricas/tempo-medio-execucao"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.totalOrdens").value(0))
			.andExpect(jsonPath("$.tempoMedioExecucaoMinutos").value(0.0));
	}

	@Test
	void shouldReturnEmptyListWhenNoServiceItems() throws Exception {
		jdbcTemplate.execute("DELETE FROM ordens_servico_itens");

		mockMvc.perform(get("/api/v1/metricas/tempo-medio-execucao/por-tipo-servico"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$").isEmpty());
	}

	@Test
	@WithMockUser(roles = "MECANICO")
	void shouldDenyAccessForNonAdmin() throws Exception {
		mockMvc.perform(get("/api/v1/metricas/tempo-medio-execucao")).andExpect(status().isForbidden());
	}

}

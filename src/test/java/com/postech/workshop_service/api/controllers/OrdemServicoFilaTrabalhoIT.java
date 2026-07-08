package com.postech.workshop_service.api.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.postech.workshop_service.api.dtos.CadastroClienteRequest;
import com.postech.workshop_service.api.dtos.CriarOrdemServicoRequest;
import com.postech.workshop_service.config.PostgresTestContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@WithMockUser(roles = { "ADMINISTRADOR", "ATENDENTE", "MECANICO" })
class OrdemServicoFilaTrabalhoIT extends PostgresTestContainer {

	@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@BeforeEach
	void limparDados() {
		jdbcTemplate.execute("TRUNCATE TABLE historico_status_os, orcamentos_itens, orcamentos, ordens_servico_itens, "
				+ "ordens_servico, servicos, pecas_insumos, estoques, movimentacoes_estoque RESTART IDENTITY CASCADE");
	}

	@Test
	void shouldOrderQueueByStatusPriorityThenByOldestFirst() throws Exception {
		LocalDateTime base = LocalDateTime.of(2026, 1, 1, 8, 0);

		// Mesma prioridade (EM_DIAGNOSTICO): a mais antiga deve vir primeiro.
		UUID diagAntiga = criarOs("Diag Antiga", "11144477735", "AAA1A11", "EM_DIAGNOSTICO", base.minusDays(5));
		UUID diagRecente = criarOs("Diag Recente", "52998224725", "BBB2B22", "EM_DIAGNOSTICO", base.minusDays(1));
		// Prioridades distintas.
		UUID emExecucao = criarOs("Execucao", "71428793860", "CCC3C33", "EM_EXECUCAO", base);
		UUID aguardando = criarOs("Aguardando", "98712345628", "DDD4D44", "AGUARDANDO_APROVACAO", base);
		UUID recebido = criarOs("Recebido", "39053344705", "EEE5E55", "RECEBIDO", base);
		// Encerradas nao aparecem.
		criarOs("Finalizada", "15350964038", "FFF6F66", "FINALIZADA", base);
		criarOs("Entregue", "23677128919", "GGG7G77", "ENTREGUE", base);
		criarOs("Cancelada", "47115873062", "HHH8H88", "CANCELADA", base);

		MvcResult result = mockMvc.perform(get("/api/v1/ordens-servico/fila-trabalho?pagina=0&tamanho=20"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.totalElementos").value(5))
			.andReturn();

		JsonNode conteudo = objectMapper.readTree(result.getResponse().getContentAsString()).get("conteudo");

		// Ordem esperada: EM_EXECUCAO, AGUARDANDO_APROVACAO, EM_DIAGNOSTICO(antiga),
		// EM_DIAGNOSTICO(recente), RECEBIDO.
		java.util.List<String> statusEsperados = java.util.List.of("EM_EXECUCAO", "AGUARDANDO_APROVACAO",
				"EM_DIAGNOSTICO", "EM_DIAGNOSTICO", "RECEBIDO");
		for (int i = 0; i < statusEsperados.size(); i++) {
			org.junit.jupiter.api.Assertions.assertEquals(statusEsperados.get(i),
					conteudo.get(i).get("status").asText(), "posicao " + i);
		}
		// Dentro de EM_DIAGNOSTICO, a mais antiga vem primeiro.
		org.junit.jupiter.api.Assertions.assertEquals(numeroDe(diagAntiga), conteudo.get(2).get("numero").asText());
		org.junit.jupiter.api.Assertions.assertEquals(numeroDe(diagRecente), conteudo.get(3).get("numero").asText());
		// Sanidade dos extremos.
		org.junit.jupiter.api.Assertions.assertEquals(numeroDe(emExecucao), conteudo.get(0).get("numero").asText());
		org.junit.jupiter.api.Assertions.assertEquals(numeroDe(aguardando), conteudo.get(1).get("numero").asText());
		org.junit.jupiter.api.Assertions.assertEquals(numeroDe(recebido), conteudo.get(4).get("numero").asText());
	}

	@Test
	void shouldKeepClosedOrdersAccessibleByIdButNotInQueue() throws Exception {
		UUID finalizada = criarOs("Final", "11144477735", "III9I99", "FINALIZADA", LocalDateTime.of(2026, 1, 1, 8, 0));

		mockMvc.perform(get("/api/v1/ordens-servico/fila-trabalho"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.totalElementos").value(0));

		// Continua acessivel diretamente por id (nao foi apagada do banco).
		mockMvc.perform(get("/api/v1/ordens-servico/{id}", finalizada))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("FINALIZADA"));
	}

	private UUID criarOs(String nome, String documento, String placa, String status, LocalDateTime dataCriacao)
			throws Exception {
		criarCliente(nome, documento);
		CriarOrdemServicoRequest request = CriarOrdemServicoRequest.builder()
			.clienteDocumento(documento)
			.veiculoPlaca(placa)
			.veiculo(CriarOrdemServicoRequest.DadosVeiculoRequest.builder()
				.marca("Toyota")
				.modelo("Corolla")
				.ano(2020)
				.build())
			.build();
		MvcResult result = mockMvc
			.perform(post("/api/v1/ordens-servico").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated())
			.andReturn();
		UUID id = UUID.fromString(objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText());
		// Forca status e data de criacao de forma deterministica para o cenario da fila.
		jdbcTemplate.update("UPDATE ordens_servico SET status = ?, data_criacao = ? WHERE id = ?", status,
				Timestamp.valueOf(dataCriacao), id);
		return id;
	}

	private void criarCliente(String nome, String documento) throws Exception {
		CadastroClienteRequest req = CadastroClienteRequest.builder()
			.nome(nome)
			.documento(documento)
			.email("contato@email.com")
			.build();
		mockMvc
			.perform(post("/api/v1/clientes").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(req)))
			.andExpect(status().isCreated());
	}

	private String numeroDe(UUID osId) throws Exception {
		MvcResult result = mockMvc.perform(get("/api/v1/ordens-servico/{id}", osId))
			.andExpect(status().isOk())
			.andReturn();
		return objectMapper.readTree(result.getResponse().getContentAsString()).get("numero").asText();
	}

}

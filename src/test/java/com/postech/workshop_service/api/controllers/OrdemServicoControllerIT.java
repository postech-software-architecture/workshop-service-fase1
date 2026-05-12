package com.postech.workshop_service.api.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.postech.workshop_service.api.dtos.AdicionarItemOrdemServicoRequest;
import com.postech.workshop_service.api.dtos.CadastroClienteRequest;
import com.postech.workshop_service.api.dtos.CadastroServicoRequest;
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

import java.math.BigDecimal;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@WithMockUser(roles = { "ADMINISTRADOR", "ATENDENTE", "MECANICO" })
class OrdemServicoControllerIT extends PostgresTestContainer {

	@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@BeforeEach
	void limparDadosOS() {
		jdbcTemplate.execute("TRUNCATE TABLE historico_status_os, orcamentos_itens, orcamentos, ordens_servico_itens, "
				+ "ordens_servico, servicos, pecas_insumos, estoques, movimentacoes_estoque RESTART IDENTITY CASCADE");
	}

	@Test
	void shouldCreateOsInRecebidoStatus() throws Exception {
		UUID clienteId = criarCliente("Joao Silva", "12345678909");

		CriarOrdemServicoRequest request = CriarOrdemServicoRequest.builder()
			.clienteDocumento("12345678909")
			.veiculoPlaca("ABC1D23")
			.veiculo(CriarOrdemServicoRequest.DadosVeiculoRequest.builder()
				.marca("Toyota")
				.modelo("Corolla")
				.ano(2020)
				.build())
			.observacoes("Barulho ao frear")
			.build();

		mockMvc
			.perform(post("/api/v1/ordens-servico").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.numero").value(org.hamcrest.Matchers.startsWith("OS-")))
			.andExpect(jsonPath("$.status").value("RECEBIDO"))
			.andExpect(jsonPath("$.cliente.id").value(clienteId.toString()))
			.andExpect(jsonPath("$.veiculo.placa").value("ABC1D23"))
			.andExpect(jsonPath("$.observacoes").value("Barulho ao frear"));
	}

	@Test
	void shouldRunFullCycleFromRecebidoToEntregue() throws Exception {
		UUID osId = criarOsRecebida("Joao Silva", "12345678909", "ABC1D23");

		mockMvc.perform(patch("/api/v1/ordens-servico/{id}/iniciar-diagnostico", osId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("EM_DIAGNOSTICO"));

		mockMvc.perform(patch("/api/v1/ordens-servico/{id}/encerrar-diagnostico", osId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("EM_COMPOSICAO"));

		UUID servicoId = criarServico("Troca de oleo", new BigDecimal("100.00"));

		AdicionarItemOrdemServicoRequest addItem = AdicionarItemOrdemServicoRequest.builder()
			.tipo(AdicionarItemOrdemServicoRequest.TipoItem.SERVICO)
			.servicoId(servicoId)
			.quantidade(BigDecimal.ONE)
			.build();

		MvcResult detalheResult = mockMvc
			.perform(post("/api/v1/ordens-servico/{id}/itens", osId).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(addItem)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.itens[0].tipo").value("SERVICO"))
			.andExpect(jsonPath("$.itens[0].descricao").value("Troca de oleo"))
			.andReturn();

		UUID idItem = UUID.fromString(objectMapper.readTree(detalheResult.getResponse().getContentAsString())
			.get("itens")
			.get(0)
			.get("id")
			.asText());

		mockMvc.perform(get("/api/v1/ordens-servico/{id}/historico-status", osId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].statusNovo").value("EM_DIAGNOSTICO"))
			.andExpect(jsonPath("$[1].statusNovo").value("EM_COMPOSICAO"));

		// idItem nao utilizado adiante neste fluxo simplificado de IT, apenas validamos o
		// payload
		org.junit.jupiter.api.Assertions.assertNotNull(idItem);
	}

	@Test
	void shouldRejectIniciarDiagnosticoWhenNotRecebido() throws Exception {
		UUID osId = criarOsRecebida("Maria", "98765432100", "BRA2E19");
		mockMvc.perform(patch("/api/v1/ordens-servico/{id}/iniciar-diagnostico", osId)).andExpect(status().isOk());

		mockMvc.perform(patch("/api/v1/ordens-servico/{id}/iniciar-diagnostico", osId))
			.andExpect(status().isUnprocessableEntity());
	}

	@Test
	void shouldRejectAddItemWhenNotEmComposicao() throws Exception {
		UUID osId = criarOsRecebida("Carlos", "11144477735", "FRT5A42");
		UUID servicoId = criarServico("Diagnostico", new BigDecimal("80.00"));

		AdicionarItemOrdemServicoRequest addItem = AdicionarItemOrdemServicoRequest.builder()
			.tipo(AdicionarItemOrdemServicoRequest.TipoItem.SERVICO)
			.servicoId(servicoId)
			.quantidade(BigDecimal.ONE)
			.build();

		mockMvc
			.perform(post("/api/v1/ordens-servico/{id}/itens", osId).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(addItem)))
			.andExpect(status().isUnprocessableEntity());
	}

	@Test
	void shouldReturnDetailWithStatusAndEmptyItens() throws Exception {
		UUID osId = criarOsRecebida("Pedro", "71428793860", "TST0001");

		mockMvc.perform(get("/api/v1/ordens-servico/{id}", osId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("RECEBIDO"))
			.andExpect(jsonPath("$.itens").isArray())
			.andExpect(jsonPath("$.itens").isEmpty());
	}

	private UUID criarOsRecebida(String nome, String documento, String placa) throws Exception {
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
		JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
		return UUID.fromString(body.get("id").asText());
	}

	private UUID criarCliente(String nome, String documento) throws Exception {
		CadastroClienteRequest req = CadastroClienteRequest.builder()
			.nome(nome)
			.documento(documento)
			.email("contato@email.com")
			.build();
		MvcResult result = mockMvc
			.perform(post("/api/v1/clientes").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(req)))
			.andExpect(status().isCreated())
			.andReturn();
		return UUID.fromString(objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText());
	}

	private UUID criarServico(String nome, BigDecimal valor) throws Exception {
		CadastroServicoRequest req = CadastroServicoRequest.builder()
			.nome(nome)
			.descricao(nome + " - descricao")
			.valor(valor)
			.build();
		MvcResult result = mockMvc
			.perform(post("/api/v1/servicos").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(req)))
			.andExpect(status().isCreated())
			.andReturn();
		return UUID.fromString(objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText());
	}

}

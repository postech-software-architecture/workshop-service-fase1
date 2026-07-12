package com.postech.workshop_service.api.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.postech.workshop_service.api.dtos.AdicionarItemOrdemServicoRequest;
import com.postech.workshop_service.api.dtos.CadastroClienteRequest;
import com.postech.workshop_service.api.dtos.CadastroServicoRequest;
import com.postech.workshop_service.api.dtos.CriarOrdemServicoRequest;
import com.postech.workshop_service.api.controllers.support.AutenticacaoTestSupport;
import com.postech.workshop_service.config.PostgresTestContainer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@TestPropertySource(properties = "webhook.orcamento.token=test-token")
class WebhookOrcamentoControllerIT extends PostgresTestContainer {

	private static final String TOKEN = "test-token";

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
				+ "ordens_servico, servicos, pecas_insumos, estoques, movimentacoes_estoque, "
				+ "webhook_eventos_processados RESTART IDENTITY CASCADE");
		// Setup (criar OS, avancar ate orcamento pendente) roda como staff autenticado
		// real;
		// as chamadas do proprio webhook usam .with(anonymous()) para validar o
		// permitAll.
		SecurityContextHolder.getContext().setAuthentication(AutenticacaoTestSupport.autenticacaoStaff());
	}

	@AfterEach
	void limparContexto() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void shouldApproveBudgetWithValidToken() throws Exception {
		UUID orcamentoId = criarOrcamentoPendente("Joao", "12345678909", "ABC1D23");

		// Chamada anonima (sem usuario) valida de fato o permitAll do webhook: a
		// autenticacao
		// e apenas pelo X-Webhook-Token.
		mockMvc
			.perform(post("/api/v1/webhooks/orcamentos/{id}/decisao", orcamentoId).with(anonymous())
				.header("X-Webhook-Token", TOKEN)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"decisao\":\"APROVADO\",\"origem\":\"teste\",\"idEvento\":\"" + UUID.randomUUID() + "\"}"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("APROVADO"));
	}

	@Test
	void shouldRejectBudgetWithValidToken() throws Exception {
		UUID orcamentoId = criarOrcamentoPendente("Maria", "98765432100", "BRA2E19");

		mockMvc
			.perform(post("/api/v1/webhooks/orcamentos/{id}/decisao", orcamentoId).with(anonymous())
				.header("X-Webhook-Token", TOKEN)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"decisao\":\"RECUSADO\",\"origem\":\"teste\"}"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("REJEITADO"));
	}

	@Test
	void shouldReturnUnauthorizedWithoutToken() throws Exception {
		UUID orcamentoId = criarOrcamentoPendente("Carlos", "11144477735", "FRT5A42");

		mockMvc
			.perform(post("/api/v1/webhooks/orcamentos/{id}/decisao", orcamentoId).with(anonymous())
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"decisao\":\"APROVADO\"}"))
			.andExpect(status().isUnauthorized());
	}

	@Test
	void shouldReturnUnauthorizedWithWrongToken() throws Exception {
		UUID orcamentoId = criarOrcamentoPendente("Ana", "71428793860", "TST0001");

		mockMvc
			.perform(post("/api/v1/webhooks/orcamentos/{id}/decisao", orcamentoId).with(anonymous())
				.header("X-Webhook-Token", "errado")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"decisao\":\"APROVADO\"}"))
			.andExpect(status().isUnauthorized());
	}

	@Test
	void shouldReturnNotFoundForUnknownBudget() throws Exception {
		mockMvc
			.perform(post("/api/v1/webhooks/orcamentos/{id}/decisao", UUID.randomUUID()).with(anonymous())
				.header("X-Webhook-Token", TOKEN)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"decisao\":\"APROVADO\"}"))
			.andExpect(status().isNotFound());
	}

	@Test
	void shouldRejectReplayOfSameEventWith409() throws Exception {
		UUID orcamentoId = criarOrcamentoPendente("Bruno", "52998224725", "XYZ9999");
		String payload = "{\"decisao\":\"APROVADO\",\"idEvento\":\"" + UUID.randomUUID() + "\"}";

		mockMvc
			.perform(post("/api/v1/webhooks/orcamentos/{id}/decisao", orcamentoId).with(anonymous())
				.header("X-Webhook-Token", TOKEN)
				.contentType(MediaType.APPLICATION_JSON)
				.content(payload))
			.andExpect(status().isOk());

		// Reentrega do MESMO idEvento -> 409 (idempotencia explicita), sem reaplicar o
		// efeito.
		mockMvc
			.perform(post("/api/v1/webhooks/orcamentos/{id}/decisao", orcamentoId).with(anonymous())
				.header("X-Webhook-Token", TOKEN)
				.contentType(MediaType.APPLICATION_JSON)
				.content(payload))
			.andExpect(status().isConflict());
	}

	@Test
	void shouldFallBackToStateGuardWhenNoEventIdOnReplay() throws Exception {
		UUID orcamentoId = criarOrcamentoPendente("Diana", "39053344705", "ZZZ1A11");
		// Sem idEvento: dedup por estado. Primeira aprova; segunda cai em 422
		// (nao-PENDENTE).
		String payload = "{\"decisao\":\"APROVADO\"}";

		mockMvc
			.perform(post("/api/v1/webhooks/orcamentos/{id}/decisao", orcamentoId).with(anonymous())
				.header("X-Webhook-Token", TOKEN)
				.contentType(MediaType.APPLICATION_JSON)
				.content(payload))
			.andExpect(status().isOk());

		mockMvc
			.perform(post("/api/v1/webhooks/orcamentos/{id}/decisao", orcamentoId).with(anonymous())
				.header("X-Webhook-Token", TOKEN)
				.contentType(MediaType.APPLICATION_JSON)
				.content(payload))
			.andExpect(status().isUnprocessableEntity());
	}

	private UUID criarOrcamentoPendente(String nome, String documento, String placa) throws Exception {
		criarCliente(nome, documento);
		UUID osId = criarOs(documento, placa);
		mockMvc.perform(patch("/api/v1/ordens-servico/{id}/iniciar-diagnostico", osId)).andExpect(status().isOk());
		mockMvc.perform(patch("/api/v1/ordens-servico/{id}/encerrar-diagnostico", osId)).andExpect(status().isOk());
		UUID servicoId = criarServico("Troca de oleo", new BigDecimal("120.00"));
		adicionarItemServico(osId, servicoId);
		mockMvc.perform(patch("/api/v1/ordens-servico/{id}/encerrar-composicao", osId)).andExpect(status().isOk());

		MvcResult result = mockMvc.perform(get("/api/v1/orcamentos/ordem-servico/{idOrdemServico}", osId))
			.andExpect(status().isOk())
			.andReturn();
		JsonNode orcamentos = objectMapper.readTree(result.getResponse().getContentAsString());
		return UUID.fromString(orcamentos.get(0).get("id").asText());
	}

	private UUID criarOs(String documento, String placa) throws Exception {
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
		return UUID.fromString(objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText());
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

	private void adicionarItemServico(UUID osId, UUID servicoId) throws Exception {
		AdicionarItemOrdemServicoRequest addItem = AdicionarItemOrdemServicoRequest.builder()
			.tipo(AdicionarItemOrdemServicoRequest.TipoItem.SERVICO)
			.servicoId(servicoId)
			.quantidade(BigDecimal.ONE)
			.build();
		mockMvc
			.perform(post("/api/v1/ordens-servico/{id}/itens", osId).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(addItem)))
			.andExpect(status().isOk());
	}

}

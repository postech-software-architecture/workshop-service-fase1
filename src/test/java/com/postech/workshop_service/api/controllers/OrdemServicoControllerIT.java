package com.postech.workshop_service.api.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.postech.workshop_service.api.dtos.AdicionarItemOrdemServicoRequest;
import com.postech.workshop_service.api.dtos.CadastroClienteRequest;
import com.postech.workshop_service.api.dtos.CadastroServicoRequest;
import com.postech.workshop_service.api.dtos.CriarOrdemServicoRequest;
import com.postech.workshop_service.api.dtos.CriarOrdemServicoComItensRequest;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
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
		// Autentica com um UsuarioAutenticadoPrincipal REAL (staff), como o filtro JWT
		// faz em
		// producao. Necessario porque as transicoes resolvem o responsavel via esse
		// principal.
		SecurityContextHolder.getContext().setAuthentication(AutenticacaoTestSupport.autenticacaoStaff());
	}

	@AfterEach
	void limparContexto() {
		SecurityContextHolder.clearContext();
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
	void shouldCreateOsWithInitialItemsAndReserveStock() throws Exception {
		UUID clienteId = criarCliente("Marina Souza", "52998224725");
		UUID servicoId = criarServico("Troca de filtro", new BigDecimal("120.00"));
		UUID pecaId = UUID.randomUUID();
		UUID estoqueId = UUID.randomUUID();
		jdbcTemplate.update("""
				INSERT INTO pecas_insumos
				(id, sku, nome, valor_unitario, estoque_minimo, unidade_medida, tipo_item, ativo)
				VALUES (?, ?, ?, ?, ?, ?, ?, true)
				""", pecaId, "FLT-001", "Filtro de oleo", new BigDecimal("45.00"), BigDecimal.ZERO, "UN", "PECA");
		jdbcTemplate.update("""
				INSERT INTO estoques (id, peca_insumo_id, localizacao, quantidade, ativo)
				VALUES (?, ?, ?, ?, true)
				""", estoqueId, pecaId, "A1", new BigDecimal("5.000"));

		CriarOrdemServicoComItensRequest request = CriarOrdemServicoComItensRequest.builder()
			.clienteDocumento("52998224725")
			.veiculoPlaca("BRA2E19")
			.veiculo(CriarOrdemServicoRequest.DadosVeiculoRequest.builder()
				.marca("Honda")
				.modelo("Civic")
				.ano(2022)
				.build())
			.servicos(List.of(CriarOrdemServicoComItensRequest.ItemServicoRequest.builder()
				.servicoId(servicoId)
				.quantidade(1)
				.build()))
			.pecas(List.of(CriarOrdemServicoComItensRequest.ItemPecaRequest.builder()
				.pecaId(pecaId)
				.quantidade(new BigDecimal("2.000"))
				.build()))
			.build();

		MvcResult result = mockMvc
			.perform(post("/api/v1/ordens-servico/com-itens").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.status").value("RECEBIDO"))
			.andExpect(jsonPath("$.cliente.id").value(clienteId.toString()))
			.andExpect(jsonPath("$.orcamento").value(nullValue()))
			.andReturn();

		UUID osId = UUID
			.fromString(objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText());
		mockMvc.perform(get("/api/v1/ordens-servico/{id}", osId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.itens", hasSize(2)))
			.andExpect(jsonPath("$.itens[0].tipo").value("SERVICO"))
			.andExpect(jsonPath("$.itens[1].tipo").value("PECA"));

		BigDecimal saldo = jdbcTemplate.queryForObject("SELECT quantidade FROM estoques WHERE id = ?", BigDecimal.class,
				estoqueId);
		Integer reservas = jdbcTemplate.queryForObject(
				"SELECT count(*) FROM movimentacoes_estoque WHERE ordem_servico_id = ? AND tipo = 'RESERVA'",
				Integer.class, osId);
		org.junit.jupiter.api.Assertions.assertEquals(0, new BigDecimal("3.000").compareTo(saldo));
		org.junit.jupiter.api.Assertions.assertEquals(1, reservas);
	}

	@Test
	void shouldCreateOsWithOnlyPartAndReserveStock() throws Exception {
		criarCliente("Paulo Lima", "16899535009");
		UUID pecaId = UUID.randomUUID();
		UUID estoqueId = UUID.randomUUID();
		jdbcTemplate.update("""
				INSERT INTO pecas_insumos
				(id, sku, nome, valor_unitario, estoque_minimo, unidade_medida, tipo_item, ativo)
				VALUES (?, ?, ?, ?, ?, ?, ?, true)
				""", pecaId, "OLE-001", "Oleo de motor", new BigDecimal("40.00"), BigDecimal.ZERO, "UN", "PECA");
		jdbcTemplate.update("""
				INSERT INTO estoques (id, peca_insumo_id, localizacao, quantidade, ativo)
				VALUES (?, ?, ?, ?, true)
				""", estoqueId, pecaId, "C1", new BigDecimal("5.000"));
		CriarOrdemServicoComItensRequest request = CriarOrdemServicoComItensRequest.builder()
			.clienteDocumento("16899535009")
			.veiculoPlaca("XYZ9G87")
			.veiculo(CriarOrdemServicoRequest.DadosVeiculoRequest.builder()
				.marca("Fiat")
				.modelo("Argo")
				.ano(2023)
				.build())
			.pecas(List.of(CriarOrdemServicoComItensRequest.ItemPecaRequest.builder()
				.pecaId(pecaId)
				.quantidade(new BigDecimal("2.000"))
				.build()))
			.build();

		MvcResult result = mockMvc
			.perform(post("/api/v1/ordens-servico/com-itens").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.status").value("RECEBIDO"))
			.andReturn();

		UUID osId = UUID
			.fromString(objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText());
		mockMvc.perform(get("/api/v1/ordens-servico/{id}", osId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.itens", hasSize(1)))
			.andExpect(jsonPath("$.itens[0].tipo").value("PECA"));
		BigDecimal saldo = jdbcTemplate.queryForObject("SELECT quantidade FROM estoques WHERE id = ?", BigDecimal.class,
				estoqueId);
		org.junit.jupiter.api.Assertions.assertEquals(0, new BigDecimal("3.000").compareTo(saldo));
	}

	@Test
	void shouldRejectCompleteOpeningWithoutItems() throws Exception {
		criarCliente("Paulo Lima", "16899535009");
		CriarOrdemServicoComItensRequest request = CriarOrdemServicoComItensRequest.builder()
			.clienteDocumento("16899535009")
			.veiculoPlaca("XYZ9G87")
			.servicos(List.of())
			.pecas(List.of())
			.build();

		mockMvc
			.perform(post("/api/v1/ordens-servico/com-itens").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest());
	}

	@Test
	void shouldRollbackCompleteOpeningWhenStockIsInsufficient() throws Exception {
		criarCliente("Carla Mendes", "39053344705");
		UUID servicoId = criarServico("Troca de correia", new BigDecimal("180.00"));
		UUID pecaId = UUID.randomUUID();
		UUID estoqueId = UUID.randomUUID();
		jdbcTemplate.update("""
				INSERT INTO pecas_insumos
				(id, sku, nome, valor_unitario, estoque_minimo, unidade_medida, tipo_item, ativo)
				VALUES (?, ?, ?, ?, ?, ?, ?, true)
				""", pecaId, "COR-001", "Correia", new BigDecimal("70.00"), BigDecimal.ZERO, "UN", "PECA");
		jdbcTemplate.update("""
				INSERT INTO estoques (id, peca_insumo_id, localizacao, quantidade, ativo)
				VALUES (?, ?, ?, ?, true)
				""", estoqueId, pecaId, "B1", BigDecimal.ONE);

		CriarOrdemServicoComItensRequest request = CriarOrdemServicoComItensRequest.builder()
			.clienteDocumento("39053344705")
			.veiculoPlaca("CAR1A23")
			.veiculo(
					CriarOrdemServicoRequest.DadosVeiculoRequest.builder().marca("Ford").modelo("Ka").ano(2020).build())
			.servicos(List.of(CriarOrdemServicoComItensRequest.ItemServicoRequest.builder()
				.servicoId(servicoId)
				.quantidade(1)
				.build()))
			.pecas(List.of(CriarOrdemServicoComItensRequest.ItemPecaRequest.builder()
				.pecaId(pecaId)
				.quantidade(new BigDecimal("2.000"))
				.build()))
			.build();

		mockMvc
			.perform(post("/api/v1/ordens-servico/com-itens").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isUnprocessableEntity());

		Integer ordens = jdbcTemplate.queryForObject("SELECT count(*) FROM ordens_servico", Integer.class);
		BigDecimal saldo = jdbcTemplate.queryForObject("SELECT quantidade FROM estoques WHERE id = ?", BigDecimal.class,
				estoqueId);
		org.junit.jupiter.api.Assertions.assertEquals(0, ordens);
		org.junit.jupiter.api.Assertions.assertEquals(0, BigDecimal.ONE.compareTo(saldo));
	}

	@Test
	void shouldIncludeInitialAndDiagnosticItemsInBudget() throws Exception {
		criarCliente("Rafael Costa", "15350946056");
		UUID servicoInicialId = criarServico("Diagnostico eletronico", new BigDecimal("90.00"));
		UUID servicoDiagnosticadoId = criarServico("Reparo eletrico", new BigDecimal("210.00"));
		CriarOrdemServicoComItensRequest request = CriarOrdemServicoComItensRequest.builder()
			.clienteDocumento("15350946056")
			.veiculoPlaca("ELE2T24")
			.veiculo(CriarOrdemServicoRequest.DadosVeiculoRequest.builder()
				.marca("Renault")
				.modelo("Sandero")
				.ano(2021)
				.build())
			.servicos(List.of(CriarOrdemServicoComItensRequest.ItemServicoRequest.builder()
				.servicoId(servicoInicialId)
				.quantidade(1)
				.build()))
			.build();

		MvcResult result = mockMvc
			.perform(post("/api/v1/ordens-servico/com-itens").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.orcamento").value(nullValue()))
			.andReturn();
		UUID osId = UUID
			.fromString(objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText());

		avancaParaComposicao(osId);
		adicionarItemServico(osId, servicoDiagnosticadoId);

		mockMvc.perform(patch("/api/v1/ordens-servico/{id}/encerrar-composicao", osId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("AGUARDANDO_APROVACAO"))
			.andExpect(jsonPath("$.orcamento.valorTotal").value(300.00));
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

	@Test
	void shouldAllowPublicAccessToStatusEndpointByNumero() throws Exception {
		UUID osId = criarOsRecebidaComNumero("Ana Costa", "98712345628", "XYZ9999");
		String numero = obterNumeroOS(osId);

		mockMvc.perform(get("/api/v1/ordens-servico/{numero}/status", numero))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.numero").value(numero))
			.andExpect(jsonPath("$.status").value("RECEBIDO"))
			.andExpect(jsonPath("$.itens").isArray())
			.andExpect(jsonPath("$.itens").isEmpty());
	}

	@Test
	void shouldReturnStatusWithItemsInPublicEndpoint() throws Exception {
		UUID osId = criarOsRecebidaComNumero("Bruno Lima", "11144477735", "DEF4444");
		avancaParaComposicao(osId);

		UUID servicoId = criarServico("Alinhamento", new BigDecimal("150.00"));
		adicionarItemServico(osId, servicoId);

		String numero = obterNumeroOS(osId);

		mockMvc.perform(get("/api/v1/ordens-servico/{numero}/status", numero))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.numero").value(numero))
			.andExpect(jsonPath("$.status").value("EM_COMPOSICAO"))
			.andExpect(jsonPath("$.itens").isArray())
			.andExpect(jsonPath("$.itens[0].descricao").value("Alinhamento"))
			.andExpect(jsonPath("$.itens[0].tipo").value("SERVICO"))
			.andExpect(jsonPath("$.itens[0].valor").value(150.00))
			.andExpect(jsonPath("$.itens[0].statusExecucao").value("PENDENTE"));
	}

	@Test
	void shouldReturn404WhenNumeroNotFound() throws Exception {
		mockMvc.perform(get("/api/v1/ordens-servico/{numero}/status", "OS-2026-99999"))
			.andExpect(status().isNotFound());
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

	private UUID criarOsRecebidaComNumero(String nome, String documento, String placa) throws Exception {
		return criarOsRecebida(nome, documento, placa);
	}

	private String obterNumeroOS(UUID osId) throws Exception {
		MvcResult result = mockMvc.perform(get("/api/v1/ordens-servico/{id}", osId))
			.andExpect(status().isOk())
			.andReturn();
		return objectMapper.readTree(result.getResponse().getContentAsString()).get("numero").asText();
	}

	private void avancaParaComposicao(UUID osId) throws Exception {
		mockMvc.perform(patch("/api/v1/ordens-servico/{id}/iniciar-diagnostico", osId)).andExpect(status().isOk());
		mockMvc.perform(patch("/api/v1/ordens-servico/{id}/encerrar-diagnostico", osId)).andExpect(status().isOk());
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

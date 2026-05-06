package com.postech.workshop_service.api.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.postech.workshop_service.api.dtos.CadastroPecaRequest;
import com.postech.workshop_service.api.dtos.CadastroClienteRequest;
import com.postech.workshop_service.api.dtos.CadastroServicoRequest;
import com.postech.workshop_service.api.dtos.CriarEstoqueRequest;
import com.postech.workshop_service.api.dtos.CriarOrdemServicoRequest;
import com.postech.workshop_service.config.PostgresTestContainer;
import com.postech.workshop_service.config.SecurityTestConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(SecurityTestConfiguration.class)
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
		jdbcTemplate
			.execute("TRUNCATE TABLE orcamentos_itens, orcamentos, ordens_servico_itens, ordens_servico, servicos, "
					+ "pecas_insumos, estoques, movimentacoes_estoque RESTART IDENTITY CASCADE");
	}

	@Test
	void shouldCreateOsWithServiceOnly() throws Exception {
		UUID clienteId = criarCliente("Joao Silva", "12345678909");
		UUID servicoId = criarServico("Troca de oleo", new BigDecimal("100.00"));

		CriarOrdemServicoRequest request = CriarOrdemServicoRequest.builder()
			.clienteDocumento("12345678909")
			.veiculoPlaca("ABC1D23")
			.veiculo(CriarOrdemServicoRequest.DadosVeiculoRequest.builder()
				.marca("Toyota")
				.modelo("Corolla")
				.ano(2020)
				.build())
			.servicos(List
				.of(CriarOrdemServicoRequest.ItemServicoRequest.builder().servicoId(servicoId).quantidade(1).build()))
			.observacoes("Barulho ao frear")
			.build();

		mockMvc
			.perform(post("/api/v1/ordens-servico").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.numero").value(org.hamcrest.Matchers.startsWith("OS-")))
			.andExpect(jsonPath("$.status").value("AGUARDANDO_RESPOSTA_CLIENTE"))
			.andExpect(jsonPath("$.cliente.id").value(clienteId.toString()))
			.andExpect(jsonPath("$.veiculo.placa").value("ABC1D23"))
			.andExpect(jsonPath("$.orcamento.valorTotal").value(100.00))
			.andExpect(jsonPath("$.orcamento.status").value("PENDENTE_APROVACAO"))
			.andExpect(jsonPath("$.observacoes").value("Barulho ao frear"));
	}

	@Test
	void shouldCreateOsWithServicesAndParts() throws Exception {
		criarCliente("Maria Souza", "98765432100");
		UUID servicoId = criarServico("Alinhamento", new BigDecimal("80.00"));
		UUID pecaId = criarPecaComEstoque("OLEO-5W30", "Oleo 5W30", new BigDecimal("50.00"), new BigDecimal("10"));

		CriarOrdemServicoRequest request = CriarOrdemServicoRequest.builder()
			.clienteDocumento("98765432100")
			.veiculoPlaca("XYZ9G87")
			.veiculo(CriarOrdemServicoRequest.DadosVeiculoRequest.builder()
				.marca("Honda")
				.modelo("Civic")
				.ano(2019)
				.build())
			.servicos(List
				.of(CriarOrdemServicoRequest.ItemServicoRequest.builder().servicoId(servicoId).quantidade(1).build()))
			.pecas(List.of(CriarOrdemServicoRequest.ItemPecaRequest.builder()
				.pecaId(pecaId)
				.quantidade(new BigDecimal("2"))
				.build()))
			.build();

		mockMvc
			.perform(post("/api/v1/ordens-servico").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated())
			// R$80 servico + R$50*2 pecas = R$180
			.andExpect(jsonPath("$.orcamento.valorTotal").value(180.00));
	}

	@Test
	void shouldUseExistingVehicleWhenAlreadyRegistered() throws Exception {
		UUID clienteId = criarCliente("Pedro Alves", "11144477735");
		UUID servicoId = criarServico("Balanceamento", new BigDecimal("60.00"));

		// first OS creates the vehicle
		CriarOrdemServicoRequest primeiraOS = CriarOrdemServicoRequest.builder()
			.clienteDocumento("11144477735")
			.veiculoPlaca("ABC1D23")
			.veiculo(CriarOrdemServicoRequest.DadosVeiculoRequest.builder()
				.marca("Fiat")
				.modelo("Uno")
				.ano(2018)
				.build())
			.servicos(List
				.of(CriarOrdemServicoRequest.ItemServicoRequest.builder().servicoId(servicoId).quantidade(1).build()))
			.build();

		mockMvc
			.perform(post("/api/v1/ordens-servico").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(primeiraOS)))
			.andExpect(status().isCreated());

		// second OS for the same vehicle — must reuse without re-creating
		UUID servicoId2 = criarServico("Troca de filtro", new BigDecimal("40.00"));
		CriarOrdemServicoRequest segundaOS = CriarOrdemServicoRequest.builder()
			.clienteDocumento("11144477735")
			.veiculoPlaca("ABC1D23")
			.servicos(List
				.of(CriarOrdemServicoRequest.ItemServicoRequest.builder().servicoId(servicoId2).quantidade(1).build()))
			.build();

		mockMvc
			.perform(post("/api/v1/ordens-servico").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(segundaOS)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.veiculo.placa").value("ABC1D23"))
			.andExpect(jsonPath("$.numero").value(org.hamcrest.Matchers.startsWith("OS-")));
	}

	@Test
	void shouldReturn404WhenClientNotFound() throws Exception {
		UUID servicoId = criarServico("Servico X", new BigDecimal("50.00"));

		CriarOrdemServicoRequest request = CriarOrdemServicoRequest.builder()
			.clienteDocumento("00000000000")
			.veiculoPlaca("ABC1D23")
			.servicos(List
				.of(CriarOrdemServicoRequest.ItemServicoRequest.builder().servicoId(servicoId).quantidade(1).build()))
			.build();

		mockMvc
			.perform(post("/api/v1/ordens-servico").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isNotFound());
	}

	@Test
	void shouldReturn404WhenServiceNotFoundInCatalog() throws Exception {
		criarCliente("Ana Lima", "71428793860");

		CriarOrdemServicoRequest request = CriarOrdemServicoRequest.builder()
			.clienteDocumento("71428793860")
			.veiculoPlaca("ABC1D23")
			.veiculo(CriarOrdemServicoRequest.DadosVeiculoRequest.builder().marca("VW").modelo("Gol").ano(2015).build())
			.servicos(List.of(CriarOrdemServicoRequest.ItemServicoRequest.builder()
				.servicoId(UUID.randomUUID())
				.quantidade(1)
				.build()))
			.build();

		mockMvc
			.perform(post("/api/v1/ordens-servico").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isNotFound());
	}

	@Test
	void shouldReturn422WhenStockIsInsufficient() throws Exception {
		criarCliente("Carlos Neto", "87748248800");
		UUID servicoId = criarServico("Troca de oleo", new BigDecimal("100.00"));
		UUID pecaId = criarPecaComEstoque("OLEO-10W40", "Oleo 10W40", new BigDecimal("45.00"), new BigDecimal("1"));

		CriarOrdemServicoRequest request = CriarOrdemServicoRequest.builder()
			.clienteDocumento("87748248800")
			.veiculoPlaca("DEF2E34")
			.veiculo(CriarOrdemServicoRequest.DadosVeiculoRequest.builder()
				.marca("Chevrolet")
				.modelo("Onix")
				.ano(2022)
				.build())
			.servicos(List
				.of(CriarOrdemServicoRequest.ItemServicoRequest.builder().servicoId(servicoId).quantidade(1).build()))
			.pecas(List.of(CriarOrdemServicoRequest.ItemPecaRequest.builder()
				.pecaId(pecaId)
				.quantidade(new BigDecimal("5"))
				.build()))
			.build();

		mockMvc
			.perform(post("/api/v1/ordens-servico").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isUnprocessableEntity());
	}

	@Test
	void shouldReturn422WhenVehicleBelongsToDifferentClient() throws Exception {
		UUID outroCliente = criarCliente("Outro Cliente", "52998224725");
		UUID meuCliente = criarCliente("Meu Cliente", "71428793860");
		UUID servicoId = criarServico("Revisao", new BigDecimal("200.00"));

		// register vehicle for another client
		CriarOrdemServicoRequest registrarVeiculo = CriarOrdemServicoRequest.builder()
			.clienteDocumento("52998224725")
			.veiculoPlaca("ZZZ9Z99")
			.veiculo(
					CriarOrdemServicoRequest.DadosVeiculoRequest.builder().marca("Ford").modelo("Ka").ano(2017).build())
			.servicos(List
				.of(CriarOrdemServicoRequest.ItemServicoRequest.builder().servicoId(servicoId).quantidade(1).build()))
			.build();

		mockMvc
			.perform(post("/api/v1/ordens-servico").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(registrarVeiculo)))
			.andExpect(status().isCreated());

		// try to use same vehicle with different client
		CriarOrdemServicoRequest request = CriarOrdemServicoRequest.builder()
			.clienteDocumento("71428793860")
			.veiculoPlaca("ZZZ9Z99")
			.servicos(List
				.of(CriarOrdemServicoRequest.ItemServicoRequest.builder().servicoId(servicoId).quantidade(1).build()))
			.build();

		mockMvc
			.perform(post("/api/v1/ordens-servico").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isUnprocessableEntity());
	}

	@Test
	void shouldReturn400WhenRequiredFieldsMissing() throws Exception {
		mockMvc
			.perform(post("/api/v1/ordens-servico").contentType(MediaType.APPLICATION_JSON)
				.content("{\"clienteDocumento\":\"12345678909\"}"))
			.andExpect(status().isBadRequest());
	}

	@Test
	void shouldReturn422WhenVehicleNotFoundAndDataMissing() throws Exception {
		criarCliente("Teste Missing", "71428793860");
		UUID servicoId = criarServico("Servico Y", new BigDecimal("50.00"));

		CriarOrdemServicoRequest request = CriarOrdemServicoRequest.builder()
			.clienteDocumento("71428793860")
			.veiculoPlaca("NEW9X99")
			.servicos(List
				.of(CriarOrdemServicoRequest.ItemServicoRequest.builder().servicoId(servicoId).quantidade(1).build()))
			.build();

		mockMvc
			.perform(post("/api/v1/ordens-servico").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isUnprocessableEntity());
	}

	@Test
	void shouldApproveOrcamento() throws Exception {
		UUID orcamentoId = criarOsEObterOrcamentoId();

		mockMvc.perform(patch("/api/v1/orcamentos/{id}/aprovar", orcamentoId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(orcamentoId.toString()))
			.andExpect(jsonPath("$.status").value("APROVADO"))
			.andExpect(jsonPath("$.valorTotal").isNumber())
			.andExpect(jsonPath("$.itens").isArray());
	}

	@Test
	void shouldRejectOrcamento() throws Exception {
		UUID orcamentoId = criarOsEObterOrcamentoId();

		mockMvc.perform(patch("/api/v1/orcamentos/{id}/rejeitar", orcamentoId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(orcamentoId.toString()))
			.andExpect(jsonPath("$.status").value("REJEITADO"));
	}

	@Test
	void shouldReturn404WhenApprovingNonExistentOrcamento() throws Exception {
		mockMvc.perform(patch("/api/v1/orcamentos/{id}/aprovar", UUID.randomUUID())).andExpect(status().isNotFound());
	}

	@Test
	void shouldReturn422WhenApprovingAlreadyApprovedOrcamento() throws Exception {
		UUID orcamentoId = criarOsEObterOrcamentoId();

		mockMvc.perform(patch("/api/v1/orcamentos/{id}/aprovar", orcamentoId)).andExpect(status().isOk());

		mockMvc.perform(patch("/api/v1/orcamentos/{id}/aprovar", orcamentoId))
			.andExpect(status().isUnprocessableEntity());
	}

	@Test
	void shouldReturn404WhenRejectingNonExistentOrcamento() throws Exception {
		mockMvc.perform(patch("/api/v1/orcamentos/{id}/rejeitar", UUID.randomUUID())).andExpect(status().isNotFound());
	}

	// --- helpers ---

	private UUID criarOsEObterOrcamentoId() throws Exception {
		criarCliente("Cliente Orcamento", "33200738006");
		UUID servicoId = criarServico("Servico Orcamento", new BigDecimal("150.00"));

		CriarOrdemServicoRequest request = CriarOrdemServicoRequest.builder()
			.clienteDocumento("33200738006")
			.veiculoPlaca("ORC1A11")
			.veiculo(
					CriarOrdemServicoRequest.DadosVeiculoRequest.builder().marca("VW").modelo("Polo").ano(2023).build())
			.servicos(List
				.of(CriarOrdemServicoRequest.ItemServicoRequest.builder().servicoId(servicoId).quantidade(1).build()))
			.build();

		MvcResult result = mockMvc
			.perform(post("/api/v1/ordens-servico").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated())
			.andReturn();

		JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
		return UUID.fromString(body.get("orcamento").get("id").asText());
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

	private UUID criarPecaComEstoque(String sku, String nome, BigDecimal valorUnitario, BigDecimal quantidade)
			throws Exception {
		CadastroPecaRequest pecaReq = CadastroPecaRequest.builder()
			.sku(sku)
			.nome(nome)
			.valorUnitario(valorUnitario)
			.estoqueMinimo(BigDecimal.ZERO)
			.unidadeMedida("UN")
			.tipoItem("PECA")
			.build();
		MvcResult pecaResult = mockMvc
			.perform(post("/api/v1/pecas").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(pecaReq)))
			.andExpect(status().isCreated())
			.andReturn();
		UUID pecaId = UUID
			.fromString(objectMapper.readTree(pecaResult.getResponse().getContentAsString()).get("id").asText());

		CriarEstoqueRequest estoqueReq = CriarEstoqueRequest.builder()
			.pecaInsumoId(pecaId)
			.localizacao("Prateleira A1")
			.quantidade(quantidade)
			.build();
		mockMvc
			.perform(post("/api/v1/pecas/estoques").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(estoqueReq)))
			.andExpect(status().isCreated());

		return pecaId;
	}

}

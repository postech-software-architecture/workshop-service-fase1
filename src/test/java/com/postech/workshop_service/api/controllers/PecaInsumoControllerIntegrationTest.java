package com.postech.workshop_service.api.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.postech.workshop_service.config.PostgresTestContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class PecaInsumoControllerIntegrationTest extends PostgresTestContainer {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	private String uniqueSku;

	@BeforeEach
	void setUp() {
		uniqueSku = "TEST-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
	}

	@Test
	void deveCriarPecaComSucesso() throws Exception {
		Map<String, Object> request = new HashMap<>();
		request.put("sku", uniqueSku);
		request.put("nome", "Peca Teste");
		request.put("valorUnitario", new BigDecimal("100.00"));
		request.put("estoqueMinimo", new BigDecimal("5"));
		request.put("unidadeMedida", "UN");
		request.put("tipoItem", "PECA");

		mockMvc
			.perform(post("/api/v1/pecas").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.sku").value(uniqueSku))
			.andExpect(jsonPath("$.nome").value("Peca Teste"))
			.andExpect(jsonPath("$.tipoItem").value("PECA"));
	}

	@Test
	void deveLancarErroAoCriarPecaComSkuDuplicado() throws Exception {
		Map<String, Object> request = new HashMap<>();
		request.put("sku", uniqueSku);
		request.put("nome", "Peca Duplicada");
		request.put("valorUnitario", new BigDecimal("50.00"));
		request.put("estoqueMinimo", new BigDecimal("2"));
		request.put("unidadeMedida", "UN");
		request.put("tipoItem", "PECA");

		mockMvc
			.perform(post("/api/v1/pecas").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated());

		mockMvc
			.perform(post("/api/v1/pecas").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isUnprocessableEntity());
	}

	@Test
	void deveListarPecas() throws Exception {
		mockMvc.perform(get("/api/v1/pecas"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.conteudo").isArray())
			.andExpect(jsonPath("$.pagina").exists())
			.andExpect(jsonPath("$.tamanho").exists())
			.andExpect(jsonPath("$.totalElementos").exists())
			.andExpect(jsonPath("$.totalPaginas").exists());
	}

	@Test
	void deveBuscarPecaPorId() throws Exception {
		Map<String, Object> request = new HashMap<>();
		request.put("sku", uniqueSku);
		request.put("nome", "Peca Busca");
		request.put("valorUnitario", new BigDecimal("75.00"));
		request.put("estoqueMinimo", new BigDecimal("3"));
		request.put("unidadeMedida", "UN");
		request.put("tipoItem", "PECA");

		String response = mockMvc
			.perform(post("/api/v1/pecas").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated())
			.andReturn()
			.getResponse()
			.getContentAsString();

		String id = objectMapper.readTree(response).get("id").asText();

		mockMvc.perform(get("/api/v1/pecas/{id}", id))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.sku").value(uniqueSku));
	}

	@Test
	void deveRetornar404AoBuscarPecaInexistente() throws Exception {
		mockMvc.perform(get("/api/v1/pecas/{id}", UUID.randomUUID())).andExpect(status().isNotFound());
	}

	@Test
	void deveBuscarPecaPorSku() throws Exception {
		Map<String, Object> request = new HashMap<>();
		request.put("sku", uniqueSku);
		request.put("nome", "Peca SKU Busca");
		request.put("valorUnitario", new BigDecimal("80.00"));
		request.put("estoqueMinimo", new BigDecimal("4"));
		request.put("unidadeMedida", "UN");
		request.put("tipoItem", "PECA");

		mockMvc
			.perform(post("/api/v1/pecas").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated());

		mockMvc.perform(get("/api/v1/pecas/sku/{sku}", uniqueSku))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.sku").value(uniqueSku));
	}

	@Test
	void deveAtualizarPeca() throws Exception {
		Map<String, Object> createRequest = new HashMap<>();
		createRequest.put("sku", uniqueSku);
		createRequest.put("nome", "Peca Original");
		createRequest.put("valorUnitario", new BigDecimal("50.00"));
		createRequest.put("estoqueMinimo", new BigDecimal("2"));
		createRequest.put("unidadeMedida", "UN");
		createRequest.put("tipoItem", "PECA");

		String response = mockMvc
			.perform(post("/api/v1/pecas").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(createRequest)))
			.andExpect(status().isCreated())
			.andReturn()
			.getResponse()
			.getContentAsString();

		String id = objectMapper.readTree(response).get("id").asText();

		Map<String, Object> updateRequest = new HashMap<>();
		updateRequest.put("nome", "Peca Atualizada");
		updateRequest.put("valorUnitario", new BigDecimal("60.00"));
		updateRequest.put("estoqueMinimo", new BigDecimal("5"));
		updateRequest.put("unidadeMedida", "UN");
		updateRequest.put("tipoItem", "INSUMO");

		mockMvc
			.perform(put("/api/v1/pecas/{id}", id).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(updateRequest)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.nome").value("Peca Atualizada"))
			.andExpect(jsonPath("$.valorUnitario").value(60.00))
			.andExpect(jsonPath("$.tipoItem").value("INSUMO"));
	}

	@Test
	void deveRemoverPeca() throws Exception {
		Map<String, Object> request = new HashMap<>();
		request.put("sku", uniqueSku);
		request.put("nome", "Peca Remover");
		request.put("valorUnitario", new BigDecimal("30.00"));
		request.put("estoqueMinimo", new BigDecimal("1"));
		request.put("unidadeMedida", "UN");
		request.put("tipoItem", "PECA");

		String response = mockMvc
			.perform(post("/api/v1/pecas").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated())
			.andReturn()
			.getResponse()
			.getContentAsString();

		String id = objectMapper.readTree(response).get("id").asText();

		mockMvc.perform(delete("/api/v1/pecas/{id}", id)).andExpect(status().isNoContent());

		mockMvc.perform(get("/api/v1/pecas/{id}", id)).andExpect(status().isNotFound());
	}

	@Test
	void deveFiltrarPecasPorNome() throws Exception {
		Map<String, Object> request = new HashMap<>();
		request.put("sku", uniqueSku);
		request.put("nome", "Filtro Especial Teste");
		request.put("valorUnitario", new BigDecimal("40.00"));
		request.put("estoqueMinimo", new BigDecimal("2"));
		request.put("unidadeMedida", "UN");
		request.put("tipoItem", "PECA");

		mockMvc
			.perform(post("/api/v1/pecas").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated());

		mockMvc.perform(get("/api/v1/pecas").param("nome", "Filtro")).andExpect(status().isOk());
	}

	@Test
	void deveFiltrarPecasPorCategoria() throws Exception {
		Map<String, Object> request = new HashMap<>();
		request.put("sku", uniqueSku);
		request.put("nome", "Peca Categoria");
		request.put("valorUnitario", new BigDecimal("45.00"));
		request.put("estoqueMinimo", new BigDecimal("3"));
		request.put("unidadeMedida", "UN");
		request.put("tipoItem", "INSUMO");
		request.put("categoria", "Filtros");

		mockMvc
			.perform(post("/api/v1/pecas").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated());

		mockMvc.perform(get("/api/v1/pecas").param("categoria", "Filtros")).andExpect(status().isOk());
	}

}

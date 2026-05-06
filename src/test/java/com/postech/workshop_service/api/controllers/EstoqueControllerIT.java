package com.postech.workshop_service.api.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.postech.workshop_service.config.PostgresTestContainer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@WithMockUser(roles = "ADMINISTRADOR")
class EstoqueControllerIT extends PostgresTestContainer {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void deveBuscarEstoquePorIdEListarPorPeca() throws Exception {
		String pecaId = criarPeca().get("id").asText();
		String estoqueId = criarEstoque(pecaId, "Prateleira A1", new BigDecimal("10")).get("id").asText();
		criarEstoque(pecaId, "Prateleira B2", new BigDecimal("5"));

		mockMvc.perform(get("/api/v1/estoques/{id}", estoqueId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(estoqueId))
			.andExpect(jsonPath("$.pecaInsumoId").value(pecaId))
			.andExpect(jsonPath("$.localizacao").value("Prateleira A1"))
			.andExpect(jsonPath("$.quantidade").value(10));

		mockMvc.perform(get("/api/v1/estoques/peca/{pecaInsumoId}", pecaId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].pecaInsumoId").value(pecaId))
			.andExpect(jsonPath("$[0].localizacao").value("Prateleira A1"))
			.andExpect(jsonPath("$[1].localizacao").value("Prateleira B2"));
	}

	@Test
	void deveRetornar404AoBuscarEstoqueInexistente() throws Exception {
		mockMvc.perform(get("/api/v1/estoques/{id}", UUID.randomUUID())).andExpect(status().isNotFound());
	}

	private JsonNode criarPeca() throws Exception {
		Map<String, Object> request = new HashMap<>();
		request.put("sku", "EST-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
		request.put("nome", "Peca Estoque");
		request.put("valorUnitario", new BigDecimal("50.00"));
		request.put("estoqueMinimo", new BigDecimal("2"));
		request.put("unidadeMedida", "UN");
		request.put("tipoItem", "PECA");

		MvcResult result = mockMvc
			.perform(post("/api/v1/pecas").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated())
			.andReturn();

		return objectMapper.readTree(result.getResponse().getContentAsString());
	}

	private JsonNode criarEstoque(String pecaId, String localizacao, BigDecimal quantidade) throws Exception {
		Map<String, Object> request = new HashMap<>();
		request.put("pecaInsumoId", pecaId);
		request.put("localizacao", localizacao);
		request.put("quantidade", quantidade);

		MvcResult result = mockMvc
			.perform(post("/api/v1/pecas/estoques").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated())
			.andReturn();

		return objectMapper.readTree(result.getResponse().getContentAsString());
	}

}

package com.postech.workshop_service.api.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.postech.workshop_service.api.dtos.AtualizarServicoRequest;
import com.postech.workshop_service.api.dtos.CadastroServicoRequest;
import com.postech.workshop_service.config.PostgresTestContainer;
import com.postech.workshop_service.domain.enums.CategoriaServico;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class ServicoControllerIT extends PostgresTestContainer {

	@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@BeforeEach
	void limparServicos() {
		jdbcTemplate.execute("TRUNCATE TABLE servicos RESTART IDENTITY CASCADE");
	}

	@Test
	void shouldExecuteServicoFlow() throws Exception {
		CadastroServicoRequest cadastro = CadastroServicoRequest.builder()
			.nome("Troca de oleo")
			.descricao("Substituicao do oleo do motor")
			.valor(new BigDecimal("150.00"))
			.tempoEstimadoMinutos(60)
			.build();

		MvcResult createResult = mockMvc
			.perform(post("/api/v1/servicos").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(cadastro)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.nome").value("Troca de oleo"))
			.andExpect(jsonPath("$.ativo").value(true))
			.andReturn();

		JsonNode created = objectMapper.readTree(createResult.getResponse().getContentAsString());
		UUID servicoId = UUID.fromString(created.get("id").asText());

		mockMvc.perform(get("/api/v1/servicos/{id}", servicoId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(servicoId.toString()));

		mockMvc.perform(get("/api/v1/servicos").param("pagina", "0").param("tamanho", "10"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.conteudo[0].id").value(servicoId.toString()));

		AtualizarServicoRequest atualizar = AtualizarServicoRequest.builder()
			.nome("Troca de oleo premium")
			.descricao("Substituicao do oleo sintetico")
			.valor(new BigDecimal("200.00"))
			.tempoEstimadoMinutos(75)
			.build();

		mockMvc
			.perform(put("/api/v1/servicos/{id}", servicoId).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(atualizar)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.nome").value("Troca de oleo premium"));

		mockMvc.perform(delete("/api/v1/servicos/{id}", servicoId)).andExpect(status().isNoContent());

		mockMvc.perform(get("/api/v1/servicos/{id}", servicoId)).andExpect(status().isNotFound());
	}

	@Test
	void shouldReturn400WhenRequiredFieldMissing() throws Exception {
		CadastroServicoRequest cadastro = CadastroServicoRequest.builder()
			.descricao("Descricao sem nome")
			.valor(new BigDecimal("100.00"))
			.tempoEstimadoMinutos(60)
			.build();

		mockMvc
			.perform(post("/api/v1/servicos").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(cadastro)))
			.andExpect(status().isUnprocessableEntity());
	}

	@Test
	void shouldReturn422WhenDuplicateName() throws Exception {
		CadastroServicoRequest primeiro = CadastroServicoRequest.builder()
			.nome("Alinhamento")
			.descricao("Alinhamento de rodas")
			.valor(new BigDecimal("80.00"))
			.tempoEstimadoMinutos(30)
			.build();

		mockMvc
			.perform(post("/api/v1/servicos").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(primeiro)))
			.andExpect(status().isCreated());

		CadastroServicoRequest duplicado = CadastroServicoRequest.builder()
			.nome("Alinhamento")
			.descricao("Outro alinhamento")
			.valor(new BigDecimal("90.00"))
			.tempoEstimadoMinutos(45)
			.build();

		mockMvc
			.perform(post("/api/v1/servicos").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(duplicado)))
			.andExpect(status().isBadRequest());
	}

	@Test
	void shouldReturn422WhenInvalidValue() throws Exception {
		CadastroServicoRequest cadastro = CadastroServicoRequest.builder()
			.nome("Servico invalido")
			.descricao("Descricao")
			.valor(new BigDecimal("-50.00"))
			.tempoEstimadoMinutos(30)
			.build();

		mockMvc
			.perform(post("/api/v1/servicos").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(cadastro)))
			.andExpect(status().isUnprocessableEntity());
	}

	@Test
	void shouldListByCategoria() throws Exception {
		CadastroServicoRequest cadastro = CadastroServicoRequest.builder()
			.nome("Revisao preventiva")
			.descricao("Revisao completa do veiculo")
			.valor(new BigDecimal("300.00"))
			.tempoEstimadoMinutos(120)
			.categoria(CategoriaServico.PREVENTIVA)
			.build();

		mockMvc
			.perform(post("/api/v1/servicos").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(cadastro)))
			.andExpect(status().isCreated());

		mockMvc.perform(get("/api/v1/servicos/categoria/{categoria}", "PREVENTIVA"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].nome").value("Revisao preventiva"));
	}

	@Test
	void shouldReturn200ForTempoMedio() throws Exception {
		CadastroServicoRequest cadastro = CadastroServicoRequest.builder()
			.nome("Troca de correia")
			.descricao("Substituicao da correia dentada")
			.valor(new BigDecimal("450.00"))
			.tempoEstimadoMinutos(180)
			.build();

		MvcResult createResult = mockMvc
			.perform(post("/api/v1/servicos").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(cadastro)))
			.andExpect(status().isCreated())
			.andReturn();

		UUID servicoId = UUID
			.fromString(objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asText());

		mockMvc.perform(get("/api/v1/servicos/{id}/tempo-medio", servicoId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.tempoEstimadoMinutos").value(180))
			.andExpect(jsonPath("$.tempoMedioRealMinutos").isEmpty());
	}

}

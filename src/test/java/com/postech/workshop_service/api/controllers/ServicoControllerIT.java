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

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
			.build();

		mockMvc
			.perform(post("/api/v1/servicos").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(cadastro)))
			.andExpect(status().isBadRequest());
	}

	@Test
	void shouldReturn422WhenDuplicateName() throws Exception {
		CadastroServicoRequest primeiro = CadastroServicoRequest.builder()
			.nome("Alinhamento")
			.descricao("Alinhamento de rodas")
			.valor(new BigDecimal("80.00"))
			.build();

		mockMvc
			.perform(post("/api/v1/servicos").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(primeiro)))
			.andExpect(status().isCreated());

		CadastroServicoRequest duplicado = CadastroServicoRequest.builder()
			.nome("Alinhamento")
			.descricao("Outro alinhamento")
			.valor(new BigDecimal("90.00"))
			.build();

		mockMvc
			.perform(post("/api/v1/servicos").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(duplicado)))
			.andExpect(status().isUnprocessableEntity());
	}

	@Test
	void shouldReturn400WhenInvalidValue() throws Exception {
		CadastroServicoRequest cadastro = CadastroServicoRequest.builder()
			.nome("Servico invalido")
			.descricao("Descricao")
			.valor(new BigDecimal("-50.00"))
			.build();

		mockMvc
			.perform(post("/api/v1/servicos").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(cadastro)))
			.andExpect(status().isBadRequest());
	}

	@Test
	void shouldListByCategoria() throws Exception {
		CadastroServicoRequest cadastro = CadastroServicoRequest.builder()
			.nome("Revisao preventiva")
			.descricao("Revisao completa do veiculo")
			.valor(new BigDecimal("300.00"))
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
	void shouldReturn400WhenNomeIsNull() throws Exception {
		CadastroServicoRequest cadastro = CadastroServicoRequest.builder()
			.descricao("Descricao valida")
			.valor(new BigDecimal("100.00"))
			.build();

		mockMvc
			.perform(post("/api/v1/servicos").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(cadastro)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.fieldErrors[?(@.field=='nome')]").exists());
	}

	@Test
	void shouldReturn400WhenDescricaoIsBlank() throws Exception {
		CadastroServicoRequest cadastro = CadastroServicoRequest.builder()
			.nome("Servico X")
			.descricao("")
			.valor(new BigDecimal("100.00"))
			.build();

		mockMvc
			.perform(post("/api/v1/servicos").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(cadastro)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.fieldErrors[?(@.field=='descricao')]").exists());
	}

	@Test
	void shouldReturn400WhenValorIsNull() throws Exception {
		CadastroServicoRequest cadastro = CadastroServicoRequest.builder()
			.nome("Servico X")
			.descricao("Descricao valida")
			.build();

		mockMvc
			.perform(post("/api/v1/servicos").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(cadastro)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.fieldErrors[?(@.field=='valor')]").exists());
	}

	@Test
	void shouldReturn400WhenValorIsZero() throws Exception {
		CadastroServicoRequest cadastro = CadastroServicoRequest.builder()
			.nome("Servico X")
			.descricao("Descricao valida")
			.valor(BigDecimal.ZERO)
			.build();

		mockMvc
			.perform(post("/api/v1/servicos").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(cadastro)))
			.andExpect(status().isBadRequest());
	}

	@Test
	void shouldReturn400WhenGarantiaIsNegative() throws Exception {
		CadastroServicoRequest cadastro = CadastroServicoRequest.builder()
			.nome("Servico X")
			.descricao("Descricao valida")
			.valor(new BigDecimal("100.00"))
			.garantiaDias(-5)
			.build();

		mockMvc
			.perform(post("/api/v1/servicos").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(cadastro)))
			.andExpect(status().isBadRequest());
	}

	@Test
	void shouldReturn400WhenNomeExceedsMaxLength() throws Exception {
		String longName = "a".repeat(101);
		CadastroServicoRequest cadastro = CadastroServicoRequest.builder()
			.nome(longName)
			.descricao("Descricao valida")
			.valor(new BigDecimal("100.00"))
			.build();

		mockMvc
			.perform(post("/api/v1/servicos").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(cadastro)))
			.andExpect(status().isBadRequest());
	}

	@Test
	void shouldReturn400WhenJsonIsMalformed() throws Exception {
		mockMvc.perform(post("/api/v1/servicos").contentType(MediaType.APPLICATION_JSON).content("{nome: 'sem aspas'}"))
			.andExpect(status().isBadRequest());
	}

	@Test
	void shouldReturn400WhenCategoriaEnumIsInvalid() throws Exception {
		String payload = "{\"nome\":\"Servico X\",\"descricao\":\"D\",\"valor\":100.00,"
				+ "\"categoria\":\"NAO_EXISTE\"}";
		mockMvc.perform(post("/api/v1/servicos").contentType(MediaType.APPLICATION_JSON).content(payload))
			.andExpect(status().isBadRequest());
	}

	@Test
	void shouldReturn404WhenPuttingNonexistentId() throws Exception {
		AtualizarServicoRequest atualizar = AtualizarServicoRequest.builder()
			.nome("Qualquer")
			.descricao("Qualquer")
			.valor(new BigDecimal("100.00"))
			.build();

		mockMvc
			.perform(put("/api/v1/servicos/{id}", UUID.randomUUID()).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(atualizar)))
			.andExpect(status().isNotFound());
	}

	@Test
	void shouldReturn404WhenDeletingNonexistentId() throws Exception {
		mockMvc.perform(delete("/api/v1/servicos/{id}", UUID.randomUUID())).andExpect(status().isNotFound());
	}

	@Test
	void shouldReturn400WhenIdInPathIsNotUuid() throws Exception {
		mockMvc.perform(get("/api/v1/servicos/{id}", "not-a-uuid")).andExpect(status().isBadRequest());
	}

	@Test
	void shouldReturn404WhenInactiveServicoIsRequestedWithoutFlag() throws Exception {
		CadastroServicoRequest cadastro = CadastroServicoRequest.builder()
			.nome("Servico desativado")
			.descricao("Sera removido")
			.valor(new BigDecimal("100.00"))
			.build();

		MvcResult result = mockMvc
			.perform(post("/api/v1/servicos").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(cadastro)))
			.andExpect(status().isCreated())
			.andReturn();

		UUID id = UUID.fromString(objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText());

		mockMvc.perform(delete("/api/v1/servicos/{id}", id)).andExpect(status().isNoContent());

		mockMvc.perform(get("/api/v1/servicos/{id}", id)).andExpect(status().isNotFound());

		mockMvc.perform(get("/api/v1/servicos/{id}", id).param("incluirInativos", "true"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.ativo").value(false));
	}

	@Test
	void shouldReativarPreviouslyDeletedServico() throws Exception {
		CadastroServicoRequest cadastro = CadastroServicoRequest.builder()
			.nome("Servico para reativar")
			.descricao("Descricao do servico")
			.valor(new BigDecimal("100.00"))
			.build();

		MvcResult result = mockMvc
			.perform(post("/api/v1/servicos").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(cadastro)))
			.andExpect(status().isCreated())
			.andReturn();

		UUID id = UUID.fromString(objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText());

		mockMvc.perform(delete("/api/v1/servicos/{id}", id)).andExpect(status().isNoContent());

		mockMvc.perform(post("/api/v1/servicos/{id}/reativar", id))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(id.toString()))
			.andExpect(jsonPath("$.ativo").value(true))
			.andExpect(jsonPath("$.dataRemocao").doesNotExist());

		mockMvc.perform(get("/api/v1/servicos/{id}", id))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.ativo").value(true));
	}

	@Test
	void shouldReturn200WhenReativarAlreadyActive() throws Exception {
		CadastroServicoRequest cadastro = CadastroServicoRequest.builder()
			.nome("Servico ja ativo")
			.descricao("Descricao")
			.valor(new BigDecimal("100.00"))
			.build();

		MvcResult result = mockMvc
			.perform(post("/api/v1/servicos").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(cadastro)))
			.andExpect(status().isCreated())
			.andReturn();

		UUID id = UUID.fromString(objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText());

		mockMvc.perform(post("/api/v1/servicos/{id}/reativar", id))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.ativo").value(true));
	}

	@Test
	void shouldReturn404WhenReativarNonexistentId() throws Exception {
		mockMvc.perform(post("/api/v1/servicos/{id}/reativar", UUID.randomUUID())).andExpect(status().isNotFound());
	}

	@Test
	void shouldReturn422WhenReativarConflictsWithActiveName() throws Exception {
		CadastroServicoRequest primeiro = CadastroServicoRequest.builder()
			.nome("Servico A")
			.descricao("Primeiro servico")
			.valor(new BigDecimal("100.00"))
			.build();

		MvcResult primeiroResult = mockMvc
			.perform(post("/api/v1/servicos").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(primeiro)))
			.andExpect(status().isCreated())
			.andReturn();

		UUID primeiroId = UUID
			.fromString(objectMapper.readTree(primeiroResult.getResponse().getContentAsString()).get("id").asText());

		mockMvc.perform(delete("/api/v1/servicos/{id}", primeiroId)).andExpect(status().isNoContent());

		CadastroServicoRequest segundo = CadastroServicoRequest.builder()
			.nome("Servico A")
			.descricao("Segundo servico com mesmo nome")
			.valor(new BigDecimal("110.00"))
			.build();

		mockMvc
			.perform(post("/api/v1/servicos").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(segundo)))
			.andExpect(status().isCreated());

		mockMvc.perform(post("/api/v1/servicos/{id}/reativar", primeiroId)).andExpect(status().isUnprocessableEntity());
	}

	@Test
	void shouldReturn422WhenUpdatingWithDuplicateNameInActiveRecord() throws Exception {
		CadastroServicoRequest first = CadastroServicoRequest.builder()
			.nome("Lavagem completa")
			.descricao("Lavagem externa e interna")
			.valor(new BigDecimal("60.00"))
			.build();
		CadastroServicoRequest second = CadastroServicoRequest.builder()
			.nome("Polimento")
			.descricao("Polimento da lataria")
			.valor(new BigDecimal("120.00"))
			.build();

		mockMvc
			.perform(post("/api/v1/servicos").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(first)))
			.andExpect(status().isCreated());

		MvcResult result = mockMvc
			.perform(post("/api/v1/servicos").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(second)))
			.andExpect(status().isCreated())
			.andReturn();

		UUID secondId = UUID
			.fromString(objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText());

		AtualizarServicoRequest atualizar = AtualizarServicoRequest.builder()
			.nome("Lavagem completa")
			.descricao("Tentando duplicar")
			.valor(new BigDecimal("130.00"))
			.build();

		mockMvc
			.perform(put("/api/v1/servicos/{id}", secondId).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(atualizar)))
			.andExpect(status().isUnprocessableEntity());
	}

	@Test
	void shouldReturn404WhenIdPathIsEmpty() throws Exception {
		mockMvc.perform(get("/api/v1/servicos/"))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.status").value(404))
			.andExpect(jsonPath("$.message").value(containsString("Recurso não encontrado")))
			.andExpect(jsonPath("$.message").value(containsString("Verifique a URL")));
	}

	@Test
	void shouldReturn404ForUnknownSubpath() throws Exception {
		mockMvc.perform(get("/api/v1/servicos/{id}/inexistente", UUID.randomUUID()))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.status").value(404))
			.andExpect(jsonPath("$.message").value(containsString("Recurso não encontrado")));
	}

	@Test
	void shouldReturn405WhenMethodIsNotAllowed() throws Exception {
		mockMvc.perform(patch("/api/v1/servicos/{id}", UUID.randomUUID()))
			.andExpect(status().isMethodNotAllowed())
			.andExpect(jsonPath("$.status").value(405))
			.andExpect(jsonPath("$.message").value(containsString("Método PATCH não suportado")));
	}

}

package com.postech.workshop_service.api.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.postech.workshop_service.api.dtos.AtualizarClienteRequest;
import com.postech.workshop_service.api.dtos.CadastroClienteRequest;
import com.postech.workshop_service.api.dtos.EnderecoDTO;
import com.postech.workshop_service.config.PostgresTestContainer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class ClienteControllerIT extends PostgresTestContainer {

	@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void shouldExecuteFullCrudFlow() throws Exception {
		// 1. Create
		CadastroClienteRequest createRequest = new CadastroClienteRequest();
		createRequest.setNome("João da Silva");
		createRequest.setDocumento("98765432100"); // Valid CPF
		createRequest.setEmail("joao@silva.com");

		MvcResult result = mockMvc
			.perform(post("/api/v1/clientes").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(createRequest)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.id").exists())
			.andExpect(jsonPath("$.documento").value("***.654.321-**"))
			.andReturn();

		String idStr = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();
		UUID id = UUID.fromString(idStr);

		// 2. Read by ID
		mockMvc.perform(get("/api/v1/clientes/{id}", id))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.nome").value("João da Silva"));

		// 3. Update
		AtualizarClienteRequest updateRequest = new AtualizarClienteRequest();
		updateRequest.setNome("João Silva Junior");
		updateRequest.setEmail("joao.junior@silva.com");
		updateRequest.setEndereco(EnderecoDTO.builder()
			.logradouro("Rua das Flores")
			.numero("123")
			.cidade("São Paulo")
			.estado("SP")
			.cep("01234-567")
			.build());

		mockMvc
			.perform(put("/api/v1/clientes/{id}", id).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(updateRequest)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.nome").value("João Silva Junior"))
			.andExpect(jsonPath("$.endereco.logradouro").value("Rua das Flores"));

		// 4. List
		mockMvc.perform(get("/api/v1/clientes").param("pagina", "0").param("tamanho", "10"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].id").value(idStr));

		// 5. Delete (soft delete)
		mockMvc.perform(delete("/api/v1/clientes/{id}", id)).andExpect(status().isNoContent());

		// 6. Verify soft delete - client still exists but with dataRemocao set
		mockMvc.perform(get("/api/v1/clientes/{id}", id))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.dataRemocao").isNotEmpty());
	}

	@Test
	void shouldReturn422WhenDocumentoIsInvalid() throws Exception {
		CadastroClienteRequest request = new CadastroClienteRequest();
		request.setNome("Maria DB");
		request.setDocumento("123"); // Inválido
		request.setEmail("maria@db.com");

		mockMvc
			.perform(post("/api/v1/clientes").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isUnprocessableEntity());
	}

}

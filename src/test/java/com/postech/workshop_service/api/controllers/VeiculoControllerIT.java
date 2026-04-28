package com.postech.workshop_service.api.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.postech.workshop_service.api.dtos.AtualizarVeiculoRequest;
import com.postech.workshop_service.api.dtos.CadastroVeiculoRequest;
import com.postech.workshop_service.config.PostgresTestContainer;
import com.postech.workshop_service.domain.entities.Cliente;
import com.postech.workshop_service.domain.valueobjects.Documento;
import com.postech.workshop_service.infrastructure.persistence.repositories.ClienteRepositoryImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class VeiculoControllerIT extends PostgresTestContainer {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private ClienteRepositoryImpl clienteRepository;

	@Test
	void shouldExecuteVehicleFlow() throws Exception {
		UUID clienteA = criarCliente("Joao da Silva", "98765432100");
		UUID clienteB = criarCliente("Maria da Silva", "12345678909");

		CadastroVeiculoRequest cadastro = CadastroVeiculoRequest.builder()
			.placa("BRA1D23")
			.marca("Toyota")
			.modelo("Corolla")
			.ano(2020)
			.clientesIds(List.of(clienteA, clienteB))
			.build();

		MvcResult createResult = mockMvc
			.perform(post("/api/v1/veiculos").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(cadastro)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.placa").value("BRA1D23"))
			.andExpect(jsonPath("$.clientes.length()").value(2))
			.andReturn();

		JsonNode created = objectMapper.readTree(createResult.getResponse().getContentAsString());
		UUID veiculoId = UUID.fromString(created.get("id").asText());

		mockMvc.perform(get("/api/v1/veiculos/{id}", veiculoId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(veiculoId.toString()));

		mockMvc.perform(get("/api/v1/veiculos/placa/{placa}", "bra-1d23"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.placa").value("BRA1D23"));

		mockMvc.perform(get("/api/v1/veiculos").param("pagina", "0").param("tamanho", "10"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.conteudo[0].id").value(veiculoId.toString()));

		mockMvc.perform(get("/api/v1/veiculos/cliente/{clienteId}", clienteA))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].id").value(veiculoId.toString()));

		AtualizarVeiculoRequest atualizar = new AtualizarVeiculoRequest();
		atualizar.setPlaca("ABC1234");
		atualizar.setMarca("Ford");
		atualizar.setModelo("Focus");
		atualizar.setAno(2019);
		atualizar.setCor("Azul");

		mockMvc
			.perform(put("/api/v1/veiculos/{id}", veiculoId).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(atualizar)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.placa").value("ABC1234"))
			.andExpect(jsonPath("$.clientes.length()").value(2));

		mockMvc.perform(delete("/api/v1/veiculos/{id}/clientes/{clienteId}", veiculoId, clienteB))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.clientes.length()").value(1));

		mockMvc.perform(post("/api/v1/veiculos/{id}/clientes/{clienteId}", veiculoId, clienteB))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.clientes.length()").value(2));

		mockMvc.perform(delete("/api/v1/veiculos/{id}", veiculoId)).andExpect(status().isNoContent());

		mockMvc.perform(get("/api/v1/veiculos/{id}", veiculoId)).andExpect(status().isNotFound());

		mockMvc.perform(get("/api/v1/veiculos/{id}", veiculoId).param("incluirInativos", "true"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.ativo").value(false));
	}

	@Test
	void shouldReturn400WhenPlateIsInvalid() throws Exception {
		UUID clienteA = criarCliente("Cliente Erro", "11144477735");

		CadastroVeiculoRequest cadastro = CadastroVeiculoRequest.builder()
			.placa("123")
			.marca("Toyota")
			.modelo("Corolla")
			.ano(2020)
			.clientesIds(List.of(clienteA))
			.build();

		mockMvc
			.perform(post("/api/v1/veiculos").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(cadastro)))
			.andExpect(status().isBadRequest());
	}

	@Test
	void shouldReturn422WhenClientesListIsEmpty() throws Exception {
		CadastroVeiculoRequest cadastro = CadastroVeiculoRequest.builder()
			.placa("BRA1D23")
			.marca("Toyota")
			.modelo("Corolla")
			.ano(2020)
			.clientesIds(List.of())
			.build();

		mockMvc
			.perform(post("/api/v1/veiculos").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(cadastro)))
			.andExpect(status().isUnprocessableEntity());
	}

	@Test
	void shouldReturn400WhenTryingToRemoveLastCliente() throws Exception {
		UUID clienteA = criarCliente("Cliente Unico", "98712345628");

		CadastroVeiculoRequest cadastro = CadastroVeiculoRequest.builder()
			.placa("XYZ1A23")
			.marca("Honda")
			.modelo("Civic")
			.ano(2021)
			.clientesIds(List.of(clienteA))
			.build();

		MvcResult createResult = mockMvc
			.perform(post("/api/v1/veiculos").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(cadastro)))
			.andExpect(status().isCreated())
			.andReturn();

		UUID veiculoId = UUID
			.fromString(objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asText());

		mockMvc.perform(delete("/api/v1/veiculos/{id}/clientes/{clienteId}", veiculoId, clienteA))
			.andExpect(status().isBadRequest());
	}

	private UUID criarCliente(String nome, String documento) {
		Cliente cliente = new Cliente(UUID.randomUUID(), nome, new Documento(documento),
				nome.toLowerCase().replace(" ", ".") + "@teste.com", null);
		return clienteRepository.salvar(cliente).getId();
	}

}

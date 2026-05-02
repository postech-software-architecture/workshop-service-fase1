package com.postech.workshop_service.api.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.postech.workshop_service.api.dtos.LoginRequest;
import com.postech.workshop_service.config.PostgresTestContainer;
import com.postech.workshop_service.domain.entities.Cliente;
import com.postech.workshop_service.domain.entities.Usuario;
import com.postech.workshop_service.domain.enums.Role;
import com.postech.workshop_service.domain.repositories.ClienteRepository;
import com.postech.workshop_service.domain.repositories.UsuarioRepository;
import com.postech.workshop_service.domain.valueobjects.Documento;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class ClienteSecurityIT extends PostgresTestContainer {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Autowired
	private ClienteRepository clienteRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Test
	void shouldAllowClienteToReadOwnProfile() throws Exception {
		Cliente cliente = clienteRepository
			.salvar(new Cliente(null, "Cliente Auth", new Documento("98765432100"), "cliente@teste.com", null));
		usuarioRepository.salvar(new Usuario("cliente1", "cliente1@teste.com", passwordEncoder.encode("senha123"),
				Set.of(Role.CLIENTE), cliente.getId()));

		String accessToken = fazerLogin("cliente1", "senha123").get("accessToken").asText();

		mockMvc.perform(get("/api/v1/clientes/me").header("Authorization", "Bearer " + accessToken))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(cliente.getId().toString()))
			.andExpect(jsonPath("$.nome").value("Cliente Auth"));
	}

	@Test
	void shouldRejectClienteWithoutDomainLink() throws Exception {
		usuarioRepository.salvar(new Usuario("atendente", "atendente@teste.com", passwordEncoder.encode("senha123"),
				Set.of(Role.ATENDENTE), null));
		String accessToken = fazerLogin("atendente", "senha123").get("accessToken").asText();

		mockMvc.perform(get("/api/v1/clientes/me").header("Authorization", "Bearer " + accessToken))
			.andExpect(status().isForbidden());
	}

	private JsonNode fazerLogin(String identificador, String senha) throws Exception {
		LoginRequest request = new LoginRequest();
		request.setUsername(identificador);
		request.setPassword(senha);
		MvcResult result = mockMvc
			.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andReturn();
		return objectMapper.readTree(result.getResponse().getContentAsString());
	}

}

package com.postech.workshop_service.api.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.postech.workshop_service.api.dtos.LoginRequest;
import com.postech.workshop_service.config.PostgresTestContainer;
import com.postech.workshop_service.domain.entities.Usuario;
import com.postech.workshop_service.domain.enums.Role;
import com.postech.workshop_service.domain.repositories.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class SecurityAccessIT extends PostgresTestContainer {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Test
	void shouldReturn401ForProtectedEndpointWithoutToken() throws Exception {
		mockMvc.perform(get("/api/v1/clientes")).andExpect(status().isUnauthorized());
	}

	@Test
	void shouldAllowPublicRouteWithoutAuthentication() throws Exception {
		mockMvc.perform(get("/api/public/ordem-servico/rastreamento").param("codigo", "OS-123"))
			.andExpect(status().isNotFound());
	}

	@Test
	void shouldReturn403ForAuthenticatedUserWithoutRole() throws Exception {
		criarUsuario("atendente", "atendente@teste.com", "senha123", Set.of(Role.ATENDENTE));
		String accessToken = fazerLogin("atendente", "senha123").get("accessToken").asText();

		mockMvc
			.perform(delete("/api/v1/servicos/00000000-0000-0000-0000-000000000001").header("Authorization",
					"Bearer " + accessToken))
			.andExpect(status().isForbidden());
	}

	private Usuario criarUsuario(String username, String email, String senha, Set<Role> roles) {
		return usuarioRepository.salvar(new Usuario(username, email, passwordEncoder.encode(senha), roles, null));
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

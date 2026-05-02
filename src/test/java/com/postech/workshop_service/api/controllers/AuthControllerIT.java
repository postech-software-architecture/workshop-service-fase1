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

import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class AuthControllerIT extends PostgresTestContainer {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Test
	void shouldLoginByUsernameAndReadAuthenticatedIdentity() throws Exception {
		criarUsuario("admin", "admin@teste.com", "senha123", Set.of(Role.ADMINISTRADOR));

		LoginRequest request = new LoginRequest();
		request.setUsername("admin");
		request.setPassword("senha123");

		MvcResult resultadoLogin = mockMvc
			.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.accessToken").isNotEmpty())
			.andExpect(jsonPath("$.refreshToken").isNotEmpty())
			.andExpect(jsonPath("$.expiresIn").value(3600))
			.andReturn();

		String accessToken = objectMapper.readTree(resultadoLogin.getResponse().getContentAsString())
			.get("accessToken")
			.asText();

		mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + accessToken))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.username").value("admin"))
			.andExpect(jsonPath("$.roles[0]").value("ADMINISTRADOR"));
	}

	@Test
	void shouldLoginByEmail() throws Exception {
		criarUsuario("admin2", "admin2@teste.com", "senha123", Set.of(Role.ADMINISTRADOR));

		LoginRequest request = new LoginRequest();
		request.setUsername("admin2@teste.com");
		request.setPassword("senha123");

		mockMvc
			.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk());
	}

	@Test
	void shouldRejectInvalidCredentials() throws Exception {
		criarUsuario("admin3", "admin3@teste.com", "senha123", Set.of(Role.ADMINISTRADOR));

		LoginRequest request = new LoginRequest();
		request.setUsername("admin3");
		request.setPassword("senha-errada");

		mockMvc
			.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isUnauthorized());
	}

	private Usuario criarUsuario(String username, String email, String senha, Set<Role> roles) {
		return usuarioRepository.salvar(new Usuario(username, email, passwordEncoder.encode(senha), roles, null));
	}

}

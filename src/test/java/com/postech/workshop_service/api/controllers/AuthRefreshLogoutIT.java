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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class AuthRefreshLogoutIT extends PostgresTestContainer {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Test
	void shouldRotateRefreshTokenAndRejectOldOne() throws Exception {
		criarUsuario("admin", "admin@teste.com", "senha123", Set.of(Role.ADMINISTRADOR));

		JsonNode login = fazerLogin("admin", "senha123");
		String refreshToken = login.get("refreshToken").asText();

		MvcResult refreshResult = mockMvc
			.perform(post("/api/auth/refresh").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of("refreshToken", refreshToken))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.accessToken").isNotEmpty())
			.andExpect(jsonPath("$.refreshToken").isNotEmpty())
			.andReturn();

		String refreshTokenNovo = objectMapper.readTree(refreshResult.getResponse().getContentAsString())
			.get("refreshToken")
			.asText();

		mockMvc
			.perform(post("/api/auth/refresh").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of("refreshToken", refreshToken))))
			.andExpect(status().isUnauthorized());

		mockMvc
			.perform(post("/api/auth/refresh").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of("refreshToken", refreshTokenNovo))))
			.andExpect(status().isOk());
	}

	@Test
	void shouldLogoutOnlyTargetSession() throws Exception {
		criarUsuario("admin2", "admin2@teste.com", "senha123", Set.of(Role.ADMINISTRADOR));

		JsonNode login1 = fazerLogin("admin2", "senha123");
		JsonNode login2 = fazerLogin("admin2", "senha123");

		String refresh1 = login1.get("refreshToken").asText();
		String refresh2 = login2.get("refreshToken").asText();

		mockMvc
			.perform(post("/api/auth/logout").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of("refreshToken", refresh1))))
			.andExpect(status().isNoContent());

		mockMvc
			.perform(post("/api/auth/refresh").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of("refreshToken", refresh1))))
			.andExpect(status().isUnauthorized());

		mockMvc
			.perform(post("/api/auth/refresh").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of("refreshToken", refresh2))))
			.andExpect(status().isOk());
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

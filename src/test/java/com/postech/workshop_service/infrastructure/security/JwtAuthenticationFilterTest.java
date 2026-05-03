package com.postech.workshop_service.infrastructure.security;

import com.postech.workshop_service.domain.entities.Usuario;
import com.postech.workshop_service.domain.enums.Role;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

	@Mock
	private JwtTokenService jwtTokenService;

	@Mock
	private DetalhesUsuarioServiceImpl detalhesUsuarioService;

	@Mock
	private FilterChain filterChain;

	@AfterEach
	void cleanContext() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void shouldPopulateSecurityContextForEnabledAndUnlockedUser() throws Exception {
		UUID usuarioId = UUID.randomUUID();
		UsuarioAutenticadoPrincipal principal = UsuarioAutenticadoPrincipal.fromDomain(usuarioAtivo(usuarioId));
		JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtTokenService, detalhesUsuarioService);
		MockHttpServletRequest request = requestComBearerToken("token-valido");
		MockHttpServletResponse response = new MockHttpServletResponse();

		when(jwtTokenService.extrairUsuarioId("token-valido")).thenReturn(usuarioId);
		when(detalhesUsuarioService.carregarPorId(usuarioId)).thenReturn(principal);
		when(jwtTokenService.validarAccessToken("token-valido", usuarioId)).thenReturn(true);

		filter.doFilterInternal(request, response, filterChain);

		assertNotNull(SecurityContextHolder.getContext().getAuthentication());
		verify(filterChain).doFilter(request, response);
	}

	@Test
	void shouldNotPopulateSecurityContextForBlockedUser() throws Exception {
		UUID usuarioId = UUID.randomUUID();
		Usuario usuarioBloqueado = new Usuario(usuarioId, "admin", "admin@teste.com", "hash",
				Set.of(Role.ADMINISTRADOR), null, true, true, LocalDateTime.now(), LocalDateTime.now(), null);
		UsuarioAutenticadoPrincipal principal = UsuarioAutenticadoPrincipal.fromDomain(usuarioBloqueado);
		JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtTokenService, detalhesUsuarioService);
		MockHttpServletRequest request = requestComBearerToken("token-valido");
		MockHttpServletResponse response = new MockHttpServletResponse();

		when(jwtTokenService.extrairUsuarioId("token-valido")).thenReturn(usuarioId);
		when(detalhesUsuarioService.carregarPorId(usuarioId)).thenReturn(principal);
		when(jwtTokenService.validarAccessToken("token-valido", usuarioId)).thenReturn(true);

		filter.doFilterInternal(request, response, filterChain);

		assertNull(SecurityContextHolder.getContext().getAuthentication());
		verify(filterChain).doFilter(request, response);
	}

	@Test
	void shouldClearContextWhenTokenInvalidOrUserDisabled() throws Exception {
		UUID usuarioId = UUID.randomUUID();
		JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtTokenService, detalhesUsuarioService);
		MockHttpServletResponse response = new MockHttpServletResponse();
		UsuarioAutenticadoPrincipal principal = UsuarioAutenticadoPrincipal.fromDomain(usuarioAtivo(usuarioId));

		when(jwtTokenService.extrairUsuarioId("token-invalido")).thenReturn(usuarioId);
		when(detalhesUsuarioService.carregarPorId(usuarioId)).thenReturn(principal);
		when(jwtTokenService.validarAccessToken("token-invalido", usuarioId)).thenReturn(false);

		filter.doFilterInternal(requestComBearerToken("token-invalido"), response, filterChain);
		assertNull(SecurityContextHolder.getContext().getAuthentication());

		Usuario usuarioInativo = new Usuario(usuarioId, "admin", "admin@teste.com", "hash", Set.of(Role.ADMINISTRADOR),
				null, false, false, LocalDateTime.now(), LocalDateTime.now(), null);
		when(jwtTokenService.extrairUsuarioId("token-usuario-inativo")).thenReturn(usuarioId);
		when(detalhesUsuarioService.carregarPorId(usuarioId))
			.thenReturn(UsuarioAutenticadoPrincipal.fromDomain(usuarioInativo));
		when(jwtTokenService.validarAccessToken("token-usuario-inativo", usuarioId)).thenReturn(true);

		filter.doFilterInternal(requestComBearerToken("token-usuario-inativo"), response, filterChain);
		assertNull(SecurityContextHolder.getContext().getAuthentication());
	}

	@Test
	void shouldSkipWhenAuthorizationHeaderIsMissingOrNotBearer() throws Exception {
		JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtTokenService, detalhesUsuarioService);
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();

		filter.doFilterInternal(request, response, filterChain);
		verify(jwtTokenService, never()).extrairUsuarioId("token");

		MockHttpServletRequest basicRequest = new MockHttpServletRequest();
		basicRequest.addHeader("Authorization", "Basic token");
		filter.doFilterInternal(basicRequest, response, filterChain);

		assertNull(SecurityContextHolder.getContext().getAuthentication());
	}

	@Test
	void shouldKeepExistingAuthenticationAndClearContextForInvalidToken() throws Exception {
		UUID usuarioId = UUID.randomUUID();
		JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtTokenService, detalhesUsuarioService);
		MockHttpServletResponse response = new MockHttpServletResponse();

		SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("atual", null));
		MockHttpServletRequest existingAuthRequest = requestComBearerToken("token-valido");
		when(jwtTokenService.extrairUsuarioId("token-valido")).thenReturn(usuarioId);
		filter.doFilterInternal(existingAuthRequest, response, filterChain);
		verify(detalhesUsuarioService, never()).carregarPorId(usuarioId);
		assertNotNull(SecurityContextHolder.getContext().getAuthentication());

		SecurityContextHolder.clearContext();
		MockHttpServletRequest invalidRequest = requestComBearerToken("token-invalido");
		when(jwtTokenService.extrairUsuarioId("token-invalido")).thenThrow(new IllegalArgumentException("invalido"));
		filter.doFilterInternal(invalidRequest, response, filterChain);

		assertNull(SecurityContextHolder.getContext().getAuthentication());
	}

	private MockHttpServletRequest requestComBearerToken(String token) {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader("Authorization", "Bearer " + token);
		return request;
	}

	private Usuario usuarioAtivo(UUID usuarioId) {
		return new Usuario(usuarioId, "admin", "admin@teste.com", "hash", Set.of(Role.ADMINISTRADOR), null, true, false,
				LocalDateTime.now(), LocalDateTime.now(), null);
	}

}

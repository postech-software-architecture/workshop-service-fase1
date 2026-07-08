package com.postech.workshop_service.infrastructure.security;

import com.postech.workshop_service.application.usecases.IdentidadeAutenticada;
import com.postech.workshop_service.domain.entities.Usuario;
import com.postech.workshop_service.domain.enums.Role;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContextoSegurancaProviderImplTest {

	private final ContextoSegurancaProviderImpl provider = new ContextoSegurancaProviderImpl();

	@AfterEach
	void cleanContext() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void shouldMapPrincipalToIdentidade() {
		UUID clienteId = UUID.randomUUID();
		Usuario usuario = new Usuario(UUID.randomUUID(), "cliente1", "cliente@teste.com", "hash", Set.of(Role.CLIENTE),
				clienteId, true, false, LocalDateTime.now(), LocalDateTime.now(), null);
		UsuarioAutenticadoPrincipal principal = UsuarioAutenticadoPrincipal.fromDomain(usuario);
		SecurityContextHolder.getContext()
			.setAuthentication(new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));

		Optional<IdentidadeAutenticada> identidade = provider.identidadeAtual();

		assertTrue(identidade.isPresent());
		assertEquals(usuario.getId(), identidade.get().id());
		assertEquals("cliente1", identidade.get().username());
		assertEquals(clienteId, identidade.get().clienteId());
		assertTrue(identidade.get().roles().contains(Role.CLIENTE));
	}

	@Test
	void shouldReturnEmptyWhenNoAuthentication() {
		assertTrue(provider.identidadeAtual().isEmpty());
	}

	@Test
	void shouldDeriveIdentityFromUsernameWhenPrincipalIsNotUsuarioAutenticado() {
		SecurityContextHolder.getContext()
			.setAuthentication(new UsernamePasswordAuthenticationToken("mecanico", null,
					java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority(
							"ROLE_" + Role.MECANICO.name()))));

		Optional<IdentidadeAutenticada> identidade = provider.identidadeAtual();

		assertTrue(identidade.isPresent());
		assertEquals("mecanico", identidade.get().username());
		assertTrue(identidade.get().roles().contains(Role.MECANICO));
		assertTrue(identidade.get().clienteId() == null);
	}

	@Test
	void shouldReturnEmptyForAnonymousUser() {
		SecurityContextHolder.getContext()
			.setAuthentication(new UsernamePasswordAuthenticationToken("anonymousUser", null, java.util.List.of()));

		assertTrue(provider.identidadeAtual().isEmpty());
	}

}

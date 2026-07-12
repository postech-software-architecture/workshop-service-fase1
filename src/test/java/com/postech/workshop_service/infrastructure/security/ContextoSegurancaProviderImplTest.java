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
	void shouldReturnEmptyWhenPrincipalIsNotUsuarioAutenticado() {
		// Fail-closed: um principal que nao e UsuarioAutenticadoPrincipal (ex.: String de
		// um
		// usuario mock) NAO gera identidade sintetica — retorna vazio.
		SecurityContextHolder.getContext()
			.setAuthentication(new UsernamePasswordAuthenticationToken("mecanico", null,
					java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority(
							"ROLE_" + Role.MECANICO.name()))));

		assertTrue(provider.identidadeAtual().isEmpty());
	}

	@Test
	void shouldReturnEmptyForAnonymousUser() {
		SecurityContextHolder.getContext()
			.setAuthentication(new UsernamePasswordAuthenticationToken("anonymousUser", null, java.util.List.of()));

		assertTrue(provider.identidadeAtual().isEmpty());
	}

	@Test
	void shouldIgnoreUnknownRoleAuthoritiesWithout500() {
		// Authority com prefixo ROLE_ mas sufixo que nao e um Role conhecido (ex.:
		// ROLE_USER)
		// deve ser ignorada, sem lancar IllegalArgumentException (evita 500).
		Usuario usuario = new Usuario(UUID.randomUUID(), "cliente1", "cliente@teste.com", "hash", Set.of(Role.CLIENTE),
				UUID.randomUUID(), true, false, LocalDateTime.now(), LocalDateTime.now(), null);
		UsuarioAutenticadoPrincipal principal = org.mockito.Mockito.mock(UsuarioAutenticadoPrincipal.class);
		org.mockito.Mockito.when(principal.getId()).thenReturn(usuario.getId());
		org.mockito.Mockito.when(principal.getUsername()).thenReturn("cliente1");
		org.mockito.Mockito.when(principal.getClienteId()).thenReturn(usuario.getClienteId());
		org.mockito.Mockito.<java.util.Collection<? extends org.springframework.security.core.GrantedAuthority>>when(
				principal.getAuthorities())
			.thenReturn(java.util.List.of(
					new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER"),
					new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_CLIENTE")));
		SecurityContextHolder.getContext()
			.setAuthentication(new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));

		Optional<IdentidadeAutenticada> identidade = provider.identidadeAtual();

		assertTrue(identidade.isPresent());
		assertTrue(identidade.get().roles().contains(Role.CLIENTE));
		assertEquals(1, identidade.get().roles().size());
	}

}

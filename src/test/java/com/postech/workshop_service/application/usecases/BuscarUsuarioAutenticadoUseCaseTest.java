package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.application.exceptions.AcessoNegadoException;
import com.postech.workshop_service.domain.entities.Usuario;
import com.postech.workshop_service.domain.enums.Role;
import com.postech.workshop_service.infrastructure.security.UsuarioAutenticadoPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BuscarUsuarioAutenticadoUseCaseTest {

	private final BuscarUsuarioAutenticadoUseCase useCase = new BuscarUsuarioAutenticadoUseCase();

	@AfterEach
	void cleanContext() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void shouldReturnAuthenticatedUserData() {
		UUID clienteId = UUID.randomUUID();
		Usuario usuario = new Usuario(UUID.randomUUID(), "cliente1", "cliente@teste.com", "hash", Set.of(Role.CLIENTE),
				clienteId, true, false, LocalDateTime.now(), LocalDateTime.now(), null);
		UsuarioAutenticadoPrincipal principal = UsuarioAutenticadoPrincipal.fromDomain(usuario);
		SecurityContextHolder.getContext()
			.setAuthentication(new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));

		ResultadoUsuarioAutenticado resultado = useCase.executar();

		assertEquals(usuario.getId(), resultado.getId());
		assertEquals(clienteId, resultado.getClienteId());
	}

	@Test
	void shouldRequireClienteLinkForClienteContext() {
		Usuario usuario = new Usuario(UUID.randomUUID(), "admin", "admin@teste.com", "hash", Set.of(Role.ADMINISTRADOR),
				null, true, false, LocalDateTime.now(), LocalDateTime.now(), null);
		UsuarioAutenticadoPrincipal principal = UsuarioAutenticadoPrincipal.fromDomain(usuario);
		SecurityContextHolder.getContext()
			.setAuthentication(new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));

		assertThrows(AcessoNegadoException.class, useCase::obterClienteIdObrigatorio);
	}

	@Test
	void shouldRejectClienteRoleWithoutClienteIdInPrincipal() {
		UsuarioAutenticadoPrincipal principal = mock(UsuarioAutenticadoPrincipal.class);
		when(principal.getId()).thenReturn(UUID.randomUUID());
		when(principal.getUsername()).thenReturn("cliente");
		when(principal.getClienteId()).thenReturn(null);
		when(principal.getAuthorities())
			.thenAnswer(invocation -> java.util.List.of(new SimpleGrantedAuthority("ROLE_" + Role.CLIENTE.name())));
		SecurityContextHolder.getContext()
			.setAuthentication(new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));

		assertThrows(AcessoNegadoException.class, useCase::obterClienteIdObrigatorio);
	}

	@Test
	void shouldReturnRequiredClienteId() {
		UUID clienteId = UUID.randomUUID();
		Usuario usuario = new Usuario(UUID.randomUUID(), "cliente1", "cliente@teste.com", "hash", Set.of(Role.CLIENTE),
				clienteId, true, false, LocalDateTime.now(), LocalDateTime.now(), null);
		UsuarioAutenticadoPrincipal principal = UsuarioAutenticadoPrincipal.fromDomain(usuario);
		SecurityContextHolder.getContext()
			.setAuthentication(new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));

		assertEquals(clienteId, useCase.obterClienteIdObrigatorio());
	}

	@Test
	void shouldRejectMissingAuthentication() {
		assertThrows(AcessoNegadoException.class, useCase::executar);
		SecurityContextHolder.getContext()
			.setAuthentication(new UsernamePasswordAuthenticationToken("anonimo", null, java.util.List.of()));
		assertThrows(AcessoNegadoException.class, useCase::executar);
	}

}

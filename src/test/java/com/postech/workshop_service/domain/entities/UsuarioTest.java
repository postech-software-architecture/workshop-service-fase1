package com.postech.workshop_service.domain.entities;

import com.postech.workshop_service.domain.enums.Role;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UsuarioTest {

	@Test
	void shouldRequireClienteLinkWhenRoleIsCliente() {
		assertThrows(IllegalArgumentException.class,
				() -> new Usuario("cliente1", "cliente@teste.com", "hash", Set.of(Role.CLIENTE), null));
	}

	@Test
	void shouldRejectClienteLinkWithoutClienteRole() {
		assertThrows(IllegalArgumentException.class,
				() -> new Usuario("admin", "admin@teste.com", "hash", Set.of(Role.ADMINISTRADOR), UUID.randomUUID()));
	}

	@Test
	void shouldAllowValidClienteUserWithSingleClienteLink() {
		UUID clienteId = UUID.randomUUID();
		Usuario usuario = new Usuario("cliente1", "cliente@teste.com", "hash", Set.of(Role.CLIENTE), clienteId);
		assertEquals(clienteId, usuario.getClienteId());
		assertTrue(usuario.possuiRole(Role.CLIENTE));
		assertTrue(usuario.podeAutenticar());
	}

	@Test
	void shouldAllowInternalUserWithoutClienteLink() {
		assertDoesNotThrow(() -> new Usuario("admin", "admin@teste.com", "hash",
				Set.of(Role.ADMINISTRADOR, Role.ATENDENTE), null));
	}

	@Test
	void shouldSanitizeOptionalEmailAndRequiredFields() {
		Usuario usuario = new Usuario(" admin  geral ", " admin  geral@teste.com ", " hash-123 ",
				Set.of(Role.ADMINISTRADOR), null);

		assertEquals("admin geral", usuario.getUsername());
		assertEquals("admin geral@teste.com", usuario.getEmail());
		assertEquals("hash-123", usuario.getSenhaHash());
	}

	@Test
	void shouldRejectInvalidRoles() {
		assertThrows(IllegalArgumentException.class, () -> new Usuario("admin", "admin@teste.com", "hash", null, null));
		assertThrows(IllegalArgumentException.class,
				() -> new Usuario("admin", "admin@teste.com", "hash", Arrays.asList(Role.ADMINISTRADOR, null), null));
	}

	@Test
	void shouldUpdatePasswordAndAuthenticationState() {
		Usuario usuario = new Usuario("admin", "admin@teste.com", "hash", Set.of(Role.ADMINISTRADOR), null);

		usuario.atualizarSenha(" novo  hash ");
		usuario.bloquear();
		assertEquals("novo hash", usuario.getSenhaHash());
		assertFalse(usuario.podeAutenticar());

		usuario.desbloquear();
		assertTrue(usuario.podeAutenticar());
	}

	@Test
	void shouldRejectBlankPasswordUpdate() {
		Usuario usuario = new Usuario("admin", "admin@teste.com", "hash", Set.of(Role.ADMINISTRADOR), null);

		assertThrows(IllegalArgumentException.class, () -> usuario.atualizarSenha(" "));
	}

}

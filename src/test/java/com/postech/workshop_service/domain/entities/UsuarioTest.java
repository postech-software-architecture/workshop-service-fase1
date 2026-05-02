package com.postech.workshop_service.domain.entities;

import com.postech.workshop_service.domain.enums.Role;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
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

}

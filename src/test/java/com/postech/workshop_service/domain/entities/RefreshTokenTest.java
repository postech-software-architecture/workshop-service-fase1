package com.postech.workshop_service.domain.entities;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RefreshTokenTest {

	@Test
	void deveCriarRefreshTokenAtivo() {
		UUID usuarioId = UUID.randomUUID();
		RefreshToken refreshToken = new RefreshToken("  token-123 ", usuarioId, LocalDateTime.now().plusDays(1));

		assertNotNull(refreshToken.getId());
		assertEquals("token-123", refreshToken.getToken());
		assertEquals(usuarioId, refreshToken.getUsuarioId());
		assertTrue(refreshToken.estaAtivo());
		assertFalse(refreshToken.isRevogado());
		assertNull(refreshToken.getDataRevogacao());
	}

	@Test
	void deveReconstituirRefreshTokenExpiradoOuRevogado() {
		RefreshToken expirado = new RefreshToken(UUID.randomUUID(), "expirado", UUID.randomUUID(),
				LocalDateTime.now().minusDays(1), false, null, LocalDateTime.now(), LocalDateTime.now(), null);
		RefreshToken revogado = new RefreshToken(UUID.randomUUID(), "revogado", UUID.randomUUID(),
				LocalDateTime.now().plusDays(1), true, LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(),
				null);

		assertFalse(expirado.estaAtivo());
		assertFalse(revogado.estaAtivo());
	}

	@Test
	void deveRevogarComIdempotencia() {
		RefreshToken refreshToken = new RefreshToken("token-123", UUID.randomUUID(), LocalDateTime.now().plusDays(1));

		refreshToken.revogar();
		LocalDateTime primeiraRevogacao = refreshToken.getDataRevogacao();
		refreshToken.revogar();

		assertTrue(refreshToken.isRevogado());
		assertEquals(primeiraRevogacao, refreshToken.getDataRevogacao());
	}

	@Test
	void deveValidarCamposObrigatorios() {
		assertThrows(IllegalArgumentException.class,
				() -> new RefreshToken(" ", UUID.randomUUID(), LocalDateTime.now().plusDays(1)));
		assertThrows(IllegalArgumentException.class,
				() -> new RefreshToken("token", null, LocalDateTime.now().plusDays(1)));
		assertThrows(IllegalArgumentException.class, () -> new RefreshToken("token", UUID.randomUUID(), null));
	}

}

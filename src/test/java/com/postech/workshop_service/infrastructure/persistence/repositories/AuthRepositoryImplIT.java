package com.postech.workshop_service.infrastructure.persistence.repositories;

import com.postech.workshop_service.config.PostgresTestContainer;
import com.postech.workshop_service.domain.entities.RefreshToken;
import com.postech.workshop_service.domain.entities.Usuario;
import com.postech.workshop_service.domain.enums.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Transactional
class AuthRepositoryImplIT extends PostgresTestContainer {

	@Autowired
	private UsuarioRepositoryImpl usuarioRepository;

	@Autowired
	private RefreshTokenRepositoryImpl refreshTokenRepository;

	@Test
	void shouldPersistUpdateAndFindUserByUsernameEmailAndId() {
		Usuario usuario = usuarioRepository
			.salvar(new Usuario("admin", "admin@teste.com", "hash", Set.of(Role.ADMINISTRADOR), null));

		assertTrue(usuarioRepository.buscarPorUsernameOuEmail("ADMIN").isPresent());
		assertTrue(usuarioRepository.buscarPorUsernameOuEmail("ADMIN@TESTE.COM").isPresent());
		assertTrue(usuarioRepository.buscarPorId(usuario.getId()).isPresent());

		usuario.bloquear();
		Usuario atualizado = usuarioRepository.salvar(usuario);

		assertFalse(atualizado.podeAutenticar());
	}

	@Test
	void shouldPersistUpdateAndFindRefreshToken() {
		Usuario usuario = usuarioRepository
			.salvar(new Usuario("admin", "admin@teste.com", "hash", Set.of(Role.ADMINISTRADOR), null));
		RefreshToken refreshToken = refreshTokenRepository
			.salvar(new RefreshToken("refresh-123", usuario.getId(), LocalDateTime.now().plusDays(1)));

		assertTrue(refreshTokenRepository.buscarPorToken("refresh-123").isPresent());

		refreshToken.revogar();
		RefreshToken revogado = refreshTokenRepository.salvar(refreshToken);

		assertEquals("refresh-123", revogado.getToken());
		assertFalse(refreshTokenRepository.buscarPorToken("refresh-123").orElseThrow().estaAtivo());
	}

}

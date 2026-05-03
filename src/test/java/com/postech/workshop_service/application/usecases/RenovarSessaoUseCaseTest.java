package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.application.exceptions.TokenInvalidoException;
import com.postech.workshop_service.application.exceptions.ContaInativaException;
import com.postech.workshop_service.domain.entities.RefreshToken;
import com.postech.workshop_service.domain.entities.Usuario;
import com.postech.workshop_service.domain.enums.Role;
import com.postech.workshop_service.domain.repositories.RefreshTokenRepository;
import com.postech.workshop_service.domain.repositories.UsuarioRepository;
import com.postech.workshop_service.infrastructure.security.JwtTokenService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RenovarSessaoUseCaseTest {

	@Mock
	private RefreshTokenRepository refreshTokenRepository;

	@Mock
	private UsuarioRepository usuarioRepository;

	@Mock
	private JwtTokenService jwtTokenService;

	@InjectMocks
	private RenovarSessaoUseCase useCase;

	@Test
	void shouldRotateRefreshTokenSuccessfully() {
		Usuario usuario = new Usuario(UUID.randomUUID(), "admin", "admin@teste.com", "hash", Set.of(Role.ADMINISTRADOR),
				null, true, false, LocalDateTime.now(), LocalDateTime.now(), null);
		RefreshToken refreshToken = new RefreshToken(UUID.randomUUID(), "refresh-antigo", usuario.getId(),
				LocalDateTime.now().plusDays(1), false, null, LocalDateTime.now(), LocalDateTime.now(), null);

		when(refreshTokenRepository.buscarPorToken("refresh-antigo")).thenReturn(Optional.of(refreshToken));
		when(usuarioRepository.buscarPorId(usuario.getId())).thenReturn(Optional.of(usuario));
		when(jwtTokenService.gerarAccessToken(usuario)).thenReturn("access-novo");
		when(jwtTokenService.gerarRefreshToken()).thenReturn("refresh-novo");
		when(jwtTokenService.calcularExpiracaoRefreshToken()).thenReturn(LocalDateTime.now().plusDays(7));
		when(jwtTokenService.getExpiracaoAccessSegundos()).thenReturn(3600L);
		when(refreshTokenRepository.salvar(any(RefreshToken.class)))
			.thenAnswer(invocation -> invocation.getArgument(0, RefreshToken.class));

		ResultadoAutenticacao resultado = useCase.executar("refresh-antigo");

		assertEquals("access-novo", resultado.getAccessToken());
		assertEquals("refresh-novo", resultado.getRefreshToken());
	}

	@Test
	void shouldRejectRevokedOrExpiredRefreshToken() {
		RefreshToken refreshToken = new RefreshToken(UUID.randomUUID(), "refresh-revogado", UUID.randomUUID(),
				LocalDateTime.now().minusDays(1), false, null, LocalDateTime.now(), LocalDateTime.now(), null);
		when(refreshTokenRepository.buscarPorToken("refresh-revogado")).thenReturn(Optional.of(refreshToken));

		assertThrows(TokenInvalidoException.class, () -> useCase.executar("refresh-revogado"));
	}

	@Test
	void shouldRejectUnknownRefreshTokenOrUser() {
		when(refreshTokenRepository.buscarPorToken("inexistente")).thenReturn(Optional.empty());
		assertThrows(TokenInvalidoException.class, () -> useCase.executar("inexistente"));

		RefreshToken refreshToken = new RefreshToken(UUID.randomUUID(), "refresh", UUID.randomUUID(),
				LocalDateTime.now().plusDays(1), false, null, LocalDateTime.now(), LocalDateTime.now(), null);
		when(refreshTokenRepository.buscarPorToken("refresh")).thenReturn(Optional.of(refreshToken));
		when(usuarioRepository.buscarPorId(refreshToken.getUsuarioId())).thenReturn(Optional.empty());

		assertThrows(TokenInvalidoException.class, () -> useCase.executar("refresh"));
	}

	@Test
	void shouldRejectInactiveUser() {
		Usuario usuario = new Usuario(UUID.randomUUID(), "admin", "admin@teste.com", "hash", Set.of(Role.ADMINISTRADOR),
				null, false, false, LocalDateTime.now(), LocalDateTime.now(), null);
		RefreshToken refreshToken = new RefreshToken(UUID.randomUUID(), "refresh", usuario.getId(),
				LocalDateTime.now().plusDays(1), false, null, LocalDateTime.now(), LocalDateTime.now(), null);
		when(refreshTokenRepository.buscarPorToken("refresh")).thenReturn(Optional.of(refreshToken));
		when(usuarioRepository.buscarPorId(usuario.getId())).thenReturn(Optional.of(usuario));

		assertThrows(ContaInativaException.class, () -> useCase.executar("refresh"));
	}

}

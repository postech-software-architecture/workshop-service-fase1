package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.application.exceptions.TokenInvalidoException;
import com.postech.workshop_service.domain.entities.RefreshToken;
import com.postech.workshop_service.domain.repositories.RefreshTokenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EncerrarSessaoUseCaseTest {

	@Mock
	private RefreshTokenRepository refreshTokenRepository;

	@InjectMocks
	private EncerrarSessaoUseCase useCase;

	@Test
	void shouldRevokeRefreshTokenForLogout() {
		RefreshToken refreshToken = new RefreshToken(UUID.randomUUID(), "refresh-logout", UUID.randomUUID(),
				LocalDateTime.now().plusDays(1), false, null, LocalDateTime.now(), LocalDateTime.now(), null);
		when(refreshTokenRepository.buscarPorToken("refresh-logout")).thenReturn(Optional.of(refreshToken));
		when(refreshTokenRepository.salvar(any(RefreshToken.class)))
			.thenAnswer(invocation -> invocation.getArgument(0, RefreshToken.class));

		useCase.executar("refresh-logout");

		verify(refreshTokenRepository).salvar(any(RefreshToken.class));
	}

	@Test
	void shouldRejectInvalidRefreshTokenOnLogout() {
		when(refreshTokenRepository.buscarPorToken("invalido")).thenReturn(Optional.empty());

		assertThrows(TokenInvalidoException.class, () -> useCase.executar("invalido"));
	}

}

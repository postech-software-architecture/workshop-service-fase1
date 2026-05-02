package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.application.exceptions.TokenInvalidoException;
import com.postech.workshop_service.domain.entities.RefreshToken;
import com.postech.workshop_service.domain.repositories.RefreshTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso responsavel por revogar um refresh token especifico.
 */
@Service
public class EncerrarSessaoUseCase {

	private final RefreshTokenRepository refreshTokenRepository;

	public EncerrarSessaoUseCase(RefreshTokenRepository refreshTokenRepository) {
		this.refreshTokenRepository = refreshTokenRepository;
	}

	/**
	 * Revoga apenas a sessao identificada pelo refresh token informado.
	 * @param refreshTokenValor valor do refresh token recebido no logout.
	 */
	@Transactional
	public void executar(String refreshTokenValor) {
		RefreshToken refreshToken = refreshTokenRepository.buscarPorToken(refreshTokenValor)
			.orElseThrow(() -> new TokenInvalidoException("Refresh token invalido."));
		if (!refreshToken.estaAtivo()) {
			throw new TokenInvalidoException("Refresh token expirado ou revogado.");
		}
		refreshToken.revogar();
		refreshTokenRepository.salvar(refreshToken);
	}

}

package com.postech.workshop_service.domain.repositories;

import com.postech.workshop_service.domain.entities.RefreshToken;

import java.util.Optional;

/**
 * Repositorio de refresh tokens persistidos.
 */
public interface RefreshTokenRepository {

	/**
	 * Persiste um refresh token.
	 * @param refreshToken credencial a ser persistida.
	 * @return credencial persistida.
	 */
	RefreshToken salvar(RefreshToken refreshToken);

	/**
	 * Busca um refresh token pelo valor opaco.
	 * @param token valor do refresh token.
	 * @return credencial encontrada, se existir.
	 */
	Optional<RefreshToken> buscarPorToken(String token);

}

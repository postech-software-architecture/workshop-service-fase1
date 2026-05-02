package com.postech.workshop_service.infrastructure.persistence.repositories;

import com.postech.workshop_service.infrastructure.persistence.entities.RefreshTokenJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio Spring Data de refresh tokens.
 */
public interface JpaRefreshTokenRepository extends JpaRepository<RefreshTokenJpaEntity, UUID> {

	/**
	 * Busca uma credencial de renovacao pelo valor opaco do token.
	 * @param token valor do token.
	 * @return credencial encontrada, se existir.
	 */
	Optional<RefreshTokenJpaEntity> findByToken(String token);

}

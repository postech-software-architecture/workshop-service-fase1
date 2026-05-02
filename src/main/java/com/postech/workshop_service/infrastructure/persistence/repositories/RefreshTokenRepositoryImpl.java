package com.postech.workshop_service.infrastructure.persistence.repositories;

import com.postech.workshop_service.domain.entities.RefreshToken;
import com.postech.workshop_service.domain.repositories.RefreshTokenRepository;
import com.postech.workshop_service.infrastructure.persistence.entities.RefreshTokenJpaEntity;
import com.postech.workshop_service.infrastructure.persistence.entities.UsuarioJpaEntity;
import com.postech.workshop_service.infrastructure.persistence.mappers.RefreshTokenMapper;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Adaptador JPA do repositorio de refresh tokens.
 */
@Component
@Transactional
public class RefreshTokenRepositoryImpl implements RefreshTokenRepository {

	private final JpaRefreshTokenRepository jpaRefreshTokenRepository;

	private final RefreshTokenMapper refreshTokenMapper;

	private final EntityManager entityManager;

	public RefreshTokenRepositoryImpl(JpaRefreshTokenRepository jpaRefreshTokenRepository,
			RefreshTokenMapper refreshTokenMapper, EntityManager entityManager) {
		this.jpaRefreshTokenRepository = jpaRefreshTokenRepository;
		this.refreshTokenMapper = refreshTokenMapper;
		this.entityManager = entityManager;
	}

	@Override
	public RefreshToken salvar(RefreshToken refreshToken) {
		RefreshTokenJpaEntity entity = refreshToken.getId() != null
				? jpaRefreshTokenRepository.findById(refreshToken.getId()).map(existing -> {
					refreshTokenMapper.updateEntityFromDomain(refreshToken, existing);
					return existing;
				}).orElseGet(() -> refreshTokenMapper.toEntity(refreshToken))
				: refreshTokenMapper.toEntity(refreshToken);

		entity.setUsuario(entityManager.getReference(UsuarioJpaEntity.class, refreshToken.getUsuarioId()));
		RefreshTokenJpaEntity salvo = jpaRefreshTokenRepository.save(entity);
		return refreshTokenMapper.toDomain(salvo);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<RefreshToken> buscarPorToken(String token) {
		return jpaRefreshTokenRepository.findByToken(token).map(refreshTokenMapper::toDomain);
	}

}

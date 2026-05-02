package com.postech.workshop_service.infrastructure.persistence.mappers;

import com.postech.workshop_service.domain.entities.RefreshToken;
import com.postech.workshop_service.infrastructure.persistence.entities.RefreshTokenJpaEntity;
import com.postech.workshop_service.infrastructure.persistence.entities.UsuarioJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.UUID;

/**
 * Mapper entre dominio e persistencia do refresh token.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RefreshTokenMapper {

	@Mapping(target = "usuario", ignore = true)
	RefreshTokenJpaEntity toEntity(RefreshToken refreshToken);

	@Mapping(target = "usuarioId", source = "usuario")
	RefreshToken toDomain(RefreshTokenJpaEntity entity);

	@Mapping(target = "usuario", ignore = true)
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "dataCriacao", ignore = true)
	void updateEntityFromDomain(RefreshToken refreshToken, @MappingTarget RefreshTokenJpaEntity entity);

	default UUID map(UsuarioJpaEntity usuario) {
		return usuario != null ? usuario.getId() : null;
	}

}

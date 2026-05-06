package com.postech.workshop_service.infrastructure.persistence.mappers;

import com.postech.workshop_service.domain.entities.Usuario;
import com.postech.workshop_service.infrastructure.persistence.entities.ClienteJpaEntity;
import com.postech.workshop_service.infrastructure.persistence.entities.UsuarioJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.UUID;

/**
 * Mapper entre dominio e persistencia da conta autenticavel.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UsuarioMapper {

	@Mapping(target = "cliente", ignore = true)
	UsuarioJpaEntity toEntity(Usuario usuario);

	@Mapping(target = "clienteId", source = "cliente")
	Usuario toDomain(UsuarioJpaEntity entity);

	@Mapping(target = "cliente", ignore = true)
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "dataCriacao", ignore = true)
	void updateEntityFromDomain(Usuario usuario, @MappingTarget UsuarioJpaEntity entity);

	default UUID map(ClienteJpaEntity cliente) {
		return cliente != null ? cliente.getId() : null;
	}

}

package com.postech.workshop_service.infrastructure.persistence.mappers;

import com.postech.workshop_service.domain.entities.Estoque;
import com.postech.workshop_service.infrastructure.persistence.entities.EstoqueJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

/**
 * Mapper responsavel por converter a entidade Estoque entre dominio e persistencia.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EstoqueMapper {

	@Mapping(target = "versao", ignore = true)
	EstoqueJpaEntity toEntity(Estoque estoque);

	default Estoque toDomain(EstoqueJpaEntity entity) {
		if (entity == null) {
			return null;
		}
		return new Estoque(entity.getId(), entity.getPecaInsumoId(), entity.getLocalizacao(), entity.getQuantidade(),
				Boolean.TRUE.equals(entity.getAtivo()), entity.getVersao(), entity.getDataCriacao(),
				entity.getDataUltimaAtualizacao());
	}

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "pecaInsumoId", ignore = true)
	@Mapping(target = "dataCriacao", ignore = true)
	void updateEntityFromDomain(Estoque domain, @MappingTarget EstoqueJpaEntity entity);

}

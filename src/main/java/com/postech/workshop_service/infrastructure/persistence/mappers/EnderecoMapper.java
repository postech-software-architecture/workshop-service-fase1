package com.postech.workshop_service.infrastructure.persistence.mappers;

import com.postech.workshop_service.domain.entities.Endereco;
import com.postech.workshop_service.infrastructure.persistence.entities.EnderecoJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EnderecoMapper {

	@Mapping(target = "cliente", ignore = true)
	@Mapping(target = "dataCriacao", ignore = true)
	@Mapping(target = "dataUltimaAtualizacao", ignore = true)
	EnderecoJpaEntity toEntity(Endereco domain);

	Endereco toDomain(EnderecoJpaEntity entity);

}

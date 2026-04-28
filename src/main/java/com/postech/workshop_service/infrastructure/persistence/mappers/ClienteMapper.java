package com.postech.workshop_service.infrastructure.persistence.mappers;

import com.postech.workshop_service.domain.entities.Cliente;
import com.postech.workshop_service.domain.valueobjects.Documento;
import com.postech.workshop_service.infrastructure.persistence.entities.ClienteJpaEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = { EnderecoMapper.class })
public interface ClienteMapper {

	@Mapping(target = "documento", source = "documento.valor")
	@Mapping(target = "endereco", source = "endereco")
	ClienteJpaEntity toEntity(Cliente cliente);

	@Mapping(target = "documento", source = "documento")
	@Mapping(target = "endereco", source = "endereco")
	Cliente toDomain(ClienteJpaEntity entity);

	default Documento map(String valor) {
		return valor != null ? new Documento(valor) : null;
	}

	@AfterMapping
	default void linkAddress(@MappingTarget ClienteJpaEntity entity) {
		if (entity.getEndereco() != null) {
			entity.getEndereco().setCliente(entity);
			entity.getEndereco().setClienteId(entity.getId());
			if (entity.getEndereco().getDataCriacao() == null) {
				entity.getEndereco().setDataCriacao(entity.getDataCriacao());
				entity.getEndereco().setDataUltimaAtualizacao(entity.getDataUltimaAtualizacao());
			}
		}
	}

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "documento", source = "documento.valor")
	@Mapping(target = "dataCriacao", ignore = true)
	@Mapping(target = "dataUltimaAtualizacao", ignore = true)
	@Mapping(target = "dataRemocao", source = "dataRemocao")
	void updateEntityFromDomain(Cliente domain, @MappingTarget ClienteJpaEntity entity);

}

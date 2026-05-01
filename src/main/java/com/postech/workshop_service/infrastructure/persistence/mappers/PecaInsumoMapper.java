package com.postech.workshop_service.infrastructure.persistence.mappers;

import com.postech.workshop_service.domain.entities.PecaInsumo;
import com.postech.workshop_service.domain.valueobjects.TipoItem;
import com.postech.workshop_service.domain.valueobjects.UnidadeMedida;
import com.postech.workshop_service.infrastructure.persistence.entities.PecaInsumoJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

/**
 * Mapper responsavel por converter o agregado de peca entre dominio e persistencia.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PecaInsumoMapper {

	@Mapping(target = "versao", ignore = true)
	@Mapping(target = "unidadeMedida", expression = "java(mapUnidadeMedida(pecaInsumo.getUnidadeMedida()))")
	@Mapping(target = "tipoItem", expression = "java(mapTipoItem(pecaInsumo.getTipoItem()))")
	PecaInsumoJpaEntity toEntity(PecaInsumo pecaInsumo);

	default PecaInsumo toDomain(PecaInsumoJpaEntity entity) {
		if (entity == null) {
			return null;
		}
		return new PecaInsumo(entity.getId(), entity.getSku(), entity.getNome(), entity.getValorUnitario(),
				entity.getEstoqueMinimo(), UnidadeMedida.valueOf(entity.getUnidadeMedida()),
				TipoItem.valueOf(entity.getTipoItem()), entity.getFornecedor(), entity.getCodigoBarras(),
				entity.getMarca(), entity.getCategoria(), entity.getAplicacao(), entity.getObservacoes(),
				Boolean.TRUE.equals(entity.getAtivo()), entity.getVersao(), entity.getDataCriacao(),
				entity.getDataUltimaAtualizacao(), entity.getDataRemocao());
	}

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "sku", ignore = true)
	@Mapping(target = "dataCriacao", ignore = true)
	@Mapping(target = "unidadeMedida", expression = "java(mapUnidadeMedida(domain.getUnidadeMedida()))")
	@Mapping(target = "tipoItem", expression = "java(mapTipoItem(domain.getTipoItem()))")
	void updateEntityFromDomain(PecaInsumo domain, @MappingTarget PecaInsumoJpaEntity entity);

	default String mapUnidadeMedida(UnidadeMedida unidadeMedida) {
		return unidadeMedida != null ? unidadeMedida.name() : null;
	}

	default String mapTipoItem(TipoItem tipoItem) {
		return tipoItem != null ? tipoItem.name() : null;
	}

}

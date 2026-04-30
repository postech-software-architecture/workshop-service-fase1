package com.postech.workshop_service.infrastructure.persistence.mappers;

import com.postech.workshop_service.domain.entities.ItemOrcamento;
import com.postech.workshop_service.domain.entities.Orcamento;
import com.postech.workshop_service.infrastructure.persistence.entities.ItemOrcamentoJpaEntity;
import com.postech.workshop_service.infrastructure.persistence.entities.OrcamentoJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * Mapper entre o agregado de orcamento e sua representacao persistente.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrcamentoMapper {

	@Mapping(target = "itens", source = "itens")
	OrcamentoJpaEntity toEntity(Orcamento orcamento);

	@Mapping(target = "itens", source = "itens")
	Orcamento toDomain(OrcamentoJpaEntity entity);

	ItemOrcamentoJpaEntity toEntity(ItemOrcamento item);

	ItemOrcamento toDomain(ItemOrcamentoJpaEntity item);

}

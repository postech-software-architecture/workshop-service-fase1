package com.postech.workshop_service.infrastructure.persistence.mappers;

import com.postech.workshop_service.domain.entities.ItemComposicaoTecnica;
import com.postech.workshop_service.domain.entities.OrdemServico;
import com.postech.workshop_service.infrastructure.persistence.entities.ItemComposicaoTecnicaJpaEntity;
import com.postech.workshop_service.infrastructure.persistence.entities.OrdemServicoJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * Mapper entre o agregado de ordem de servico e sua representacao persistente.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrdemServicoMapper {

	@Mapping(target = "itensComposicao", source = "itensComposicao")
	OrdemServicoJpaEntity toEntity(OrdemServico ordemServico);

	@Mapping(target = "itensComposicao", source = "itensComposicao")
	OrdemServico toDomain(OrdemServicoJpaEntity entity);

	ItemComposicaoTecnicaJpaEntity toEntity(ItemComposicaoTecnica item);

	ItemComposicaoTecnica toDomain(ItemComposicaoTecnicaJpaEntity item);

}

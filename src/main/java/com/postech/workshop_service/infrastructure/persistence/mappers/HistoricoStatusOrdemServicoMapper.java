package com.postech.workshop_service.infrastructure.persistence.mappers;

import com.postech.workshop_service.domain.entities.HistoricoStatusOrdemServico;
import com.postech.workshop_service.infrastructure.persistence.entities.HistoricoStatusOrdemServicoJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * Mapper entre historico de status de OS e sua representacao persistente.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface HistoricoStatusOrdemServicoMapper {

	HistoricoStatusOrdemServicoJpaEntity toEntity(HistoricoStatusOrdemServico historico);

	HistoricoStatusOrdemServico toDomain(HistoricoStatusOrdemServicoJpaEntity entity);

}

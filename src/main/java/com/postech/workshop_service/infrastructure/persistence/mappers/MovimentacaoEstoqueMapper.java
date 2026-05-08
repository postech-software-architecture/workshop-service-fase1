package com.postech.workshop_service.infrastructure.persistence.mappers;

import com.postech.workshop_service.domain.entities.MovimentacaoEstoque;
import com.postech.workshop_service.domain.valueobjects.TipoMovimentacao;
import com.postech.workshop_service.infrastructure.persistence.entities.MovimentacaoEstoqueJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * Mapper responsavel por converter a entidade MovimentacaoEstoque entre dominio e
 * persistencia.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MovimentacaoEstoqueMapper {

	@Mapping(target = "tipo", expression = "java(mapTipoMovimentacao(movimentacao.getTipo()))")
	MovimentacaoEstoqueJpaEntity toEntity(MovimentacaoEstoque movimentacao);

	default MovimentacaoEstoque toDomain(MovimentacaoEstoqueJpaEntity entity) {
		if (entity == null) {
			return null;
		}
		return new MovimentacaoEstoque(entity.getId(), entity.getEstoqueId(),
				TipoMovimentacao.valueOf(entity.getTipo()), entity.getQuantidade(), entity.getQuantidadeAnterior(),
				entity.getQuantidadePosterior(), entity.getMotivo(), entity.getOrdemServicoId(),
				entity.getOrcamentoId(), entity.getDataMovimentacao(), entity.getDataCriacao());
	}

	default String mapTipoMovimentacao(TipoMovimentacao tipo) {
		return tipo != null ? tipo.name() : null;
	}

}

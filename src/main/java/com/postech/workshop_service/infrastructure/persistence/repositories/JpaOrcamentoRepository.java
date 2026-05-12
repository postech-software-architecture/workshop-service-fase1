package com.postech.workshop_service.infrastructure.persistence.repositories;

import com.postech.workshop_service.domain.entities.StatusOrcamento;
import com.postech.workshop_service.infrastructure.persistence.entities.OrcamentoJpaEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio Spring Data para persistencia de orcamentos.
 */
public interface JpaOrcamentoRepository extends JpaRepository<OrcamentoJpaEntity, UUID> {

	@Override
	@EntityGraph(attributePaths = "itens")
	Optional<OrcamentoJpaEntity> findById(UUID id);

	@EntityGraph(attributePaths = "itens")
	List<OrcamentoJpaEntity> findByIdOrdemServicoOrderByDataCriacaoDesc(UUID idOrdemServico);

	boolean existsByIdOrdemServicoAndStatus(UUID idOrdemServico, StatusOrcamento status);

}

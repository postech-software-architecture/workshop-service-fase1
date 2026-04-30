package com.postech.workshop_service.infrastructure.persistence.repositories;

import com.postech.workshop_service.infrastructure.persistence.entities.OrdemServicoJpaEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio Spring Data para persistencia da ordem de servico.
 */
public interface JpaOrdemServicoRepository extends JpaRepository<OrdemServicoJpaEntity, UUID> {

	@Override
	@EntityGraph(attributePaths = "itensComposicao")
	Optional<OrdemServicoJpaEntity> findById(UUID id);

}

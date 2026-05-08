package com.postech.workshop_service.infrastructure.persistence.repositories;

import com.postech.workshop_service.infrastructure.persistence.entities.HistoricoStatusOrdemServicoJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Repositorio Spring Data para historico de status da ordem de servico.
 */
public interface JpaHistoricoStatusOrdemServicoRepository
		extends JpaRepository<HistoricoStatusOrdemServicoJpaEntity, UUID> {

	List<HistoricoStatusOrdemServicoJpaEntity> findByIdOrdemServicoOrderByDataTransicaoAsc(UUID idOrdemServico);

}

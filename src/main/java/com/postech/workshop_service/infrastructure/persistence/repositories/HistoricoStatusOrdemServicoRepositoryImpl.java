package com.postech.workshop_service.infrastructure.persistence.repositories;

import com.postech.workshop_service.domain.entities.HistoricoStatusOrdemServico;
import com.postech.workshop_service.domain.repositories.HistoricoStatusOrdemServicoRepository;
import com.postech.workshop_service.infrastructure.persistence.mappers.HistoricoStatusOrdemServicoMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Adaptador de persistencia do historico de status da ordem de servico.
 */
@Component
@Transactional
public class HistoricoStatusOrdemServicoRepositoryImpl implements HistoricoStatusOrdemServicoRepository {

	private final JpaHistoricoStatusOrdemServicoRepository jpaRepository;

	private final HistoricoStatusOrdemServicoMapper mapper;

	public HistoricoStatusOrdemServicoRepositoryImpl(JpaHistoricoStatusOrdemServicoRepository jpaRepository,
			HistoricoStatusOrdemServicoMapper mapper) {
		this.jpaRepository = jpaRepository;
		this.mapper = mapper;
	}

	@Override
	public HistoricoStatusOrdemServico salvar(HistoricoStatusOrdemServico historico) {
		return mapper.toDomain(jpaRepository.save(mapper.toEntity(historico)));
	}

	@Override
	@Transactional(readOnly = true)
	public List<HistoricoStatusOrdemServico> listarPorOrdemServico(UUID idOrdemServico) {
		return jpaRepository.findByIdOrdemServicoOrderByDataTransicaoAsc(idOrdemServico)
			.stream()
			.map(mapper::toDomain)
			.toList();
	}

}

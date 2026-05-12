package com.postech.workshop_service.infrastructure.persistence.repositories;

import com.postech.workshop_service.domain.entities.Orcamento;
import com.postech.workshop_service.domain.entities.StatusOrcamento;
import com.postech.workshop_service.domain.repositories.OrcamentoRepository;
import com.postech.workshop_service.infrastructure.persistence.mappers.OrcamentoMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador de persistencia do agregado de orcamento.
 */
@Component
@Transactional
public class OrcamentoRepositoryImpl implements OrcamentoRepository {

	private final JpaOrcamentoRepository jpaOrcamentoRepository;

	private final OrcamentoMapper orcamentoMapper;

	/**
	 * Construtor para injecao das dependencias do adaptador.
	 * @param jpaOrcamentoRepository repositorio Spring Data.
	 * @param orcamentoMapper mapper do agregado.
	 */
	public OrcamentoRepositoryImpl(JpaOrcamentoRepository jpaOrcamentoRepository, OrcamentoMapper orcamentoMapper) {
		this.jpaOrcamentoRepository = jpaOrcamentoRepository;
		this.orcamentoMapper = orcamentoMapper;
	}

	@Override
	public Orcamento salvar(Orcamento orcamento) {
		return orcamentoMapper.toDomain(jpaOrcamentoRepository.save(orcamentoMapper.toEntity(orcamento)));
	}

	@Override
	public Optional<Orcamento> buscarPorId(UUID id) {
		return jpaOrcamentoRepository.findById(id).map(orcamentoMapper::toDomain);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Orcamento> listarPorOrdemServico(UUID idOrdemServico) {
		return jpaOrcamentoRepository.findByIdOrdemServicoOrderByDataCriacaoDesc(idOrdemServico)
			.stream()
			.map(orcamentoMapper::toDomain)
			.toList();
	}

	@Override
	public boolean existePendenteAprovacaoPorOrdemServico(UUID idOrdemServico) {
		return jpaOrcamentoRepository.existsByIdOrdemServicoAndStatus(idOrdemServico,
				StatusOrcamento.PENDENTE_APROVACAO);
	}

}

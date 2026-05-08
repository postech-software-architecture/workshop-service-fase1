package com.postech.workshop_service.infrastructure.persistence.repositories;

import com.postech.workshop_service.domain.entities.MovimentacaoEstoque;
import com.postech.workshop_service.domain.repositories.MovimentacaoEstoqueRepository;
import com.postech.workshop_service.domain.valueobjects.TipoMovimentacao;
import com.postech.workshop_service.infrastructure.persistence.entities.MovimentacaoEstoqueJpaEntity;
import com.postech.workshop_service.infrastructure.persistence.mappers.MovimentacaoEstoqueMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador de persistencia para movimentacoes de estoque.
 */
@Component
@Transactional
public class MovimentacaoEstoqueRepositoryImpl implements MovimentacaoEstoqueRepository {

	private final JpaMovimentacaoEstoqueRepository jpaMovimentacaoEstoqueRepository;

	private final MovimentacaoEstoqueMapper movimentacaoEstoqueMapper;

	/**
	 * Construtor para injecao de dependencias.
	 * @param jpaMovimentacaoEstoqueRepository repositorio Spring Data.
	 * @param movimentacaoEstoqueMapper mapper de dominio/persistencia.
	 */
	public MovimentacaoEstoqueRepositoryImpl(JpaMovimentacaoEstoqueRepository jpaMovimentacaoEstoqueRepository,
			MovimentacaoEstoqueMapper movimentacaoEstoqueMapper) {
		this.jpaMovimentacaoEstoqueRepository = jpaMovimentacaoEstoqueRepository;
		this.movimentacaoEstoqueMapper = movimentacaoEstoqueMapper;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public MovimentacaoEstoque salvar(MovimentacaoEstoque movimentacao) {
		MovimentacaoEstoqueJpaEntity entity = movimentacaoEstoqueMapper.toEntity(movimentacao);
		return movimentacaoEstoqueMapper.toDomain(jpaMovimentacaoEstoqueRepository.save(entity));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public Optional<MovimentacaoEstoque> buscarPorId(UUID id) {
		return jpaMovimentacaoEstoqueRepository.findById(id).map(movimentacaoEstoqueMapper::toDomain);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public List<MovimentacaoEstoque> listarPorEstoque(UUID estoqueId, TipoMovimentacao tipo, LocalDateTime dataInicio,
			LocalDateTime dataFim) {
		List<MovimentacaoEstoqueJpaEntity> entities = jpaMovimentacaoEstoqueRepository
			.findByEstoqueIdOrderByDataMovimentacaoDesc(estoqueId);
		return filtrar(entities, tipo, dataInicio, dataFim).stream().map(movimentacaoEstoqueMapper::toDomain).toList();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public List<MovimentacaoEstoque> listarPorPeca(UUID pecaInsumoId, TipoMovimentacao tipo, LocalDateTime dataInicio,
			LocalDateTime dataFim) {
		List<MovimentacaoEstoqueJpaEntity> entities = jpaMovimentacaoEstoqueRepository
			.findByPecaInsumoIdOrderByDataMovimentacaoDesc(pecaInsumoId);
		return filtrar(entities, tipo, dataInicio, dataFim).stream().map(movimentacaoEstoqueMapper::toDomain).toList();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public List<MovimentacaoEstoque> listarPorOrdemServico(UUID ordemServicoId) {
		return jpaMovimentacaoEstoqueRepository.findByOrdemServicoIdOrderByDataMovimentacaoDesc(ordemServicoId)
			.stream()
			.map(movimentacaoEstoqueMapper::toDomain)
			.toList();
	}

	private List<MovimentacaoEstoqueJpaEntity> filtrar(List<MovimentacaoEstoqueJpaEntity> entities,
			TipoMovimentacao tipo, LocalDateTime dataInicio, LocalDateTime dataFim) {
		return entities.stream()
			.filter(entity -> tipo == null || entity.getTipo().equals(tipo.name()))
			.filter(entity -> dataInicio == null || !entity.getDataMovimentacao().isBefore(dataInicio))
			.filter(entity -> dataFim == null || !entity.getDataMovimentacao().isAfter(dataFim))
			.toList();
	}

}

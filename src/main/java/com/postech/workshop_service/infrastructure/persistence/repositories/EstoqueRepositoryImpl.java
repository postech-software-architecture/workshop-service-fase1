package com.postech.workshop_service.infrastructure.persistence.repositories;

import com.postech.workshop_service.domain.entities.Estoque;
import com.postech.workshop_service.domain.repositories.EstoqueRepository;
import com.postech.workshop_service.infrastructure.persistence.entities.EstoqueJpaEntity;
import com.postech.workshop_service.infrastructure.persistence.mappers.EstoqueMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador de persistencia para estoques.
 */
@Component
@Transactional
public class EstoqueRepositoryImpl implements EstoqueRepository {

	private final JpaEstoqueRepository jpaEstoqueRepository;

	private final EstoqueMapper estoqueMapper;

	/**
	 * Construtor para injecao de dependencias.
	 * @param jpaEstoqueRepository repositorio Spring Data.
	 * @param estoqueMapper mapper de dominio/persistencia.
	 */
	public EstoqueRepositoryImpl(JpaEstoqueRepository jpaEstoqueRepository, EstoqueMapper estoqueMapper) {
		this.jpaEstoqueRepository = jpaEstoqueRepository;
		this.estoqueMapper = estoqueMapper;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Estoque salvar(Estoque estoque) {
		EstoqueJpaEntity entity = jpaEstoqueRepository.findById(estoque.getId()).map(existente -> {
			estoqueMapper.updateEntityFromDomain(estoque, existente);
			return existente;
		}).orElseGet(() -> estoqueMapper.toEntity(estoque));

		return estoqueMapper.toDomain(jpaEstoqueRepository.save(entity));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public Optional<Estoque> buscarPorId(UUID id, boolean incluirInativos) {
		return jpaEstoqueRepository.findById(id)
			.filter(entity -> incluirInativos || Boolean.TRUE.equals(entity.getAtivo()))
			.map(estoqueMapper::toDomain);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public List<Estoque> listarPorPeca(UUID pecaInsumoId, boolean incluirInativos) {
		List<EstoqueJpaEntity> entities = incluirInativos ? jpaEstoqueRepository.findByPecaInsumoId(pecaInsumoId)
				: jpaEstoqueRepository.findByPecaInsumoIdAndAtivoTrue(pecaInsumoId);
		return entities.stream().map(estoqueMapper::toDomain).toList();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public List<Estoque> listarPorPecaOrdenadoPorQuantidadeDisponivel(UUID pecaInsumoId, boolean incluirInativos) {
		return listarPorPeca(pecaInsumoId, incluirInativos).stream()
			.sorted((a, b) -> b.getQuantidade().compareTo(a.getQuantidade()))
			.toList();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public BigDecimal calcularQuantidadeTotal(UUID pecaInsumoId) {
		return jpaEstoqueRepository.calcularQuantidadeTotal(pecaInsumoId);
	}

	/**
	 * {@inheritDoc}
	 */
	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public Optional<Estoque> buscarPorPecaELocalizacao(UUID pecaInsumoId, String localizacao) {
		return jpaEstoqueRepository.findByPecaInsumoIdAndLocalizacao(pecaInsumoId, localizacao)
			.filter(entity -> Boolean.TRUE.equals(entity.getAtivo()))
			.map(estoqueMapper::toDomain);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public boolean existeLocalizacao(UUID pecaInsumoId, String localizacao, UUID estoqueIdIgnorado) {
		if (estoqueIdIgnorado == null) {
			return jpaEstoqueRepository.existsByPecaInsumoIdAndLocalizacao(pecaInsumoId, localizacao);
		}
		return jpaEstoqueRepository.existsByPecaInsumoIdAndLocalizacaoAndIdNot(pecaInsumoId, localizacao,
				estoqueIdIgnorado);
	}

}

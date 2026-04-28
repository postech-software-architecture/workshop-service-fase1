package com.postech.workshop_service.infrastructure.persistence.repositories;

import com.postech.workshop_service.domain.entities.Veiculo;
import com.postech.workshop_service.domain.repositories.PaginaResultado;
import com.postech.workshop_service.domain.repositories.VeiculoRepository;
import com.postech.workshop_service.infrastructure.persistence.entities.VeiculoJpaEntity;
import com.postech.workshop_service.infrastructure.persistence.mappers.VeiculoMapper;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador de persistencia do agregado de veiculos.
 */
@Component
@Transactional
public class VeiculoRepositoryImpl implements VeiculoRepository {

	private final JpaVeiculoRepository jpaVeiculoRepository;

	private final VeiculoMapper veiculoMapper;

	/**
	 * Construtor para injecao de dependencias.
	 * @param jpaVeiculoRepository repositorio Spring Data.
	 * @param veiculoMapper mapper de dominio/persistencia.
	 */
	public VeiculoRepositoryImpl(JpaVeiculoRepository jpaVeiculoRepository, VeiculoMapper veiculoMapper) {
		this.jpaVeiculoRepository = jpaVeiculoRepository;
		this.veiculoMapper = veiculoMapper;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Veiculo salvar(Veiculo veiculo) {
		VeiculoJpaEntity entity = jpaVeiculoRepository.findById(veiculo.getId()).map(existente -> {
			veiculoMapper.updateEntityFromDomain(veiculo, existente);
			return existente;
		}).orElseGet(() -> veiculoMapper.toEntity(veiculo));

		return veiculoMapper.toDomain(jpaVeiculoRepository.save(entity));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public Optional<Veiculo> buscarPorId(UUID id, boolean incluirInativos) {
		return jpaVeiculoRepository.findById(id)
			.filter(entity -> incluirInativos || Boolean.TRUE.equals(entity.getAtivo()))
			.map(veiculoMapper::toDomain);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public Optional<Veiculo> buscarPorPlaca(String placaNormalizada, boolean incluirInativos) {
		return jpaVeiculoRepository.findByPlaca(placaNormalizada)
			.filter(entity -> incluirInativos || Boolean.TRUE.equals(entity.getAtivo()))
			.map(veiculoMapper::toDomain);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public PaginaResultado<Veiculo> listar(int pagina, int tamanho, String placaNormalizada, UUID clienteId,
			boolean incluirInativos) {
		Specification<VeiculoJpaEntity> spec = filtrarPor(placaNormalizada, clienteId, incluirInativos);
		Page<VeiculoJpaEntity> resultado = jpaVeiculoRepository.findAll(spec, PageRequest.of(pagina, tamanho));
		List<Veiculo> itens = resultado.getContent().stream().map(veiculoMapper::toDomain).toList();
		return new PaginaResultado<>(itens, resultado.getTotalElements(), resultado.getTotalPages(), pagina, tamanho);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public List<Veiculo> listarPorCliente(UUID clienteId, boolean incluirInativos) {
		Specification<VeiculoJpaEntity> spec = filtrarPor(null, clienteId, incluirInativos);
		return jpaVeiculoRepository.findAll(spec).stream().map(veiculoMapper::toDomain).toList();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public boolean existePlacaAtiva(String placaNormalizada, UUID veiculoIdIgnorado) {
		if (veiculoIdIgnorado == null) {
			return jpaVeiculoRepository.existsByPlacaAndAtivoTrue(placaNormalizada);
		}
		return jpaVeiculoRepository.existsByPlacaAndAtivoTrueAndIdNot(placaNormalizada, veiculoIdIgnorado);
	}

	private Specification<VeiculoJpaEntity> filtrarPor(String placaNormalizada, UUID clienteId,
			boolean incluirInativos) {
		return (root, query, criteriaBuilder) -> {
			query.distinct(true);
			var predicates = criteriaBuilder.conjunction();

			if (!incluirInativos) {
				predicates = criteriaBuilder.and(predicates, criteriaBuilder.isTrue(root.get("ativo")));
			}
			if (placaNormalizada != null) {
				predicates = criteriaBuilder.and(predicates,
						criteriaBuilder.equal(root.get("placa"), placaNormalizada));
			}
			if (clienteId != null) {
				var join = root.join("clientesVinculados", JoinType.INNER);
				predicates = criteriaBuilder.and(predicates, criteriaBuilder.equal(join.get("clienteId"), clienteId));
			}
			return predicates;
		};
	}

}

package com.postech.workshop_service.infrastructure.persistence.repositories;

import com.postech.workshop_service.domain.entities.PecaInsumo;
import com.postech.workshop_service.domain.repositories.PaginaResultado;
import com.postech.workshop_service.domain.repositories.PecaInsumoRepository;
import com.postech.workshop_service.infrastructure.persistence.entities.PecaInsumoJpaEntity;
import com.postech.workshop_service.infrastructure.persistence.mappers.PecaInsumoMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador de persistencia do agregado de pecas e insumos.
 */
@Component
@Transactional
public class PecaInsumoRepositoryImpl implements PecaInsumoRepository {

	private final JpaPecaInsumoRepository jpaPecaInsumoRepository;

	private final PecaInsumoMapper pecaInsumoMapper;

	/**
	 * Construtor para injecao de dependencias.
	 * @param jpaPecaInsumoRepository repositorio Spring Data.
	 * @param pecaInsumoMapper mapper de dominio/persistencia.
	 */
	public PecaInsumoRepositoryImpl(JpaPecaInsumoRepository jpaPecaInsumoRepository,
			PecaInsumoMapper pecaInsumoMapper) {
		this.jpaPecaInsumoRepository = jpaPecaInsumoRepository;
		this.pecaInsumoMapper = pecaInsumoMapper;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PecaInsumo salvar(PecaInsumo pecaInsumo) {
		PecaInsumoJpaEntity entity = jpaPecaInsumoRepository.findById(pecaInsumo.getId()).map(existente -> {
			pecaInsumoMapper.updateEntityFromDomain(pecaInsumo, existente);
			return existente;
		}).orElseGet(() -> pecaInsumoMapper.toEntity(pecaInsumo));

		return pecaInsumoMapper.toDomain(jpaPecaInsumoRepository.save(entity));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public Optional<PecaInsumo> buscarPorId(UUID id, boolean incluirInativos) {
		return jpaPecaInsumoRepository.findById(id)
			.filter(entity -> incluirInativos || Boolean.TRUE.equals(entity.getAtivo()))
			.map(pecaInsumoMapper::toDomain);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public Optional<PecaInsumo> buscarPorSku(String skuNormalizado, boolean incluirInativos) {
		return jpaPecaInsumoRepository.findBySku(skuNormalizado)
			.filter(entity -> incluirInativos || Boolean.TRUE.equals(entity.getAtivo()))
			.map(pecaInsumoMapper::toDomain);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public PaginaResultado<PecaInsumo> listar(int pagina, int tamanho, String nome, String categoria,
			boolean incluirInativos) {
		Specification<PecaInsumoJpaEntity> spec = filtrarPor(nome, categoria, incluirInativos);
		Page<PecaInsumoJpaEntity> resultado = jpaPecaInsumoRepository.findAll(spec, PageRequest.of(pagina, tamanho));
		List<PecaInsumo> itens = resultado.getContent().stream().map(pecaInsumoMapper::toDomain).toList();
		return new PaginaResultado<>(itens, resultado.getTotalElements(), resultado.getTotalPages(), pagina, tamanho);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public boolean existeSkuAtivo(String skuNormalizado, UUID pecaIdIgnorado) {
		if (pecaIdIgnorado == null) {
			return jpaPecaInsumoRepository.existsBySkuAndAtivoTrue(skuNormalizado);
		}
		return jpaPecaInsumoRepository.existsBySkuAndAtivoTrueAndIdNot(skuNormalizado, pecaIdIgnorado);
	}

	private Specification<PecaInsumoJpaEntity> filtrarPor(String nome, String categoria, boolean incluirInativos) {
		return (root, query, criteriaBuilder) -> {
			var predicates = criteriaBuilder.conjunction();

			if (!incluirInativos) {
				predicates = criteriaBuilder.and(predicates, criteriaBuilder.isTrue(root.get("ativo")));
			}
			if (nome != null && !nome.isBlank()) {
				predicates = criteriaBuilder.and(predicates,
						criteriaBuilder.like(criteriaBuilder.lower(root.get("nome")), "%" + nome.toLowerCase() + "%"));
			}
			if (categoria != null && !categoria.isBlank()) {
				predicates = criteriaBuilder.and(predicates,
						criteriaBuilder.equal(criteriaBuilder.lower(root.get("categoria")), categoria.toLowerCase()));
			}
			return predicates;
		};
	}

}

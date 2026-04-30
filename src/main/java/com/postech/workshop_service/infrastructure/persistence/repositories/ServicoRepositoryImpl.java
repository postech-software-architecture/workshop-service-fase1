package com.postech.workshop_service.infrastructure.persistence.repositories;

import com.postech.workshop_service.domain.entities.Servico;
import com.postech.workshop_service.domain.enums.CategoriaServico;
import com.postech.workshop_service.domain.repositories.PaginaResultado;
import com.postech.workshop_service.domain.repositories.ServicoRepository;
import com.postech.workshop_service.infrastructure.persistence.entities.ServicoJpaEntity;
import com.postech.workshop_service.infrastructure.persistence.mappers.ServicoMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador de persistencia do agregado de servicos.
 */
@Component
@Transactional
public class ServicoRepositoryImpl implements ServicoRepository {

	private final JpaServicoRepository jpaServicoRepository;

	private final ServicoMapper servicoMapper;

	/**
	 * Construtor para injecao de dependencias.
	 * @param jpaServicoRepository repositorio Spring Data.
	 * @param servicoMapper mapper de dominio/persistencia.
	 */
	public ServicoRepositoryImpl(JpaServicoRepository jpaServicoRepository, ServicoMapper servicoMapper) {
		this.jpaServicoRepository = jpaServicoRepository;
		this.servicoMapper = servicoMapper;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Servico salvar(Servico servico) {
		ServicoJpaEntity entity = jpaServicoRepository.findById(servico.getId()).map(existente -> {
			servicoMapper.updateEntityFromDomain(servico, existente);
			return existente;
		}).orElseGet(() -> servicoMapper.toEntity(servico));

		return servicoMapper.toDomain(jpaServicoRepository.save(entity));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public Optional<Servico> buscarPorId(UUID id) {
		return jpaServicoRepository.findById(id).map(servicoMapper::toDomain);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public PaginaResultado<Servico> listar(int pagina, int tamanho, String nome, CategoriaServico categoria,
			boolean incluirInativos) {
		Specification<ServicoJpaEntity> spec = filtrarPor(nome, categoria, incluirInativos);
		Page<ServicoJpaEntity> resultado = jpaServicoRepository.findAll(spec, PageRequest.of(pagina, tamanho));
		List<Servico> itens = resultado.getContent().stream().map(servicoMapper::toDomain).toList();
		return new PaginaResultado<>(itens, resultado.getTotalElements(), resultado.getTotalPages(), pagina, tamanho);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public List<Servico> listarPorCategoria(CategoriaServico categoria, boolean incluirInativos) {
		Specification<ServicoJpaEntity> spec = filtrarPor(null, categoria, incluirInativos);
		return jpaServicoRepository.findAll(spec).stream().map(servicoMapper::toDomain).toList();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public boolean existeNomeAtivo(String nome, UUID idExcluir) {
		if (idExcluir == null) {
			return jpaServicoRepository.existsByNomeAndAtivoTrue(nome);
		}
		return jpaServicoRepository.existsByNomeAndAtivoTrueAndIdNot(nome, idExcluir);
	}

	private Specification<ServicoJpaEntity> filtrarPor(String nome, CategoriaServico categoria,
			boolean incluirInativos) {
		return (root, query, criteriaBuilder) -> {
			query.distinct(true);
			var predicates = criteriaBuilder.conjunction();

			if (!incluirInativos) {
				predicates = criteriaBuilder.and(predicates, criteriaBuilder.isTrue(root.get("ativo")));
			}
			if (nome != null && !nome.isBlank()) {
				predicates = criteriaBuilder.and(predicates,
						criteriaBuilder.like(criteriaBuilder.lower(root.get("nome")), "%" + nome.toLowerCase() + "%"));
			}
			if (categoria != null) {
				predicates = criteriaBuilder.and(predicates, criteriaBuilder.equal(root.get("categoria"), categoria));
			}
			return predicates;
		};
	}

}

package com.postech.workshop_service.infrastructure.persistence.repositories;

import com.postech.workshop_service.domain.entities.OrdemServico;
import com.postech.workshop_service.domain.repositories.FiltrosOrdemServico;
import com.postech.workshop_service.domain.repositories.OrdemServicoRepository;
import com.postech.workshop_service.domain.repositories.PaginaResultado;
import com.postech.workshop_service.infrastructure.persistence.entities.OrdemServicoJpaEntity;
import com.postech.workshop_service.infrastructure.persistence.mappers.OrdemServicoMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Adaptador de persistencia do agregado de ordem de servico.
 */
@Component
@Transactional
public class OrdemServicoRepositoryImpl implements OrdemServicoRepository {

	private final JpaOrdemServicoRepository jpaOrdemServicoRepository;

	private final OrdemServicoMapper ordemServicoMapper;

	/**
	 * Construtor para injecao das dependencias do adaptador.
	 * @param jpaOrdemServicoRepository repositorio Spring Data.
	 * @param ordemServicoMapper mapper da entidade.
	 */
	public OrdemServicoRepositoryImpl(JpaOrdemServicoRepository jpaOrdemServicoRepository,
			OrdemServicoMapper ordemServicoMapper) {
		this.jpaOrdemServicoRepository = jpaOrdemServicoRepository;
		this.ordemServicoMapper = ordemServicoMapper;
	}

	@Override
	public OrdemServico salvar(OrdemServico ordemServico) {
		return ordemServicoMapper.toDomain(jpaOrdemServicoRepository.save(ordemServicoMapper.toEntity(ordemServico)));
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<OrdemServico> buscarPorId(UUID id) {
		return jpaOrdemServicoRepository.findById(id).map(ordemServicoMapper::toDomain);
	}

	@Override
	public String gerarProximoNumero(int ano) {
		String prefixo = "OS-" + ano + "-%";
		int sequencial = jpaOrdemServicoRepository.buscarProximoSequencial(prefixo);
		return String.format("OS-%d-%05d", ano, sequencial);
	}

	@Override
	@Transactional(readOnly = true)
	public PaginaResultado<OrdemServico> listar(int pagina, int tamanho, FiltrosOrdemServico filtros) {
		FiltrosOrdemServico criterios = filtros != null ? filtros : FiltrosOrdemServico.vazio();
		Specification<OrdemServicoJpaEntity> spec = filtrarPor(criterios);

		// Two-phase fetch: paginar IDs primeiro para evitar HHH000104 (collection
		// fetch + paginacao com EAGER + OrderColumn).
		PageRequest pageRequest = PageRequest.of(pagina, tamanho, Sort.by(Sort.Direction.DESC, "dataCriacao"));
		Page<UUID> paginaIds = jpaOrdemServicoRepository.findAll(spec, pageRequest).map(OrdemServicoJpaEntity::getId);

		List<UUID> ids = paginaIds.getContent();
		if (ids.isEmpty()) {
			return new PaginaResultado<>(List.of(), paginaIds.getTotalElements(), paginaIds.getTotalPages(), pagina,
					tamanho);
		}

		List<OrdemServicoJpaEntity> entidades = jpaOrdemServicoRepository.findAllWithItensByIdIn(ids);
		// Merge function (a, b) -> a evita IllegalStateException quando o EntityGraph
		// gera linhas duplicadas para a mesma OS por causa do fetch da colecao EAGER.
		Map<UUID, OrdemServicoJpaEntity> porId = entidades.stream()
			.collect(Collectors.toMap(OrdemServicoJpaEntity::getId, Function.identity(),
					(primeira, ignorada) -> primeira));

		List<OrdemServico> itens = ids.stream().map(porId::get).map(ordemServicoMapper::toDomain).toList();

		return new PaginaResultado<>(itens, paginaIds.getTotalElements(), paginaIds.getTotalPages(), pagina, tamanho);
	}

	private Specification<OrdemServicoJpaEntity> filtrarPor(FiltrosOrdemServico filtros) {
		return (root, query, criteriaBuilder) -> {
			var predicates = criteriaBuilder.conjunction();
			if (filtros.status() != null) {
				predicates = criteriaBuilder.and(predicates,
						criteriaBuilder.equal(root.get("status"), filtros.status()));
			}
			if (filtros.idCliente() != null) {
				predicates = criteriaBuilder.and(predicates,
						criteriaBuilder.equal(root.get("idCliente"), filtros.idCliente()));
			}
			if (filtros.dataInicio() != null) {
				predicates = criteriaBuilder.and(predicates,
						criteriaBuilder.greaterThanOrEqualTo(root.get("dataCriacao"), filtros.dataInicio()));
			}
			if (filtros.dataFim() != null) {
				predicates = criteriaBuilder.and(predicates,
						criteriaBuilder.lessThan(root.get("dataCriacao"), filtros.dataFim()));
			}
			return predicates;
		};
	}

}

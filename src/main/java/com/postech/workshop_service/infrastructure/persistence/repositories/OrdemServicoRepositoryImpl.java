package com.postech.workshop_service.infrastructure.persistence.repositories;

import com.postech.workshop_service.domain.entities.OrdemServico;
import com.postech.workshop_service.domain.entities.StatusOrdemServico;
import com.postech.workshop_service.domain.repositories.FiltrosOrdemServico;
import com.postech.workshop_service.domain.repositories.OrdemServicoRepository;
import com.postech.workshop_service.domain.repositories.PaginaResultado;
import com.postech.workshop_service.infrastructure.persistence.entities.OrdemServicoJpaEntity;
import com.postech.workshop_service.infrastructure.persistence.mappers.OrdemServicoMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Root;
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

	@PersistenceContext
	private EntityManager entityManager;

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
	@Transactional(readOnly = true)
	public Optional<OrdemServico> buscarPorNumero(String numero) {
		return jpaOrdemServicoRepository.findByNumero(numero).map(ordemServicoMapper::toDomain);
	}

	@Override
	public String gerarProximoNumero(int ano) {
		String prefixo = "OS-" + ano + "-%";
		// Serializa a geracao do sequencial por ano: o advisory lock e mantido ate o
		// commit da transacao do caso de uso (apos o insert), impedindo que dois nos
		// leiam o mesmo MAX e gerem numeros duplicados sob concorrencia.
		entityManager.createNativeQuery("SELECT pg_advisory_xact_lock(hashtext(:prefixo))")
			.setParameter("prefixo", prefixo)
			.getSingleResult();
		int sequencial = jpaOrdemServicoRepository.buscarProximoSequencial(prefixo);
		return String.format("OS-%d-%05d", ano, sequencial);
	}

	@Override
	@Transactional(readOnly = true)
	public PaginaResultado<OrdemServico> listar(int pagina, int tamanho, FiltrosOrdemServico filtros) {
		FiltrosOrdemServico criterios = filtros != null ? filtros : FiltrosOrdemServico.vazio();
		Specification<OrdemServicoJpaEntity> spec = filtrarPor(criterios);

		// Two-phase fetch: paginar IDs primeiro para evitar HHH000104 (collection
		// fetch + paginacao com EAGER + OrderColumn). No modo fila, a ordenacao composta
		// (prioridade de status + antiguidade) vive no Specification via
		// query.orderBy(...),
		// entao o PageRequest vai sem Sort para nao conflitar.
		PageRequest pageRequest = criterios.apenasFilaTrabalho() ? PageRequest.of(pagina, tamanho)
				: PageRequest.of(pagina, tamanho, Sort.by(Sort.Direction.DESC, "dataCriacao"));
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

		// A ordem da primeira fase (paginacao de IDs) e preservada ao hidratar.
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
			if (filtros.apenasFilaTrabalho()) {
				predicates = criteriaBuilder.and(predicates,
						criteriaBuilder.not(root.get("status").in(StatusOrdemServico.ENCERRADOS)));
				ordenarPorFila(root, query, criteriaBuilder);
			}
			return predicates;
		};
	}

	/**
	 * Ordena a fila de trabalho por prioridade de status (mais urgente primeiro) e,
	 * dentro do mesmo status, por antiguidade (dataCriacao ASC). A prioridade e expressa
	 * via CASE porque a ordem natural do enum no banco nao corresponde a prioridade
	 * desejada.
	 */
	private void ordenarPorFila(Root<OrdemServicoJpaEntity> root, CriteriaQuery<?> query,
			CriteriaBuilder criteriaBuilder) {
		Expression<Object> prioridade = criteriaBuilder.selectCase()
			.when(criteriaBuilder.equal(root.get("status"), StatusOrdemServico.EM_EXECUCAO),
					StatusOrdemServico.EM_EXECUCAO.prioridadeFila())
			.when(criteriaBuilder.equal(root.get("status"), StatusOrdemServico.AGUARDANDO_APROVACAO),
					StatusOrdemServico.AGUARDANDO_APROVACAO.prioridadeFila())
			.when(criteriaBuilder.equal(root.get("status"), StatusOrdemServico.EM_DIAGNOSTICO),
					StatusOrdemServico.EM_DIAGNOSTICO.prioridadeFila())
			.when(criteriaBuilder.equal(root.get("status"), StatusOrdemServico.RECEBIDO),
					StatusOrdemServico.RECEBIDO.prioridadeFila())
			.otherwise(Integer.MAX_VALUE);
		query.orderBy(criteriaBuilder.asc(prioridade), criteriaBuilder.asc(root.get("dataCriacao")));
	}

}

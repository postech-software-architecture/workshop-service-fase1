package com.postech.workshop_service.infrastructure.persistence.repositories;

import com.postech.workshop_service.infrastructure.persistence.entities.OrdemServicoJpaEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio Spring Data para persistencia da ordem de servico.
 */
public interface JpaOrdemServicoRepository
		extends JpaRepository<OrdemServicoJpaEntity, UUID>, JpaSpecificationExecutor<OrdemServicoJpaEntity> {

	@Override
	@EntityGraph(attributePaths = "itensComposicao")
	Optional<OrdemServicoJpaEntity> findById(UUID id);

	/**
	 * Carrega ordens de servico hidratando a colecao de itens em uma unica consulta,
	 * evitando o problema de paginacao em memoria quando combinada com Specification.
	 * @param ids identificadores das ordens de servico desejadas.
	 * @return entidades hidratadas.
	 */
	@EntityGraph(attributePaths = "itensComposicao")
	List<OrdemServicoJpaEntity> findAllWithItensByIdIn(Collection<UUID> ids);

	/**
	 * Calcula o proximo sequencial disponivel para o prefixo de ano informado. Usa
	 * SPLIT_PART do PostgreSQL para extrair a parte numerica do numero da OS.
	 * @param prefixo padrao LIKE, ex: {@code OS-2026-%}.
	 * @return proximo inteiro sequencial (MAX atual + 1, ou 1 se nao houver registros).
	 */
	@Query(value = "SELECT COALESCE(MAX(CAST(SPLIT_PART(numero, '-', 3) AS INTEGER)), 0) + 1 "
			+ "FROM ordens_servico WHERE numero LIKE :prefixo", nativeQuery = true)
	int buscarProximoSequencial(@Param("prefixo") String prefixo);

}

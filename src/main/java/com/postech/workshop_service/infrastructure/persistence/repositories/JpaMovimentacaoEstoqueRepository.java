package com.postech.workshop_service.infrastructure.persistence.repositories;

import com.postech.workshop_service.infrastructure.persistence.entities.MovimentacaoEstoqueJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio Spring Data para persistencia de movimentacoes de estoque.
 */
public interface JpaMovimentacaoEstoqueRepository extends JpaRepository<MovimentacaoEstoqueJpaEntity, UUID> {

	/**
	 * Busca movimentacoes por estoque ordenadas por data decrescente.
	 * @param estoqueId identificador do estoque.
	 * @return lista de movimentacoes.
	 */
	List<MovimentacaoEstoqueJpaEntity> findByEstoqueIdOrderByDataMovimentacaoDesc(UUID estoqueId);

	/**
	 * Busca movimentacoes por estoque e tipo.
	 * @param estoqueId identificador do estoque.
	 * @param tipo tipo de movimentacao.
	 * @return lista de movimentacoes.
	 */
	List<MovimentacaoEstoqueJpaEntity> findByEstoqueIdAndTipoOrderByDataMovimentacaoDesc(UUID estoqueId, String tipo);

	/**
	 * Busca movimentacoes por estoque com filtros de data.
	 * @param estoqueId identificador do estoque.
	 * @param dataInicio data inicial.
	 * @param dataFim data final.
	 * @return lista de movimentacoes.
	 */
	@Query("SELECT m FROM MovimentacaoEstoqueJpaEntity m WHERE m.estoqueId = :estoqueId "
			+ "AND (:dataInicio IS NULL OR m.dataMovimentacao >= :dataInicio) "
			+ "AND (:dataFim IS NULL OR m.dataMovimentacao <= :dataFim) " + "ORDER BY m.dataMovimentacao DESC")
	List<MovimentacaoEstoqueJpaEntity> findByEstoqueIdAndPeriodo(UUID estoqueId, LocalDateTime dataInicio,
			LocalDateTime dataFim);

	/**
	 * Busca movimentacoes por estoque com todos os filtros.
	 * @param estoqueId identificador do estoque.
	 * @param tipo tipo de movimentacao.
	 * @param dataInicio data inicial.
	 * @param dataFim data final.
	 * @return lista de movimentacoes.
	 */
	@Query("SELECT m FROM MovimentacaoEstoqueJpaEntity m WHERE m.estoqueId = :estoqueId "
			+ "AND (:tipo IS NULL OR m.tipo = :tipo) "
			+ "AND (:dataInicio IS NULL OR m.dataMovimentacao >= :dataInicio) "
			+ "AND (:dataFim IS NULL OR m.dataMovimentacao <= :dataFim) " + "ORDER BY m.dataMovimentacao DESC")
	List<MovimentacaoEstoqueJpaEntity> findByEstoqueIdWithFilters(UUID estoqueId, String tipo, LocalDateTime dataInicio,
			LocalDateTime dataFim);

	/**
	 * Busca movimentacoes de uma peca (todos os estoques).
	 * @param pecaInsumoId identificador da peca.
	 * @param tipo tipo de movimentacao (opcional).
	 * @param dataInicio data inicial (opcional).
	 * @param dataFim data final (opcional).
	 * @return lista de movimentacoes.
	 */
	@Query("SELECT m FROM MovimentacaoEstoqueJpaEntity m " + "JOIN EstoqueJpaEntity e ON m.estoqueId = e.id "
			+ "WHERE e.pecaInsumoId = :pecaInsumoId " + "AND (:tipo IS NULL OR m.tipo = :tipo) "
			+ "AND (:dataInicio IS NULL OR m.dataMovimentacao >= :dataInicio) "
			+ "AND (:dataFim IS NULL OR m.dataMovimentacao <= :dataFim) " + "ORDER BY m.dataMovimentacao DESC")
	List<MovimentacaoEstoqueJpaEntity> findByPecaInsumoIdWithFilters(UUID pecaInsumoId, String tipo,
			LocalDateTime dataInicio, LocalDateTime dataFim);

}

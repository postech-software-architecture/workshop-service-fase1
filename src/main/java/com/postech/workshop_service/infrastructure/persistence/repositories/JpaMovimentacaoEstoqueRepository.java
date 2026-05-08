package com.postech.workshop_service.infrastructure.persistence.repositories;

import com.postech.workshop_service.infrastructure.persistence.entities.MovimentacaoEstoqueJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
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
	 * Busca movimentacoes de uma peca ordenadas por data decrescente.
	 * @param pecaInsumoId identificador da peca.
	 * @return lista de movimentacoes.
	 */
	@Query("SELECT m FROM MovimentacaoEstoqueJpaEntity m JOIN EstoqueJpaEntity e ON m.estoqueId = e.id "
			+ "WHERE e.pecaInsumoId = :pecaInsumoId ORDER BY m.dataMovimentacao DESC")
	List<MovimentacaoEstoqueJpaEntity> findByPecaInsumoIdOrderByDataMovimentacaoDesc(UUID pecaInsumoId);

	List<MovimentacaoEstoqueJpaEntity> findByOrdemServicoIdOrderByDataMovimentacaoDesc(UUID ordemServicoId);

}

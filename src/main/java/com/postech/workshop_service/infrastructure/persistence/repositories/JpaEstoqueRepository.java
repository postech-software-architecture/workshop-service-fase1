package com.postech.workshop_service.infrastructure.persistence.repositories;

import com.postech.workshop_service.infrastructure.persistence.entities.EstoqueJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio Spring Data para persistencia de estoques.
 */
public interface JpaEstoqueRepository extends JpaRepository<EstoqueJpaEntity, UUID> {

	/**
	 * Busca estoques por identificador da peca.
	 * @param pecaInsumoId identificador da peca.
	 * @return lista de estoques da peca.
	 */
	List<EstoqueJpaEntity> findByPecaInsumoId(UUID pecaInsumoId);

	/**
	 * Busca estoques ativos por identificador da peca.
	 * @param pecaInsumoId identificador da peca.
	 * @return lista de estoques ativos da peca.
	 */
	List<EstoqueJpaEntity> findByPecaInsumoIdAndAtivoTrue(UUID pecaInsumoId);

	/**
	 * Busca estoque por peca e localizacao.
	 * @param pecaInsumoId identificador da peca.
	 * @param localizacao localizacao do estoque.
	 * @return estoque encontrado, se existir.
	 */
	Optional<EstoqueJpaEntity> findByPecaInsumoIdAndLocalizacao(UUID pecaInsumoId, String localizacao);

	/**
	 * Verifica existencia de localizacao para a peca.
	 * @param pecaInsumoId identificador da peca.
	 * @param localizacao localizacao do estoque.
	 * @return verdadeiro se existir.
	 */
	boolean existsByPecaInsumoIdAndLocalizacao(UUID pecaInsumoId, String localizacao);

	/**
	 * Verifica existencia de localizacao para a peca desconsiderando um identificador.
	 * @param pecaInsumoId identificador da peca.
	 * @param localizacao localizacao do estoque.
	 * @param id identificador do estoque a ignorar.
	 * @return verdadeiro se existir.
	 */
	boolean existsByPecaInsumoIdAndLocalizacaoAndIdNot(UUID pecaInsumoId, String localizacao, UUID id);

	/**
	 * Calcula a quantidade total de estoque ativo de uma peca.
	 * @param pecaInsumoId identificador da peca.
	 * @return soma das quantidades.
	 */
	@Query("SELECT COALESCE(SUM(e.quantidade), 0) FROM EstoqueJpaEntity e WHERE e.pecaInsumoId = :pecaInsumoId AND e.ativo = true")
	BigDecimal calcularQuantidadeTotal(UUID pecaInsumoId);

}

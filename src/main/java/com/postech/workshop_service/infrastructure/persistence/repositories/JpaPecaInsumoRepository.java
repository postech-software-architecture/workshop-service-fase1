package com.postech.workshop_service.infrastructure.persistence.repositories;

import com.postech.workshop_service.infrastructure.persistence.entities.PecaInsumoJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio Spring Data para persistencia de pecas e insumos.
 */
public interface JpaPecaInsumoRepository
		extends JpaRepository<PecaInsumoJpaEntity, UUID>, JpaSpecificationExecutor<PecaInsumoJpaEntity> {

	/**
	 * Busca peca por SKU normalizado.
	 * @param sku SKU normalizado.
	 * @return entidade correspondente, se existir.
	 */
	Optional<PecaInsumoJpaEntity> findBySku(String sku);

	/**
	 * Verifica existencia de SKU ativo desconsiderando um identificador opcional.
	 * @param sku SKU normalizado.
	 * @param id identificador da peca que deve ser ignorado.
	 * @return verdadeiro quando ja existir outro cadastro ativo com o mesmo SKU.
	 */
	boolean existsBySkuAndAtivoTrueAndIdNot(String sku, UUID id);

	/**
	 * Verifica existencia de SKU ativo.
	 * @param sku SKU normalizado.
	 * @return verdadeiro quando existir cadastro ativo com o mesmo SKU.
	 */
	boolean existsBySkuAndAtivoTrue(String sku);

}

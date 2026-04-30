package com.postech.workshop_service.infrastructure.persistence.repositories;

import com.postech.workshop_service.infrastructure.persistence.entities.ServicoJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

/**
 * Repositorio Spring Data para persistencia de servicos.
 */
public interface JpaServicoRepository
		extends JpaRepository<ServicoJpaEntity, UUID>, JpaSpecificationExecutor<ServicoJpaEntity> {

	/**
	 * Verifica existencia de nome ativo.
	 * @param nome nome do servico.
	 * @return verdadeiro quando existir cadastro ativo com o mesmo nome.
	 */
	boolean existsByNomeAndAtivoTrue(String nome);

	/**
	 * Verifica existencia de nome ativo desconsiderando um identificador especifico.
	 * @param nome nome do servico.
	 * @param id identificador do servico que deve ser ignorado na verificacao.
	 * @return verdadeiro quando ja existir outro cadastro ativo com o mesmo nome.
	 */
	boolean existsByNomeAndAtivoTrueAndIdNot(String nome, UUID id);

}

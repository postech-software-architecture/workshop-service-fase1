package com.postech.workshop_service.infrastructure.persistence.repositories;

import com.postech.workshop_service.infrastructure.persistence.entities.VeiculoJpaEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio Spring Data para persistencia de veiculos.
 */
public interface JpaVeiculoRepository extends JpaRepository<VeiculoJpaEntity, UUID>, JpaSpecificationExecutor<VeiculoJpaEntity> {

    /**
     * Busca veiculo por identificador carregando os vinculos com clientes.
     *
     * @param id identificador do veiculo.
     * @return entidade correspondente, se existir.
     */
    @Override
    @EntityGraph(attributePaths = "clientesVinculados")
    Optional<VeiculoJpaEntity> findById(UUID id);

    /**
     * Busca veiculo por placa normalizada carregando os vinculos com clientes.
     *
     * @param placa placa normalizada.
     * @return entidade correspondente, se existir.
     */
    @EntityGraph(attributePaths = "clientesVinculados")
    Optional<VeiculoJpaEntity> findByPlaca(String placa);

    /**
     * Verifica existencia de placa ativa desconsiderando um identificador opcional.
     *
     * @param placa placa normalizada.
     * @param id identificador do veiculo que deve ser ignorado.
     * @return verdadeiro quando ja existir outro cadastro ativo com a mesma placa.
     */
    boolean existsByPlacaAndAtivoTrueAndIdNot(String placa, UUID id);

    /**
     * Verifica existencia de placa ativa.
     *
     * @param placa placa normalizada.
     * @return verdadeiro quando existir cadastro ativo com a mesma placa.
     */
    boolean existsByPlacaAndAtivoTrue(String placa);
}

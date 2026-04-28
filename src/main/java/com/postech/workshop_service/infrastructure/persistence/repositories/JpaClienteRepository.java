package com.postech.workshop_service.infrastructure.persistence.repositories;

import com.postech.workshop_service.infrastructure.persistence.entities.ClienteJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaClienteRepository
		extends JpaRepository<ClienteJpaEntity, UUID>, JpaSpecificationExecutor<ClienteJpaEntity> {

	Optional<ClienteJpaEntity> findByDocumento(String documento);

	Optional<ClienteJpaEntity> findByDocumentoAndAtivoTrue(String documento);

	boolean existsByDocumento(String documento);

}

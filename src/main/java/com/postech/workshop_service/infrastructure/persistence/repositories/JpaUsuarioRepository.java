package com.postech.workshop_service.infrastructure.persistence.repositories;

import com.postech.workshop_service.infrastructure.persistence.entities.UsuarioJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio Spring Data de usuarios autenticaveis.
 */
public interface JpaUsuarioRepository extends JpaRepository<UsuarioJpaEntity, UUID> {

	/**
	 * Busca uma conta por username ou email, ignorando maiusculas e minusculas.
	 * @param username username informado.
	 * @param email email informado.
	 * @return conta encontrada, se existir.
	 */
	Optional<UsuarioJpaEntity> findByUsernameIgnoreCaseOrEmailIgnoreCase(String username, String email);

}

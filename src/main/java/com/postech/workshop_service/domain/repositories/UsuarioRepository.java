package com.postech.workshop_service.domain.repositories;

import com.postech.workshop_service.domain.entities.Usuario;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio de contas autenticaveis.
 */
public interface UsuarioRepository {

	/**
	 * Persiste uma conta autenticavel.
	 * @param usuario conta a ser persistida.
	 * @return conta persistida.
	 */
	Usuario salvar(Usuario usuario);

	/**
	 * Busca uma conta por username ou email.
	 * @param identificador username ou email informado no login.
	 * @return conta encontrada, se existir.
	 */
	Optional<Usuario> buscarPorUsernameOuEmail(String identificador);

	/**
	 * Busca uma conta por identificador.
	 * @param id identificador tecnico.
	 * @return conta encontrada, se existir.
	 */
	Optional<Usuario> buscarPorId(UUID id);

}

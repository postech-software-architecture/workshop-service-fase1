package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.domain.enums.Role;

import java.util.Set;
import java.util.UUID;

/**
 * Resultado da consulta ao usuario autenticado corrente.
 */
public class ResultadoUsuarioAutenticado {

	private final UUID id;

	private final String username;

	private final UUID clienteId;

	private final Set<Role> roles;

	public ResultadoUsuarioAutenticado(UUID id, String username, UUID clienteId, Set<Role> roles) {
		this.id = id;
		this.username = username;
		this.clienteId = clienteId;
		this.roles = roles;
	}

	public UUID getId() {
		return id;
	}

	public String getUsername() {
		return username;
	}

	public UUID getClienteId() {
		return clienteId;
	}

	public Set<Role> getRoles() {
		return roles;
	}

}

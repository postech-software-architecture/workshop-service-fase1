package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.domain.enums.Role;

import java.util.Set;
import java.util.UUID;

/**
 * Identidade do usuario autenticado exposta para a camada de aplicacao.
 *
 * <p>
 * Objeto de aplicacao imutavel que isola os use cases dos detalhes de seguranca do
 * framework (Spring Security). E preenchido por um adapter em {@code infrastructure}
 * atraves da porta {@code ContextoSegurancaProvider}, de modo que a camada de aplicacao
 * dependa apenas de tipos de dominio.
 * </p>
 *
 * @param id identificador tecnico do usuario autenticado.
 * @param username nome de login do usuario autenticado.
 * @param clienteId identificador do cliente vinculado, quando houver ({@code null} caso
 * contrario).
 * @param roles perfis de acesso concedidos ao usuario.
 */
public record IdentidadeAutenticada(UUID id, String username, UUID clienteId, Set<Role> roles) {
}

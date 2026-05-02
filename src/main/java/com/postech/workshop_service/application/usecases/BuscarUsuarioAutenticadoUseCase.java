package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.application.exceptions.AcessoNegadoException;
import com.postech.workshop_service.domain.enums.Role;
import com.postech.workshop_service.infrastructure.security.UsuarioAutenticadoPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.UUID;

/**
 * Caso de uso responsavel por expor a identidade autenticada corrente.
 */
@Service
public class BuscarUsuarioAutenticadoUseCase {

	/**
	 * Retorna a identidade do usuario autenticado na request atual.
	 * @return dados da identidade autenticada.
	 */
	public ResultadoUsuarioAutenticado executar() {
		UsuarioAutenticadoPrincipal principal = obterPrincipal();
		LinkedHashSet<Role> roles = principal.getAuthorities()
			.stream()
			.map(authority -> Role.valueOf(authority.getAuthority().replace("ROLE_", "")))
			.collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
		return new ResultadoUsuarioAutenticado(principal.getId(), principal.getUsername(), principal.getClienteId(),
				roles);
	}

	/**
	 * Retorna o cliente vinculado ao principal autenticado, quando obrigatorio.
	 * @return identificador do cliente vinculado.
	 */
	public UUID obterClienteIdObrigatorio() {
		ResultadoUsuarioAutenticado usuarioAutenticado = executar();
		if (!usuarioAutenticado.getRoles().contains(Role.CLIENTE) || usuarioAutenticado.getClienteId() == null) {
			throw new AcessoNegadoException("A conta autenticada nao possui cliente vinculado para esta operacao.");
		}
		return usuarioAutenticado.getClienteId();
	}

	private UsuarioAutenticadoPrincipal obterPrincipal() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null
				|| !(authentication.getPrincipal() instanceof UsuarioAutenticadoPrincipal principal)) {
			throw new AcessoNegadoException("Nao foi possivel identificar o usuario autenticado.");
		}
		return principal;
	}

}

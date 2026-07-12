package com.postech.workshop_service.api.controllers.support;

import com.postech.workshop_service.domain.entities.Usuario;
import com.postech.workshop_service.domain.enums.Role;
import com.postech.workshop_service.infrastructure.security.UsuarioAutenticadoPrincipal;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

/**
 * Helper de testes para autenticar requisicoes MockMvc com um
 * {@link UsuarioAutenticadoPrincipal} REAL, em vez de {@code @WithMockUser} (que injeta
 * um principal generico do Spring). Assim os ITs exercitam o mesmo tipo de principal que
 * o filtro JWT produz em producao, mantendo o {@code ContextoSegurancaProviderImpl}
 * fail-closed.
 */
public final class AutenticacaoTestSupport {

	private AutenticacaoTestSupport() {
	}

	/**
	 * @param username nome de login.
	 * @param roles perfis do usuario.
	 * @return post-processor que autentica a requisicao como o usuario informado.
	 */
	public static RequestPostProcessor comUsuario(String username, Role... roles) {
		Usuario usuario = new Usuario(UUID.randomUUID(), username, username + "@teste.com", "hash", Set.of(roles), null,
				true, false, LocalDateTime.now(), LocalDateTime.now(), null);
		UsuarioAutenticadoPrincipal principal = UsuarioAutenticadoPrincipal.fromDomain(usuario);
		Authentication auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
		return SecurityMockMvcRequestPostProcessors.authentication(auth);
	}

	/**
	 * @return post-processor com um usuario de staff (administrador, atendente e
	 * mecanico).
	 */
	public static RequestPostProcessor comStaff() {
		return comUsuario("staff.teste", Role.ADMINISTRADOR, Role.ATENDENTE, Role.MECANICO);
	}

	/**
	 * @param username nome de login.
	 * @param roles perfis do usuario.
	 * @return {@link Authentication} com um {@link UsuarioAutenticadoPrincipal} real,
	 * para popular o {@code SecurityContextHolder} diretamente.
	 */
	public static Authentication autenticacao(String username, Role... roles) {
		Usuario usuario = new Usuario(UUID.randomUUID(), username, username + "@teste.com", "hash", Set.of(roles), null,
				true, false, LocalDateTime.now(), LocalDateTime.now(), null);
		UsuarioAutenticadoPrincipal principal = UsuarioAutenticadoPrincipal.fromDomain(usuario);
		return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
	}

	/**
	 * @return {@link Authentication} de staff (administrador, atendente e mecanico).
	 */
	public static Authentication autenticacaoStaff() {
		return autenticacao("staff.teste", Role.ADMINISTRADOR, Role.ATENDENTE, Role.MECANICO);
	}

}

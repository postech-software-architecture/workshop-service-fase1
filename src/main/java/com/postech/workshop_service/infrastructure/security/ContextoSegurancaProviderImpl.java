package com.postech.workshop_service.infrastructure.security;

import com.postech.workshop_service.application.usecases.ContextoSegurancaProvider;
import com.postech.workshop_service.application.usecases.IdentidadeAutenticada;
import com.postech.workshop_service.domain.enums.Role;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Adapter que traduz o contexto de seguranca do Spring Security em uma
 * {@link IdentidadeAutenticada} de aplicacao.
 *
 * <p>
 * Concentra toda a leitura do {@link SecurityContextHolder} e a extracao do
 * {@link UsuarioAutenticadoPrincipal}, de modo que os use cases dependam apenas da porta
 * {@link ContextoSegurancaProvider} e de tipos de dominio.
 * </p>
 */
@Component
public class ContextoSegurancaProviderImpl implements ContextoSegurancaProvider {

	private static final String PREFIXO_ROLE = "ROLE_";

	@Override
	public Optional<IdentidadeAutenticada> identidadeAtual() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated()) {
			return Optional.empty();
		}
		if (authentication.getPrincipal() instanceof UsuarioAutenticadoPrincipal principal) {
			return Optional.of(mapear(principal));
		}
		return mapearPorNome(authentication);
	}

	private IdentidadeAutenticada mapear(UsuarioAutenticadoPrincipal principal) {
		return new IdentidadeAutenticada(principal.getId(), principal.getUsername(), principal.getClienteId(),
				extrairRoles(principal.getAuthorities()));
	}

	/**
	 * Resolve a identidade quando o principal nao e um
	 * {@link UsuarioAutenticadoPrincipal} (ex.: contexto de teste com usuario mock),
	 * derivando um identificador estavel a partir do nome de login.
	 */
	private Optional<IdentidadeAutenticada> mapearPorNome(Authentication authentication) {
		String username = authentication.getName();
		if (username == null || username.isBlank() || "anonymousUser".equals(username)) {
			return Optional.empty();
		}
		UUID idUsuario = UUID.nameUUIDFromBytes(username.getBytes(StandardCharsets.UTF_8));
		return Optional
			.of(new IdentidadeAutenticada(idUsuario, username, null, extrairRoles(authentication.getAuthorities())));
	}

	private Set<Role> extrairRoles(Iterable<? extends GrantedAuthority> authorities) {
		Set<Role> roles = new LinkedHashSet<>();
		for (GrantedAuthority authority : authorities) {
			String nome = authority.getAuthority();
			if (nome != null && nome.startsWith(PREFIXO_ROLE)) {
				roles.add(Role.valueOf(nome.substring(PREFIXO_ROLE.length())));
			}
		}
		return roles;
	}

}

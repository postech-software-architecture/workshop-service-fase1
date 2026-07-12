package com.postech.workshop_service.infrastructure.security;

import com.postech.workshop_service.application.usecases.ContextoSegurancaProvider;
import com.postech.workshop_service.application.usecases.IdentidadeAutenticada;
import com.postech.workshop_service.domain.enums.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Adapter que traduz o contexto de seguranca do Spring Security em uma
 * {@link IdentidadeAutenticada} de aplicacao.
 *
 * <p>
 * Concentra toda a leitura do {@link SecurityContextHolder} e a extracao do
 * {@link UsuarioAutenticadoPrincipal}, de modo que os use cases dependam apenas da porta
 * {@link ContextoSegurancaProvider} e de tipos de dominio. Fail-closed: se o principal
 * nao for um {@link UsuarioAutenticadoPrincipal}, retorna vazio (nao fabrica identidade).
 * </p>
 */
@Component
public class ContextoSegurancaProviderImpl implements ContextoSegurancaProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger(ContextoSegurancaProviderImpl.class);

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
		return Optional.empty();
	}

	private IdentidadeAutenticada mapear(UsuarioAutenticadoPrincipal principal) {
		return new IdentidadeAutenticada(principal.getId(), principal.getUsername(), principal.getClienteId(),
				extrairRoles(principal.getAuthorities()));
	}

	private Set<Role> extrairRoles(Iterable<? extends GrantedAuthority> authorities) {
		Set<Role> roles = new LinkedHashSet<>();
		for (GrantedAuthority authority : authorities) {
			String nome = authority.getAuthority();
			if (nome == null || !nome.startsWith(PREFIXO_ROLE)) {
				continue;
			}
			String candidato = nome.substring(PREFIXO_ROLE.length());
			// Ignora authorities cujo sufixo nao e um Role conhecido (ex.: ROLE_USER de
			// mocks
			// ou de fontes de auth futuras), evitando IllegalArgumentException -> 500.
			try {
				roles.add(Role.valueOf(candidato));
			}
			catch (IllegalArgumentException ex) {
				LOGGER.debug("Authority ignorada por nao corresponder a um Role conhecido: {}", nome);
			}
		}
		return roles;
	}

}

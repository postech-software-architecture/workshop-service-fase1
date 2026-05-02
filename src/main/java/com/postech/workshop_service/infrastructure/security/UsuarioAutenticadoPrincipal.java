package com.postech.workshop_service.infrastructure.security;

import com.postech.workshop_service.domain.entities.Usuario;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Representa o principal autenticado no contexto do Spring Security.
 */
public class UsuarioAutenticadoPrincipal implements UserDetails {

	private final UUID id;

	private final String username;

	private final String senhaHash;

	private final UUID clienteId;

	private final boolean enabled;

	private final boolean accountNonLocked;

	private final Set<GrantedAuthority> authorities;

	private UsuarioAutenticadoPrincipal(UUID id, String username, String senhaHash, UUID clienteId, boolean enabled,
			boolean accountNonLocked, Set<GrantedAuthority> authorities) {
		this.id = id;
		this.username = username;
		this.senhaHash = senhaHash;
		this.clienteId = clienteId;
		this.enabled = enabled;
		this.accountNonLocked = accountNonLocked;
		this.authorities = authorities;
	}

	/**
	 * Cria um principal a partir do dominio.
	 * @param usuario conta autenticavel do dominio.
	 * @return principal pronto para o Spring Security.
	 */
	public static UsuarioAutenticadoPrincipal fromDomain(Usuario usuario) {
		Set<GrantedAuthority> authorities = usuario.getRoles()
			.stream()
			.map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
			.collect(Collectors.toSet());
		return new UsuarioAutenticadoPrincipal(usuario.getId(), usuario.getUsername(), usuario.getSenhaHash(),
				usuario.getClienteId(), usuario.isAtivo() && usuario.getDataRemocao() == null, !usuario.isBloqueado(),
				authorities);
	}

	public UUID getId() {
		return id;
	}

	public UUID getClienteId() {
		return clienteId;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}

	@Override
	public String getPassword() {
		return senhaHash;
	}

	@Override
	public String getUsername() {
		return username;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return accountNonLocked;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

}

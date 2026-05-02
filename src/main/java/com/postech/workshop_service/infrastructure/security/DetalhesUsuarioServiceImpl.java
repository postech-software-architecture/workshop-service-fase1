package com.postech.workshop_service.infrastructure.security;

import com.postech.workshop_service.domain.entities.Usuario;
import com.postech.workshop_service.domain.repositories.UsuarioRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Servico adaptador de usuarios autenticaveis para o Spring Security.
 */
@Service
public class DetalhesUsuarioServiceImpl implements UserDetailsService {

	private final UsuarioRepository usuarioRepository;

	public DetalhesUsuarioServiceImpl(UsuarioRepository usuarioRepository) {
		this.usuarioRepository = usuarioRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Usuario usuario = usuarioRepository.buscarPorUsernameOuEmail(username)
			.orElseThrow(() -> new UsernameNotFoundException("Usuario nao encontrado."));
		return UsuarioAutenticadoPrincipal.fromDomain(usuario);
	}

	/**
	 * Carrega um principal autenticado a partir do identificador tecnico do usuario.
	 * @param usuarioId identificador tecnico do usuario.
	 * @return principal carregado.
	 */
	public UsuarioAutenticadoPrincipal carregarPorId(UUID usuarioId) {
		Usuario usuario = usuarioRepository.buscarPorId(usuarioId)
			.orElseThrow(() -> new UsernameNotFoundException("Usuario nao encontrado."));
		return UsuarioAutenticadoPrincipal.fromDomain(usuario);
	}

}

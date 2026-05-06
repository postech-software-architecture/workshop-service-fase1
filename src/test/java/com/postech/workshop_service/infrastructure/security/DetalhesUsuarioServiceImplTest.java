package com.postech.workshop_service.infrastructure.security;

import com.postech.workshop_service.domain.entities.Usuario;
import com.postech.workshop_service.domain.enums.Role;
import com.postech.workshop_service.domain.repositories.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DetalhesUsuarioServiceImplTest {

	@Mock
	private UsuarioRepository usuarioRepository;

	@Test
	void shouldLoadUserByUsernameOrEmail() {
		Usuario usuario = usuarioAtivo(UUID.randomUUID());
		when(usuarioRepository.buscarPorUsernameOuEmail("admin")).thenReturn(Optional.of(usuario));
		DetalhesUsuarioServiceImpl service = new DetalhesUsuarioServiceImpl(usuarioRepository);

		UsuarioAutenticadoPrincipal principal = (UsuarioAutenticadoPrincipal) service.loadUserByUsername("admin");

		assertThat(principal.getId()).isEqualTo(usuario.getId());
		assertThat(principal.getUsername()).isEqualTo("admin");
	}

	@Test
	void shouldThrowWhenUsernameOrIdNotFound() {
		UUID id = UUID.randomUUID();
		when(usuarioRepository.buscarPorUsernameOuEmail("ausente")).thenReturn(Optional.empty());
		when(usuarioRepository.buscarPorId(id)).thenReturn(Optional.empty());
		DetalhesUsuarioServiceImpl service = new DetalhesUsuarioServiceImpl(usuarioRepository);

		assertThatThrownBy(() -> service.loadUserByUsername("ausente")).isInstanceOf(UsernameNotFoundException.class);
		assertThatThrownBy(() -> service.carregarPorId(id)).isInstanceOf(UsernameNotFoundException.class);
	}

	@Test
	void shouldLoadUserById() {
		UUID id = UUID.randomUUID();
		when(usuarioRepository.buscarPorId(id)).thenReturn(Optional.of(usuarioAtivo(id)));
		DetalhesUsuarioServiceImpl service = new DetalhesUsuarioServiceImpl(usuarioRepository);

		assertThat(service.carregarPorId(id).getId()).isEqualTo(id);
	}

	@Test
	void shouldReflectRemovedUserAsDisabled() {
		Usuario usuario = new Usuario(UUID.randomUUID(), "removido", "removido@teste.com", "hash",
				Set.of(Role.ADMINISTRADOR), null, true, false, LocalDateTime.now(), LocalDateTime.now(),
				LocalDateTime.now());

		assertThat(UsuarioAutenticadoPrincipal.fromDomain(usuario).isEnabled()).isFalse();
	}

	private Usuario usuarioAtivo(UUID id) {
		return new Usuario(id, "admin", "admin@teste.com", "hash", Set.of(Role.ADMINISTRADOR), null, true, false,
				LocalDateTime.now(), LocalDateTime.now(), null);
	}

}

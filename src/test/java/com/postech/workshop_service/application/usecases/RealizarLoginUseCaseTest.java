package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.application.exceptions.CredenciaisInvalidasException;
import com.postech.workshop_service.domain.entities.RefreshToken;
import com.postech.workshop_service.domain.entities.Usuario;
import com.postech.workshop_service.domain.enums.Role;
import com.postech.workshop_service.domain.repositories.RefreshTokenRepository;
import com.postech.workshop_service.domain.repositories.UsuarioRepository;
import com.postech.workshop_service.infrastructure.security.JwtTokenService;
import com.postech.workshop_service.infrastructure.security.UsuarioAutenticadoPrincipal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RealizarLoginUseCaseTest {

	@Mock
	private AuthenticationManager authenticationManager;

	@Mock
	private UsuarioRepository usuarioRepository;

	@Mock
	private RefreshTokenRepository refreshTokenRepository;

	@Mock
	private JwtTokenService jwtTokenService;

	@InjectMocks
	private RealizarLoginUseCase useCase;

	@Test
	void shouldLoginSuccessfully() {
		Usuario usuario = new Usuario(UUID.randomUUID(), "admin", "admin@teste.com", "hash", Set.of(Role.ADMINISTRADOR),
				null, true, false, LocalDateTime.now(), LocalDateTime.now(), null);
		UsuarioAutenticadoPrincipal principal = UsuarioAutenticadoPrincipal.fromDomain(usuario);
		Authentication authentication = new UsernamePasswordAuthenticationToken(principal, null,
				principal.getAuthorities());

		when(authenticationManager.authenticate(any())).thenReturn(authentication);
		when(usuarioRepository.buscarPorId(usuario.getId())).thenReturn(Optional.of(usuario));
		when(jwtTokenService.gerarAccessToken(usuario)).thenReturn("access-token");
		when(jwtTokenService.gerarRefreshToken()).thenReturn("refresh-token");
		when(jwtTokenService.calcularExpiracaoRefreshToken()).thenReturn(LocalDateTime.now().plusDays(7));
		when(jwtTokenService.getExpiracaoAccessSegundos()).thenReturn(3600L);
		when(refreshTokenRepository.salvar(any(RefreshToken.class)))
			.thenAnswer(invocation -> invocation.getArgument(0, RefreshToken.class));

		ResultadoAutenticacao resultado = useCase.executar("admin", "senha123");

		assertEquals("access-token", resultado.getAccessToken());
		assertEquals("refresh-token", resultado.getRefreshToken());
		assertEquals(3600L, resultado.getExpiresIn());
	}

	@Test
	void shouldThrowWhenCredentialsAreInvalid() {
		when(authenticationManager.authenticate(any()))
			.thenThrow(new BadCredentialsException("Credenciais invalidas."));

		assertThrows(CredenciaisInvalidasException.class, () -> useCase.executar("admin", "senhaErrada"));
	}

}

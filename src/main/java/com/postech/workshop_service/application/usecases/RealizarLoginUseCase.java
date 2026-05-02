package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.application.exceptions.ContaInativaException;
import com.postech.workshop_service.application.exceptions.CredenciaisInvalidasException;
import com.postech.workshop_service.domain.entities.RefreshToken;
import com.postech.workshop_service.domain.entities.Usuario;
import com.postech.workshop_service.domain.repositories.RefreshTokenRepository;
import com.postech.workshop_service.domain.repositories.UsuarioRepository;
import com.postech.workshop_service.infrastructure.security.JwtTokenService;
import com.postech.workshop_service.infrastructure.security.UsuarioAutenticadoPrincipal;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso responsavel por autenticar um usuario e emitir tokens.
 */
@Service
public class RealizarLoginUseCase {

	private final AuthenticationManager authenticationManager;

	private final UsuarioRepository usuarioRepository;

	private final RefreshTokenRepository refreshTokenRepository;

	private final JwtTokenService jwtTokenService;

	public RealizarLoginUseCase(AuthenticationManager authenticationManager, UsuarioRepository usuarioRepository,
			RefreshTokenRepository refreshTokenRepository, JwtTokenService jwtTokenService) {
		this.authenticationManager = authenticationManager;
		this.usuarioRepository = usuarioRepository;
		this.refreshTokenRepository = refreshTokenRepository;
		this.jwtTokenService = jwtTokenService;
	}

	/**
	 * Autentica um usuario por username ou email e senha.
	 * @param identificador username ou email informado no login.
	 * @param senha senha em texto puro.
	 * @return tokens emitidos para a sessao autenticada.
	 */
	@Transactional
	public ResultadoAutenticacao executar(String identificador, String senha) {
		try {
			Authentication authentication = authenticationManager
				.authenticate(new UsernamePasswordAuthenticationToken(identificador, senha));
			UsuarioAutenticadoPrincipal principal = (UsuarioAutenticadoPrincipal) authentication.getPrincipal();
			Usuario usuario = usuarioRepository.buscarPorId(principal.getId())
				.orElseThrow(() -> new CredenciaisInvalidasException("Credenciais invalidas."));
			String accessToken = jwtTokenService.gerarAccessToken(usuario);
			RefreshToken refreshPersistido = refreshTokenRepository
				.salvar(new RefreshToken(jwtTokenService.gerarRefreshToken(), usuario.getId(),
						jwtTokenService.calcularExpiracaoRefreshToken()));
			return new ResultadoAutenticacao(accessToken, refreshPersistido.getToken(),
					jwtTokenService.getExpiracaoAccessSegundos());
		}
		catch (BadCredentialsException ex) {
			throw new CredenciaisInvalidasException("Credenciais invalidas.");
		}
		catch (DisabledException | LockedException ex) {
			throw new ContaInativaException("Credenciais invalidas ou conta indisponivel.");
		}
	}

}

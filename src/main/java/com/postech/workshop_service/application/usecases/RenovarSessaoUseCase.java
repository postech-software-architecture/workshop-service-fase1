package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.application.exceptions.ContaInativaException;
import com.postech.workshop_service.application.exceptions.TokenInvalidoException;
import com.postech.workshop_service.domain.entities.RefreshToken;
import com.postech.workshop_service.domain.entities.Usuario;
import com.postech.workshop_service.domain.repositories.RefreshTokenRepository;
import com.postech.workshop_service.domain.repositories.UsuarioRepository;
import com.postech.workshop_service.infrastructure.security.JwtTokenService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso responsavel por renovar uma sessao autenticada.
 */
@Service
public class RenovarSessaoUseCase {

	private final RefreshTokenRepository refreshTokenRepository;

	private final UsuarioRepository usuarioRepository;

	private final JwtTokenService jwtTokenService;

	public RenovarSessaoUseCase(RefreshTokenRepository refreshTokenRepository, UsuarioRepository usuarioRepository,
			JwtTokenService jwtTokenService) {
		this.refreshTokenRepository = refreshTokenRepository;
		this.usuarioRepository = usuarioRepository;
		this.jwtTokenService = jwtTokenService;
	}

	/**
	 * Renova uma sessao autenticada com rotacao de refresh token.
	 * @param refreshTokenValor valor opaco do refresh token atual.
	 * @return novos tokens emitidos para a sessao.
	 */
	@Transactional
	public ResultadoAutenticacao executar(String refreshTokenValor) {
		RefreshToken refreshToken = refreshTokenRepository.buscarPorToken(refreshTokenValor)
			.orElseThrow(() -> new TokenInvalidoException("Refresh token invalido."));
		if (!refreshToken.estaAtivo()) {
			throw new TokenInvalidoException("Refresh token expirado ou revogado.");
		}

		Usuario usuario = usuarioRepository.buscarPorId(refreshToken.getUsuarioId())
			.orElseThrow(() -> new TokenInvalidoException("Usuario do refresh token nao encontrado."));
		if (!usuario.podeAutenticar()) {
			throw new ContaInativaException("Credenciais invalidas ou conta indisponivel.");
		}

		refreshToken.revogar();
		refreshTokenRepository.salvar(refreshToken);

		String accessToken = jwtTokenService.gerarAccessToken(usuario);
		RefreshToken novoRefreshToken = refreshTokenRepository.salvar(new RefreshToken(
				jwtTokenService.gerarRefreshToken(), usuario.getId(), jwtTokenService.calcularExpiracaoRefreshToken()));
		return new ResultadoAutenticacao(accessToken, novoRefreshToken.getToken(),
				jwtTokenService.getExpiracaoAccessSegundos());
	}

}

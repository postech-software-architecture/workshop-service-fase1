package com.postech.workshop_service.api.controllers;

import com.postech.workshop_service.api.dtos.AuthTokensResponse;
import com.postech.workshop_service.api.dtos.LoginRequest;
import com.postech.workshop_service.api.dtos.LogoutRequest;
import com.postech.workshop_service.api.dtos.RefreshTokenRequest;
import com.postech.workshop_service.api.dtos.UsuarioAutenticadoResponse;
import com.postech.workshop_service.application.usecases.BuscarUsuarioAutenticadoUseCase;
import com.postech.workshop_service.application.usecases.EncerrarSessaoUseCase;
import com.postech.workshop_service.application.usecases.RealizarLoginUseCase;
import com.postech.workshop_service.application.usecases.RenovarSessaoUseCase;
import com.postech.workshop_service.application.usecases.ResultadoAutenticacao;
import com.postech.workshop_service.application.usecases.ResultadoUsuarioAutenticado;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller responsavel pelos fluxos de autenticacao e sessao.
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticacao", description = "Fluxos de login, refresh, logout e identidade autenticada")
public class AuthController {

	private final RealizarLoginUseCase realizarLoginUseCase;

	private final RenovarSessaoUseCase renovarSessaoUseCase;

	private final EncerrarSessaoUseCase encerrarSessaoUseCase;

	private final BuscarUsuarioAutenticadoUseCase buscarUsuarioAutenticadoUseCase;

	public AuthController(RealizarLoginUseCase realizarLoginUseCase, RenovarSessaoUseCase renovarSessaoUseCase,
			EncerrarSessaoUseCase encerrarSessaoUseCase,
			BuscarUsuarioAutenticadoUseCase buscarUsuarioAutenticadoUseCase) {
		this.realizarLoginUseCase = realizarLoginUseCase;
		this.renovarSessaoUseCase = renovarSessaoUseCase;
		this.encerrarSessaoUseCase = encerrarSessaoUseCase;
		this.buscarUsuarioAutenticadoUseCase = buscarUsuarioAutenticadoUseCase;
	}

	/**
	 * Autentica um usuario e emite tokens para a sessao.
	 * @param request payload de login.
	 * @return tokens emitidos.
	 */
	@PostMapping("/login")
	@Operation(summary = "Autenticar usuario e emitir tokens")
	public ResponseEntity<AuthTokensResponse> login(@RequestBody @Valid LoginRequest request) {
		ResultadoAutenticacao resultado = realizarLoginUseCase.executar(request.getUsername(), request.getPassword());
		return ResponseEntity.ok(toAuthTokensResponse(resultado));
	}

	/**
	 * Renova uma sessao autenticada a partir do refresh token.
	 * @param request payload contendo o refresh token.
	 * @return novos tokens emitidos.
	 */
	@PostMapping("/refresh")
	@Operation(summary = "Renovar sessao autenticada")
	public ResponseEntity<AuthTokensResponse> refresh(@RequestBody @Valid RefreshTokenRequest request) {
		ResultadoAutenticacao resultado = renovarSessaoUseCase.executar(request.getRefreshToken());
		return ResponseEntity.ok(toAuthTokensResponse(resultado));
	}

	/**
	 * Revoga a sessao identificada pelo refresh token informado.
	 * @param request payload contendo o refresh token.
	 */
	@PostMapping("/logout")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Encerrar uma sessao autenticada")
	public void logout(@RequestBody @Valid LogoutRequest request) {
		encerrarSessaoUseCase.executar(request.getRefreshToken());
	}

	/**
	 * Retorna a identidade do usuario autenticado na sessao corrente.
	 * @return dados da identidade autenticada.
	 */
	@GetMapping("/me")
	@Operation(summary = "Consultar identidade autenticada")
	@SecurityRequirement(name = "bearerAuth")
	public ResponseEntity<UsuarioAutenticadoResponse> me() {
		ResultadoUsuarioAutenticado resultado = buscarUsuarioAutenticadoUseCase.executar();
		return ResponseEntity.ok(UsuarioAutenticadoResponse.builder()
			.id(resultado.getId())
			.username(resultado.getUsername())
			.roles(resultado.getRoles().stream().map(Enum::name).toList())
			.build());
	}

	private AuthTokensResponse toAuthTokensResponse(ResultadoAutenticacao resultado) {
		return AuthTokensResponse.builder()
			.accessToken(resultado.getAccessToken())
			.refreshToken(resultado.getRefreshToken())
			.expiresIn(resultado.getExpiresIn())
			.build();
	}

}

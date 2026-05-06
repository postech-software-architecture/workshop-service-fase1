package com.postech.workshop_service.application.usecases;

/**
 * Resultado padronizado de operacoes que emitem tokens.
 */
public class ResultadoAutenticacao {

	private final String accessToken;

	private final String refreshToken;

	private final long expiresIn;

	public ResultadoAutenticacao(String accessToken, String refreshToken, long expiresIn) {
		this.accessToken = accessToken;
		this.refreshToken = refreshToken;
		this.expiresIn = expiresIn;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public String getRefreshToken() {
		return refreshToken;
	}

	public long getExpiresIn() {
		return expiresIn;
	}

}

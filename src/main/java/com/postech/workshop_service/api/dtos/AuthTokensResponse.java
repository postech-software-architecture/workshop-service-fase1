package com.postech.workshop_service.api.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Resposta padronizada de tokens de autenticacao.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthTokensResponse {

	private String accessToken;

	private String refreshToken;

	private long expiresIn;

}

package com.postech.workshop_service.api.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Payload de refresh de sessao.
 */
@Data
public class RefreshTokenRequest {

	@NotBlank(message = "O refresh token e obrigatorio.")
	private String refreshToken;

}

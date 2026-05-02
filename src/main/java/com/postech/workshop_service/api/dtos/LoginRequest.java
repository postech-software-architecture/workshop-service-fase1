package com.postech.workshop_service.api.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Payload de autenticacao por username ou email.
 */
@Data
public class LoginRequest {

	@NotBlank(message = "O identificador de acesso e obrigatorio.")
	private String username;

	@NotBlank(message = "A senha e obrigatoria.")
	private String password;

}

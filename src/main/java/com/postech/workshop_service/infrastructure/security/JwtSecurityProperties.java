package com.postech.workshop_service.infrastructure.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import org.springframework.stereotype.Component;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Propriedades de configuracao dos tokens JWT.
 */
@Component
@Validated
@ConfigurationProperties(prefix = "seguranca.jwt")
public class JwtSecurityProperties {

	@NotBlank(message = "O segredo JWT deve ser configurado.")
	@Size(min = 32, message = "O segredo JWT deve possuir ao menos 32 caracteres.")
	private String secret;

	@Min(value = 1, message = "A expiracao do access token deve ser maior que zero.")
	private long expiracaoAccessSegundos;

	@Min(value = 1, message = "A expiracao do refresh token deve ser maior que zero.")
	private long expiracaoRefreshDias;

	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}

	public long getExpiracaoAccessSegundos() {
		return expiracaoAccessSegundos;
	}

	public void setExpiracaoAccessSegundos(long expiracaoAccessSegundos) {
		this.expiracaoAccessSegundos = expiracaoAccessSegundos;
	}

	public long getExpiracaoRefreshDias() {
		return expiracaoRefreshDias;
	}

	public void setExpiracaoRefreshDias(long expiracaoRefreshDias) {
		this.expiracaoRefreshDias = expiracaoRefreshDias;
	}

}

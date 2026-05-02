package com.postech.workshop_service.infrastructure.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Propriedades de configuracao dos tokens JWT.
 */
@Component
@ConfigurationProperties(prefix = "seguranca.jwt")
public class JwtSecurityProperties {

	private String secret;

	private long expiracaoAccessSegundos;

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

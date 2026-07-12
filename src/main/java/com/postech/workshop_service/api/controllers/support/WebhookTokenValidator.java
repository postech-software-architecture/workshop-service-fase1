package com.postech.workshop_service.api.controllers.support;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Valida o token de servico do webhook de decisao de orcamento em tempo constante.
 *
 * <p>
 * Fail-safe: quando o token nao esta configurado no ambiente, qualquer requisicao e
 * rejeitada, evitando expor o webhook aberto em producao.
 * </p>
 */
@Component
public class WebhookTokenValidator {

	private final String tokenConfigurado;

	/**
	 * @param tokenConfigurado token esperado, injetado da configuracao
	 * {@code webhook.orcamento.token} (default vazio).
	 */
	public WebhookTokenValidator(@Value("${webhook.orcamento.token:}") String tokenConfigurado) {
		this.tokenConfigurado = tokenConfigurado;
	}

	/**
	 * Verifica se o token recebido corresponde ao configurado, em tempo constante.
	 * @param tokenRecebido valor do header {@code X-Webhook-Token} (pode ser nulo).
	 * @return {@code true} apenas quando ha token configurado e ele confere.
	 */
	public boolean valido(String tokenRecebido) {
		if (tokenConfigurado == null || tokenConfigurado.isBlank() || tokenRecebido == null) {
			return false;
		}
		return MessageDigest.isEqual(tokenConfigurado.getBytes(StandardCharsets.UTF_8),
				tokenRecebido.getBytes(StandardCharsets.UTF_8));
	}

}

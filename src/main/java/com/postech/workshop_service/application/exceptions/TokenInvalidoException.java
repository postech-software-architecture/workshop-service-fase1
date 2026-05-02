package com.postech.workshop_service.application.exceptions;

/**
 * Excecao utilizada quando um refresh token nao e mais valido.
 */
public class TokenInvalidoException extends RuntimeException {

	/**
	 * Cria uma nova excecao de token invalido.
	 * @param mensagem descricao do erro.
	 */
	public TokenInvalidoException(String mensagem) {
		super(mensagem);
	}

}

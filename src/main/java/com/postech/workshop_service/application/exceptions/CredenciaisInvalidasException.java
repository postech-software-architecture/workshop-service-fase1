package com.postech.workshop_service.application.exceptions;

/**
 * Excecao utilizada quando o usuario nao consegue ser autenticado.
 */
public class CredenciaisInvalidasException extends RuntimeException {

	/**
	 * Cria uma nova excecao de credenciais invalidas.
	 * @param mensagem descricao do erro.
	 */
	public CredenciaisInvalidasException(String mensagem) {
		super(mensagem);
	}

}

package com.postech.workshop_service.application.exceptions;

/**
 * Excecao utilizada quando a conta nao pode autenticar.
 */
public class ContaInativaException extends RuntimeException {

	/**
	 * Cria uma nova excecao de conta indisponivel.
	 * @param mensagem descricao do erro.
	 */
	public ContaInativaException(String mensagem) {
		super(mensagem);
	}

}

package com.postech.workshop_service.application.exceptions;

/**
 * Excecao utilizada para sinalizar violacoes de regras de negocio.
 */
public class RegraDeNegocioException extends RuntimeException {

	/**
	 * Cria uma nova excecao de regra de negocio.
	 * @param mensagem descricao do erro encontrado.
	 */
	public RegraDeNegocioException(String mensagem) {
		super(mensagem);
	}

}

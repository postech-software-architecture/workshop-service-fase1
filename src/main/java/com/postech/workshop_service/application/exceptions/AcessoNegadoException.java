package com.postech.workshop_service.application.exceptions;

/**
 * Excecao utilizada para indicar acesso negado por ownership ou regra de perfil.
 */
public class AcessoNegadoException extends RuntimeException {

	/**
	 * Cria uma nova excecao de acesso negado.
	 * @param mensagem descricao do erro.
	 */
	public AcessoNegadoException(String mensagem) {
		super(mensagem);
	}

}

package com.postech.workshop_service.domain.exceptions;

/**
 * Excecao utilizada quando um recurso solicitado nao e encontrado.
 */
public class RecursoNaoEncontradoException extends RuntimeException {

	/**
	 * Cria uma nova excecao de recurso nao encontrado.
	 * @param mensagem descricao do recurso ausente.
	 */
	public RecursoNaoEncontradoException(String mensagem) {
		super(mensagem);
	}

}

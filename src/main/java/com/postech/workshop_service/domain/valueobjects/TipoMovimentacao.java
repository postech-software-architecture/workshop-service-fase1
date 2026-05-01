package com.postech.workshop_service.domain.valueobjects;

/**
 * Enumeracao que representa os tipos de movimentacao de estoque.
 */
public enum TipoMovimentacao {

	/**
	 * Entrada de estoque - incrementa a quantidade disponivel.
	 */
	ENTRADA,

	/**
	 * Saida de estoque - decrementa a quantidade disponivel.
	 */
	SAIDA,

	/**
	 * Ajuste de estoque - substitui a quantidade atual pelo valor informado.
	 */
	AJUSTE

}

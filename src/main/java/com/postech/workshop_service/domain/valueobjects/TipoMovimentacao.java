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
	AJUSTE,

	/**
	 * Reserva de estoque - decrementa a quantidade disponivel para uma OS pendente de
	 * aprovacao.
	 */
	RESERVA,

	/**
	 * Liberacao de reserva - devolve ao estoque a quantidade previamente reservada,
	 * ocorre quando o orcamento e rejeitado ou cancelado.
	 */
	LIBERACAO

}

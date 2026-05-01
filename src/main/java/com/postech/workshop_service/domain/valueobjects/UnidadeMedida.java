package com.postech.workshop_service.domain.valueobjects;

/**
 * Enumeracao que representa as unidades de medida utilizadas para pecas e insumos.
 */
public enum UnidadeMedida {

	/**
	 * Unidade - itens contaveis individualmente.
	 */
	UN("Unidade"),

	/**
	 * Litro - volume de liquidos.
	 */
	L("Litro"),

	/**
	 * Quilograma - massa/peso.
	 */
	KG("Quilograma"),

	/**
	 * Metro - comprimento.
	 */
	M("Metro"),

	/**
	 * Mililitro - volume pequeno de liquidos.
	 */
	ML("Mililitro"),

	/**
	 * Caixa - conjunto de itens embalados.
	 */
	CX("Caixa"),

	/**
	 * Peca - componente especifico.
	 */
	PC("Peca");

	private final String descricao;

	UnidadeMedida(String descricao) {
		this.descricao = descricao;
	}

	/**
	 * Retorna a descricao legivel da unidade de medida.
	 * @return descricao da unidade.
	 */
	public String getDescricao() {
		return descricao;
	}

}

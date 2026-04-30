package com.postech.workshop_service.domain.entities;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * Objeto de dominio que representa um item da composicao tecnica da ordem de servico.
 */
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public final class ItemComposicaoTecnica {

	@EqualsAndHashCode.Include
	private final String descricao;

	@EqualsAndHashCode.Include
	private final BigDecimal valor;

	@EqualsAndHashCode.Include
	private final TipoItemComposicaoTecnica tipo;

	/**
	 * Cria um item de composicao tecnica com os dados obrigatorios.
	 * @param descricao descricao do item.
	 * @param valor valor monetario do item.
	 * @param tipo classificacao do item.
	 */
	public ItemComposicaoTecnica(String descricao, BigDecimal valor, TipoItemComposicaoTecnica tipo) {
		this.descricao = validarDescricao(descricao);
		this.valor = validarValor(valor);
		this.tipo = validarTipo(tipo);
	}

	private String validarDescricao(String descricao) {
		if (descricao == null) {
			throw new IllegalArgumentException("A descricao do item de composicao tecnica e obrigatoria.");
		}

		String descricaoSanitizada = descricao.trim().replaceAll("\\s+", " ");
		if (descricaoSanitizada.isEmpty()) {
			throw new IllegalArgumentException("A descricao do item de composicao tecnica e obrigatoria.");
		}
		return descricaoSanitizada;
	}

	private BigDecimal validarValor(BigDecimal valor) {
		if (valor == null) {
			throw new IllegalArgumentException("O valor do item de composicao tecnica e obrigatorio.");
		}
		return valor;
	}

	private TipoItemComposicaoTecnica validarTipo(TipoItemComposicaoTecnica tipo) {
		if (tipo == null) {
			throw new IllegalArgumentException("O tipo do item de composicao tecnica e obrigatorio.");
		}
		return tipo;
	}

}

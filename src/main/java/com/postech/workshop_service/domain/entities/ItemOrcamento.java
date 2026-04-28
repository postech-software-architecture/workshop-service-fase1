package com.postech.workshop_service.domain.entities;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * Objeto de valor que representa um item cobrado dentro do orcamento.
 */
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public final class ItemOrcamento {

	@EqualsAndHashCode.Include
	private final String descricao;

	@EqualsAndHashCode.Include
	private final BigDecimal valor;

	/**
	 * Cria um novo item de orcamento com descricao e valor obrigatorios.
	 * @param descricao descricao do item cobrado.
	 * @param valor valor monetario do item.
	 */
	public ItemOrcamento(String descricao, BigDecimal valor) {
		this.descricao = validarDescricao(descricao);
		this.valor = validarValor(valor);
	}

	private String validarDescricao(String descricao) {
		if (descricao == null) {
			throw new IllegalArgumentException("A descricao do item de orcamento e obrigatoria.");
		}

		String descricaoSanitizada = descricao.trim().replaceAll("\\s+", " ");
		if (descricaoSanitizada.isEmpty()) {
			throw new IllegalArgumentException("A descricao do item de orcamento e obrigatoria.");
		}
		return descricaoSanitizada;
	}

	private BigDecimal validarValor(BigDecimal valor) {
		if (valor == null) {
			throw new IllegalArgumentException("O valor do item de orcamento e obrigatorio.");
		}
		return valor;
	}

}

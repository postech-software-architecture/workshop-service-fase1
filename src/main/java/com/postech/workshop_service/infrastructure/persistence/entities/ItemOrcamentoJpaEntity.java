package com.postech.workshop_service.infrastructure.persistence.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Representacao persistente de um item fotografado no orcamento.
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ItemOrcamentoJpaEntity {

	@Column(nullable = false)
	private String descricao;

	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal valor;

}

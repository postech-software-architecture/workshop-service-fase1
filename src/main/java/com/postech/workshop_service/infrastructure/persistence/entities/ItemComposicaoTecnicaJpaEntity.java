package com.postech.workshop_service.infrastructure.persistence.entities;

import com.postech.workshop_service.domain.entities.TipoItemComposicaoTecnica;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Representacao persistente de um item de composicao tecnica da ordem de servico.
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ItemComposicaoTecnicaJpaEntity {

	@Column(nullable = false)
	private String descricao;

	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal valor;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private TipoItemComposicaoTecnica tipo;

}

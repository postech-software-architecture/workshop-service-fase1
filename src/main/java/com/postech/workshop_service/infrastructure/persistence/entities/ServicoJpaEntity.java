package com.postech.workshop_service.infrastructure.persistence.entities;

import com.postech.workshop_service.domain.enums.CategoriaServico;
import com.postech.workshop_service.domain.enums.NivelComplexidade;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

/**
 * Entidade JPA do agregado de servico.
 */
@Entity
@Table(name = "servicos")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ServicoJpaEntity extends BaseJpaEntity {

	@Column(nullable = false, length = 120)
	private String nome;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String descricao;

	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal valor;

	@Column(nullable = false)
	private Integer tempoEstimadoMinutos;

	@Enumerated(EnumType.STRING)
	@Column(length = 30)
	private CategoriaServico categoria;

	@Enumerated(EnumType.STRING)
	@Column(name = "nivel_complexidade", length = 20)
	private NivelComplexidade nivelComplexidade;

	@Column(name = "garantia_dias")
	private Integer garantiaDias;

	@Column(name = "observacoes_tecnicas", columnDefinition = "TEXT")
	private String observacoesTecnicas;

	@Column(nullable = false)
	private Boolean ativo;

}

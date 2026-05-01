package com.postech.workshop_service.infrastructure.persistence.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

/**
 * Entidade JPA do agregado de pecas e insumos.
 */
@Entity
@Table(name = "pecas_insumos")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PecaInsumoJpaEntity extends BaseJpaEntity {

	@Column(nullable = false, length = 50, unique = true)
	private String sku;

	@Column(nullable = false, length = 200)
	private String nome;

	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal valorUnitario;

	@Column(nullable = false, precision = 10, scale = 3)
	private BigDecimal estoqueMinimo;

	@Column(nullable = false, length = 10)
	private String unidadeMedida;

	@Column(nullable = false, length = 20)
	private String tipoItem;

	@Column(length = 200)
	private String fornecedor;

	@Column(length = 50)
	private String codigoBarras;

	@Column(length = 100)
	private String marca;

	@Column(length = 100)
	private String categoria;

	@Column(length = 500)
	private String aplicacao;

	@Column(columnDefinition = "TEXT")
	private String observacoes;

	@Column(nullable = false)
	private Boolean ativo;

	@Version
	@Column(nullable = false)
	private Integer versao;

}

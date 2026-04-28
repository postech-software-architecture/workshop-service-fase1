package com.postech.workshop_service.infrastructure.persistence.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Entidade JPA do agregado de veiculo.
 */
@Entity
@Table(name = "veiculos")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class VeiculoJpaEntity extends BaseJpaEntity {

	@Column(nullable = false, length = 7)
	private String placa;

	@Column(nullable = false, length = 60)
	private String marca;

	@Column(nullable = false, length = 80)
	private String modelo;

	@Column(nullable = false)
	private Integer ano;

	@Column(length = 30)
	private String cor;

	@Column(columnDefinition = "TEXT")
	private String observacoes;

	@Column(nullable = false)
	private Boolean ativo;

	@Builder.Default
	@OneToMany(mappedBy = "veiculo", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private Set<VeiculoClienteJpaEntity> clientesVinculados = new LinkedHashSet<>();

}

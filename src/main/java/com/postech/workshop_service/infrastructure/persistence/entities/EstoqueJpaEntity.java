package com.postech.workshop_service.infrastructure.persistence.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidade JPA para estoques.
 */
@Entity
@Table(name = "estoques")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class EstoqueJpaEntity {

	@Id
	private UUID id;

	@Column(name = "peca_insumo_id", nullable = false)
	private UUID pecaInsumoId;

	@Column(nullable = false, length = 100)
	private String localizacao;

	@Column(nullable = false, precision = 10, scale = 3)
	private BigDecimal quantidade;

	@Column(nullable = false)
	private Boolean ativo;

	@Version
	@Column(nullable = false)
	private Integer versao;

	@Column(name = "data_criacao", nullable = false)
	private LocalDateTime dataCriacao;

	@Column(name = "data_ultima_atualizacao", nullable = false)
	private LocalDateTime dataUltimaAtualizacao;

}

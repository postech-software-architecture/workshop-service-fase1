package com.postech.workshop_service.infrastructure.persistence.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidade JPA para movimentacoes de estoque.
 */
@Entity
@Table(name = "movimentacoes_estoque")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovimentacaoEstoqueJpaEntity {

	@Id
	private UUID id;

	@Column(name = "estoque_id", nullable = false)
	private UUID estoqueId;

	@Column(nullable = false, length = 20)
	private String tipo;

	@Column(nullable = false, precision = 10, scale = 3)
	private BigDecimal quantidade;

	@Column(name = "quantidade_anterior", nullable = false, precision = 10, scale = 3)
	private BigDecimal quantidadeAnterior;

	@Column(name = "quantidade_posterior", nullable = false, precision = 10, scale = 3)
	private BigDecimal quantidadePosterior;

	@Column(length = 500)
	private String motivo;

	@Column(name = "data_movimentacao", nullable = false)
	private LocalDateTime dataMovimentacao;

	@Column(name = "data_criacao", nullable = false)
	private LocalDateTime dataCriacao;

}

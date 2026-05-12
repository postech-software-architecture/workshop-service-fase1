package com.postech.workshop_service.infrastructure.persistence.entities;

import com.postech.workshop_service.domain.entities.StatusItemExecucao;
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
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Representacao persistente de um item de composicao tecnica da ordem de servico.
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ItemComposicaoTecnicaJpaEntity {

	@EqualsAndHashCode.Include
	@Column(name = "id", nullable = false)
	private UUID id;

	@Column(nullable = false)
	private String descricao;

	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal valor;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private TipoItemComposicaoTecnica tipo;

	@Column(name = "peca_insumo_id")
	private UUID idPecaInsumo;

	@Column(name = "servico_id")
	private UUID idServico;

	@Enumerated(EnumType.STRING)
	@Column(name = "status_execucao", length = 20)
	private StatusItemExecucao statusExecucao;

	@Column(name = "data_inicio_execucao")
	private LocalDateTime dataInicioExecucao;

	@Column(name = "data_finalizacao")
	private LocalDateTime dataFinalizacao;

}

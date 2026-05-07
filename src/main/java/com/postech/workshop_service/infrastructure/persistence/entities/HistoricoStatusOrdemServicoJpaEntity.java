package com.postech.workshop_service.infrastructure.persistence.entities;

import com.postech.workshop_service.domain.entities.StatusOrdemServico;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidade JPA do historico de status da ordem de servico.
 */
@Entity
@Table(name = "historico_status_os")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class HistoricoStatusOrdemServicoJpaEntity extends BaseJpaEntity {

	@Column(name = "ordem_servico_id", nullable = false)
	private UUID idOrdemServico;

	@Enumerated(EnumType.STRING)
	@Column(name = "status_anterior", nullable = false, length = 40)
	private StatusOrdemServico statusAnterior;

	@Enumerated(EnumType.STRING)
	@Column(name = "status_novo", nullable = false, length = 40)
	private StatusOrdemServico statusNovo;

	@Column(name = "data_transicao", nullable = false)
	private LocalDateTime dataTransicao;

	@Column(name = "usuario_id", nullable = false)
	private UUID idUsuario;

	@Column(name = "usuario_username", nullable = false, length = 120)
	private String usernameUsuario;

}

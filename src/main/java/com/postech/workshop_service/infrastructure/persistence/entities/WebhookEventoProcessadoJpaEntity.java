package com.postech.workshop_service.infrastructure.persistence.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Registro de um evento de webhook ja processado (idempotencia).
 */
@Entity
@Table(name = "webhook_eventos_processados")
public class WebhookEventoProcessadoJpaEntity {

	@Id
	@Column(name = "id_evento", length = 120)
	private String idEvento;

	@Column(name = "origem", length = 120)
	private String origem;

	@Column(name = "data_processamento", nullable = false)
	private LocalDateTime dataProcessamento;

	protected WebhookEventoProcessadoJpaEntity() {
	}

	public WebhookEventoProcessadoJpaEntity(String idEvento, String origem, LocalDateTime dataProcessamento) {
		this.idEvento = idEvento;
		this.origem = origem;
		this.dataProcessamento = dataProcessamento;
	}

	public String getIdEvento() {
		return idEvento;
	}

	public String getOrigem() {
		return origem;
	}

	public LocalDateTime getDataProcessamento() {
		return dataProcessamento;
	}

}

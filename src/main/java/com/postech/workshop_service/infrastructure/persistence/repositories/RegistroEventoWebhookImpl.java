package com.postech.workshop_service.infrastructure.persistence.repositories;

import com.postech.workshop_service.application.usecases.RegistroEventoWebhook;
import com.postech.workshop_service.infrastructure.persistence.entities.WebhookEventoProcessadoJpaEntity;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Adapter de idempotencia de eventos de webhook. A atomicidade vem da PK unica
 * {@code id_evento}: uma reentrega concorrente falha no insert e e tratada como ja
 * processada.
 */
@Component
public class RegistroEventoWebhookImpl implements RegistroEventoWebhook {

	private final JpaWebhookEventoProcessadoRepository repository;

	public RegistroEventoWebhookImpl(JpaWebhookEventoProcessadoRepository repository) {
		this.repository = repository;
	}

	// REQUIRES_NEW: o registro do evento tem vida propria — se a decisao subsequente
	// falhar,
	// a marca de processado ja foi confirmada e reentregas continuam bloqueadas.
	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public boolean registrarSeInedito(String idEvento, String origem) {
		if (idEvento == null || idEvento.isBlank()) {
			// Sem idEvento nao ha como deduplicar; deixa passar (dedup por estado no use
			// case).
			return true;
		}
		if (repository.existsById(idEvento)) {
			return false;
		}
		try {
			repository.saveAndFlush(new WebhookEventoProcessadoJpaEntity(idEvento, origem, LocalDateTime.now()));
			return true;
		}
		catch (DataIntegrityViolationException ex) {
			// Reentrega concorrente venceu a corrida no insert.
			return false;
		}
	}

}

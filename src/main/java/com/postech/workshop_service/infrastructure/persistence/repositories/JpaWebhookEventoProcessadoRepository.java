package com.postech.workshop_service.infrastructure.persistence.repositories;

import com.postech.workshop_service.infrastructure.persistence.entities.WebhookEventoProcessadoJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio Spring Data dos eventos de webhook processados.
 */
public interface JpaWebhookEventoProcessadoRepository extends JpaRepository<WebhookEventoProcessadoJpaEntity, String> {

}

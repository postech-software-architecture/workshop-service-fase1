package com.postech.workshop_service.infrastructure.persistence.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Classe base para entidades JPA que compartilham os campos de auditoria e identificacao.
 */
@MappedSuperclass
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseJpaEntity {

	@Id
	private UUID id;

	@Column(name = "data_criacao", nullable = false)
	private LocalDateTime dataCriacao;

	@Column(name = "data_ultima_atualizacao", nullable = false)
	private LocalDateTime dataUltimaAtualizacao;

	@Column(name = "data_remocao")
	private LocalDateTime dataRemocao;

}

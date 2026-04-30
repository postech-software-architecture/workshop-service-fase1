package com.postech.workshop_service.domain.entities;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Classe base abstrata que concentra os atributos comuns de auditoria e identificacao
 * compartilhados por todas as entidades raiz do dominio.
 */
@Getter
public abstract class EntidadeBase {

	private final UUID id;

	private final LocalDateTime dataCriacao;

	private LocalDateTime dataUltimaAtualizacao;

	private LocalDateTime dataRemocao;

	protected EntidadeBase(UUID id, LocalDateTime dataCriacao, LocalDateTime dataUltimaAtualizacao,
			LocalDateTime dataRemocao) {
		this.id = Objects.requireNonNull(id, "O identificador da entidade e obrigatorio.");
		this.dataCriacao = Objects.requireNonNull(dataCriacao, "A data de criacao da entidade e obrigatoria.");
		this.dataUltimaAtualizacao = Objects.requireNonNull(dataUltimaAtualizacao,
				"A data de ultima atualizacao da entidade e obrigatoria.");
		this.dataRemocao = dataRemocao;
	}

	protected EntidadeBase(UUID id) {
		this.id = Objects.requireNonNull(id, "O identificador da entidade e obrigatorio.");
		LocalDateTime agora = LocalDateTime.now();
		this.dataCriacao = agora;
		this.dataUltimaAtualizacao = agora;
		this.dataRemocao = null;
	}

	protected void atualizarDataUltimaAtualizacao() {
		this.dataUltimaAtualizacao = LocalDateTime.now();
	}

	protected void registrarRemocaoLogica() {
		this.dataRemocao = LocalDateTime.now();
		this.dataUltimaAtualizacao = this.dataRemocao;
	}

}

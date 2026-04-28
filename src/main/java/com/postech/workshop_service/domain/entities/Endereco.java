package com.postech.workshop_service.domain.entities;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidade que representa um endereço físico dentro do agregado de Cliente.
 */
@Getter
public class Endereco extends EntidadeBase {

	private String logradouro;

	private String numero;

	private String complemento;

	private String bairro;

	private String cidade;

	private String estado;

	private String cep;

	public Endereco(UUID id, String logradouro, String numero, String complemento, String bairro, String cidade,
			String estado, String cep) {
		super(id != null ? id : UUID.randomUUID());
		this.logradouro = logradouro;
		this.numero = numero;
		this.complemento = complemento;
		this.bairro = bairro;
		this.cidade = cidade;
		this.estado = estado;
		this.cep = cep != null ? cep.replaceAll("[^0-9]", "") : null;

		validar();
	}

	public Endereco(String logradouro, String numero, String complemento, String bairro, String cidade, String estado,
			String cep) {
		this(UUID.randomUUID(), logradouro, numero, complemento, bairro, cidade, estado, cep);
	}

	/**
	 * Reconstroi um endereco previamente persistido.
	 */
	@Default
	public Endereco(UUID id, String logradouro, String numero, String complemento, String bairro, String cidade,
			String estado, String cep, LocalDateTime dataCriacao, LocalDateTime dataUltimaAtualizacao,
			LocalDateTime dataRemocao) {
		super(id, dataCriacao, dataUltimaAtualizacao, dataRemocao);
		this.logradouro = logradouro;
		this.numero = numero;
		this.complemento = complemento;
		this.bairro = bairro;
		this.cidade = cidade;
		this.estado = estado;
		this.cep = cep != null ? cep.replaceAll("[^0-9]", "") : null;
		validar();
	}

	private void validar() {
		if (logradouro == null || logradouro.isBlank())
			throw new IllegalArgumentException("Logradouro é obrigatório.");
		if (cidade == null || cidade.isBlank())
			throw new IllegalArgumentException("Cidade é obrigatória.");
		if (estado == null || estado.isBlank())
			throw new IllegalArgumentException("Estado é obrigatório.");
		if (cep != null && cep.length() != 8)
			throw new IllegalArgumentException("CEP deve ter 8 dígitos.");
	}

}

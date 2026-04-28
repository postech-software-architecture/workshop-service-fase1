package com.postech.workshop_service.infrastructure.persistence.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

/**
 * Chave composta do vinculo entre veiculo e cliente.
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class VeiculoClienteId implements Serializable {

	@Column(name = "veiculo_id", nullable = false)
	private UUID veiculoId;

	@Column(name = "cliente_id", nullable = false)
	private UUID clienteId;

}

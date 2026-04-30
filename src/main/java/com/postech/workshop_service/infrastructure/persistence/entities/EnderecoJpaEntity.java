package com.postech.workshop_service.infrastructure.persistence.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Entity
@Table(name = "enderecos")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class EnderecoJpaEntity extends BaseJpaEntity {

	@Column(name = "cliente_id")
	private UUID clienteId;

	@OneToOne
	@JoinColumn(name = "cliente_id", insertable = false, updatable = false)
	private ClienteJpaEntity cliente;

	@Column(nullable = false)
	private String logradouro;

	@Column(length = 20)
	private String numero;

	@Column(length = 100)
	private String complemento;

	@Column(length = 100)
	private String bairro;

	@Column(nullable = false, length = 100)
	private String cidade;

	@Column(nullable = false, length = 50)
	private String estado;

	@Column(length = 10)
	private String cep;

}

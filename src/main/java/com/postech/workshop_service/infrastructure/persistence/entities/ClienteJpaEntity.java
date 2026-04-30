package com.postech.workshop_service.infrastructure.persistence.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Entity
@Table(name = "clientes")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ClienteJpaEntity extends BaseJpaEntity {

	@Column(nullable = false)
	private String nome;

	@Column(nullable = false, unique = true, length = 20)
	private String documento;

	@Column
	private String email;

	@Column(length = 20)
	private String telefone;

	@Column(name = "data_nascimento_fundacao")
	private LocalDate dataNascimentoFundacao;

	@Column(columnDefinition = "TEXT")
	private String observacoes;

	@Column(nullable = false)
	private boolean ativo;

	@OneToOne(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true)
	private EnderecoJpaEntity endereco;

}

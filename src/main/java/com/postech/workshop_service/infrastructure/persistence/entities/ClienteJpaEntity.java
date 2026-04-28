package com.postech.workshop_service.infrastructure.persistence.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "clientes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClienteJpaEntity {

	@Id
	private UUID id;

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

	@Column(name = "data_criacao", nullable = false)
	private LocalDateTime dataCriacao;

	@Column(name = "data_ultima_atualizacao", nullable = false)
	private LocalDateTime dataUltimaAtualizacao;

	@Column(name = "data_remocao")
	private LocalDateTime dataRemocao;

	@OneToOne(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true)
	private EnderecoJpaEntity endereco;

}

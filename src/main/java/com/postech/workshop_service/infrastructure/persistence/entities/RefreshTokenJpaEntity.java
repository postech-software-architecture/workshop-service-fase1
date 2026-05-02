package com.postech.workshop_service.infrastructure.persistence.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * Entidade JPA do refresh token persistido.
 */
@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class RefreshTokenJpaEntity extends BaseJpaEntity {

	@Column(nullable = false, unique = true)
	private String token;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "usuario_id", nullable = false)
	private UsuarioJpaEntity usuario;

	@Column(name = "data_expiracao", nullable = false)
	private LocalDateTime dataExpiracao;

	@Column(nullable = false)
	private boolean revogado;

	@Column(name = "data_revogacao")
	private LocalDateTime dataRevogacao;

}

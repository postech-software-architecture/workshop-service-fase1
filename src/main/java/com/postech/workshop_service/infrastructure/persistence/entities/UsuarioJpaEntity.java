package com.postech.workshop_service.infrastructure.persistence.entities;

import com.postech.workshop_service.domain.enums.Role;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Entidade JPA da conta autenticavel.
 */
@Entity
@Table(name = "usuarios")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UsuarioJpaEntity extends BaseJpaEntity {

	@Column(nullable = false, unique = true, length = 100)
	private String username;

	@Column(unique = true)
	private String email;

	@Column(name = "senha_hash", nullable = false)
	private String senhaHash;

	@Column(nullable = false)
	private boolean ativo;

	@Column(nullable = false)
	private boolean bloqueado;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "cliente_id")
	private ClienteJpaEntity cliente;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "usuarios_roles", joinColumns = @JoinColumn(name = "usuario_id"))
	@Enumerated(EnumType.STRING)
	@Column(name = "role", nullable = false)
	@Builder.Default
	private Set<Role> roles = new LinkedHashSet<>();

}

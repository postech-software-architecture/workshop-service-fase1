package com.postech.workshop_service.domain.entities;

import com.postech.workshop_service.domain.enums.Role;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Entidade raiz que representa uma conta autenticavel do sistema.
 */
@Getter
public class Usuario extends EntidadeBase {

	private String username;

	private String email;

	private String senhaHash;

	private final Set<Role> roles;

	private UUID clienteId;

	private boolean ativo;

	private boolean bloqueado;

	/**
	 * Cria uma nova conta autenticavel.
	 * @param username identificador principal da conta.
	 * @param email email opcional para login alternativo.
	 * @param senhaHash hash nao reversivel da senha.
	 * @param roles conjunto de papeis autorizados.
	 * @param clienteId cliente vinculado quando houver papel CLIENTE.
	 */
	public Usuario(String username, String email, String senhaHash, Collection<Role> roles, UUID clienteId) {
		super(UUID.randomUUID());
		this.roles = new LinkedHashSet<>();
		this.ativo = true;
		this.bloqueado = false;
		this.username = sanitizarObrigatorio(username, "O username do usuario e obrigatorio.");
		this.email = sanitizarOpcional(email);
		this.senhaHash = sanitizarObrigatorio(senhaHash, "O hash da senha do usuario e obrigatorio.");
		definirRoles(roles);
		definirClienteId(clienteId);
	}

	/**
	 * Reconstroi uma conta previamente persistida.
	 * @param id identificador tecnico do usuario.
	 * @param username identificador principal da conta.
	 * @param email email opcional.
	 * @param senhaHash hash nao reversivel da senha.
	 * @param roles conjunto de papeis autorizados.
	 * @param clienteId cliente vinculado quando houver papel CLIENTE.
	 * @param ativo indicador de conta ativa.
	 * @param bloqueado indicador de bloqueio.
	 * @param dataCriacao data de criacao.
	 * @param dataUltimaAtualizacao data da ultima atualizacao.
	 * @param dataRemocao data da remocao logica.
	 */
	@Default
	public Usuario(UUID id, String username, String email, String senhaHash, Collection<Role> roles, UUID clienteId,
			boolean ativo, boolean bloqueado, LocalDateTime dataCriacao, LocalDateTime dataUltimaAtualizacao,
			LocalDateTime dataRemocao) {
		super(id, dataCriacao, dataUltimaAtualizacao, dataRemocao);
		this.roles = new LinkedHashSet<>();
		this.username = sanitizarObrigatorio(username, "O username do usuario e obrigatorio.");
		this.email = sanitizarOpcional(email);
		this.senhaHash = sanitizarObrigatorio(senhaHash, "O hash da senha do usuario e obrigatorio.");
		this.ativo = ativo;
		this.bloqueado = bloqueado;
		definirRoles(roles);
		definirClienteId(clienteId);
	}

	/**
	 * Informa se a conta pode autenticar no momento.
	 * @return true quando a conta estiver ativa, sem bloqueio e sem remocao logica.
	 */
	public boolean podeAutenticar() {
		return this.ativo && !this.bloqueado && getDataRemocao() == null;
	}

	/**
	 * Verifica se a conta possui determinado papel.
	 * @param role papel consultado.
	 * @return true quando o papel existir.
	 */
	public boolean possuiRole(Role role) {
		return this.roles.contains(role);
	}

	/**
	 * Atualiza o hash de senha da conta.
	 * @param novaSenhaHash novo hash de senha.
	 */
	public void atualizarSenha(String novaSenhaHash) {
		this.senhaHash = sanitizarObrigatorio(novaSenhaHash, "O hash da senha do usuario e obrigatorio.");
		atualizarDataUltimaAtualizacao();
	}

	/**
	 * Bloqueia a conta para impedir autenticacao.
	 */
	public void bloquear() {
		this.bloqueado = true;
		atualizarDataUltimaAtualizacao();
	}

	/**
	 * Desbloqueia a conta.
	 */
	public void desbloquear() {
		this.bloqueado = false;
		atualizarDataUltimaAtualizacao();
	}

	private void definirRoles(Collection<Role> roles) {
		if (roles == null || roles.isEmpty()) {
			throw new IllegalArgumentException("O usuario deve possuir ao menos um papel de acesso.");
		}
		LinkedHashSet<Role> rolesNormalizados = new LinkedHashSet<>(roles);
		if (rolesNormalizados.contains(null)) {
			throw new IllegalArgumentException("Nao e permitido informar papel de acesso nulo.");
		}
		this.roles.clear();
		this.roles.addAll(rolesNormalizados);
	}

	private void definirClienteId(UUID clienteId) {
		if (this.roles.contains(Role.CLIENTE) && clienteId == null) {
			throw new IllegalArgumentException("Usuarios com papel CLIENTE devem estar vinculados a um cliente.");
		}
		if (!this.roles.contains(Role.CLIENTE) && clienteId != null) {
			throw new IllegalArgumentException("A vinculacao com cliente exige o papel CLIENTE.");
		}
		this.clienteId = clienteId;
	}

	private String sanitizarObrigatorio(String valor, String mensagem) {
		String sanitizado = sanitizarOpcional(valor);
		if (sanitizado == null) {
			throw new IllegalArgumentException(mensagem);
		}
		return sanitizado;
	}

	private String sanitizarOpcional(String valor) {
		if (valor == null) {
			return null;
		}
		String sanitizado = valor.trim().replaceAll("\\s+", " ");
		return sanitizado.isEmpty() ? null : sanitizado;
	}

	@Override
	public int hashCode() {
		return Objects.hash(getId());
	}

}

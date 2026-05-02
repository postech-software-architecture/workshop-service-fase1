package com.postech.workshop_service.domain.entities;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidade raiz que representa um refresh token persistido.
 */
@Getter
public class RefreshToken extends EntidadeBase {

	private String token;

	private UUID usuarioId;

	private LocalDateTime dataExpiracao;

	private boolean revogado;

	private LocalDateTime dataRevogacao;

	/**
	 * Cria um novo refresh token ativo.
	 * @param token valor opaco da credencial.
	 * @param usuarioId identificador do usuario dono da credencial.
	 * @param dataExpiracao prazo maximo de uso.
	 */
	public RefreshToken(String token, UUID usuarioId, LocalDateTime dataExpiracao) {
		super(UUID.randomUUID());
		this.token = sanitizarObrigatorio(token, "O token de renovacao e obrigatorio.");
		this.usuarioId = validarUsuarioId(usuarioId);
		this.dataExpiracao = validarDataExpiracao(dataExpiracao);
		this.revogado = false;
		this.dataRevogacao = null;
	}

	/**
	 * Reconstroi um refresh token previamente persistido.
	 * @param id identificador tecnico do refresh token.
	 * @param token valor opaco da credencial.
	 * @param usuarioId identificador do usuario dono da credencial.
	 * @param dataExpiracao prazo maximo de uso.
	 * @param revogado indicador de revogacao.
	 * @param dataRevogacao data da revogacao.
	 * @param dataCriacao data de criacao.
	 * @param dataUltimaAtualizacao data da ultima atualizacao.
	 * @param dataRemocao data de remocao logica.
	 */
	@Default
	public RefreshToken(UUID id, String token, UUID usuarioId, LocalDateTime dataExpiracao, boolean revogado,
			LocalDateTime dataRevogacao, LocalDateTime dataCriacao, LocalDateTime dataUltimaAtualizacao,
			LocalDateTime dataRemocao) {
		super(id, dataCriacao, dataUltimaAtualizacao, dataRemocao);
		this.token = sanitizarObrigatorio(token, "O token de renovacao e obrigatorio.");
		this.usuarioId = validarUsuarioId(usuarioId);
		this.dataExpiracao = validarDataExpiracao(dataExpiracao);
		this.revogado = revogado;
		this.dataRevogacao = dataRevogacao;
	}

	/**
	 * Indica se a credencial ainda pode ser usada para renovacao.
	 * @return true quando o token estiver dentro do prazo e sem revogacao.
	 */
	public boolean estaAtivo() {
		return !this.revogado && this.dataExpiracao.isAfter(LocalDateTime.now());
	}

	/**
	 * Revoga a credencial de renovacao.
	 */
	public void revogar() {
		if (this.revogado) {
			return;
		}
		this.revogado = true;
		this.dataRevogacao = LocalDateTime.now();
		atualizarDataUltimaAtualizacao();
	}

	private UUID validarUsuarioId(UUID usuarioId) {
		if (usuarioId == null) {
			throw new IllegalArgumentException("O usuario do refresh token e obrigatorio.");
		}
		return usuarioId;
	}

	private LocalDateTime validarDataExpiracao(LocalDateTime dataExpiracao) {
		if (dataExpiracao == null) {
			throw new IllegalArgumentException("A data de expiracao do refresh token e obrigatoria.");
		}
		return dataExpiracao;
	}

	private String sanitizarObrigatorio(String valor, String mensagem) {
		if (valor == null || valor.trim().isEmpty()) {
			throw new IllegalArgumentException(mensagem);
		}
		return valor.trim();
	}

}

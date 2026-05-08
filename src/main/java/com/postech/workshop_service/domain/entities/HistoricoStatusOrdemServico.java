package com.postech.workshop_service.domain.entities;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Registro auditavel de uma transicao de status da ordem de servico.
 */
public class HistoricoStatusOrdemServico extends EntidadeBase {

	private final UUID idOrdemServico;

	private final StatusOrdemServico statusAnterior;

	private final StatusOrdemServico statusNovo;

	private final LocalDateTime dataTransicao;

	private final UUID idUsuario;

	private final String usernameUsuario;

	public HistoricoStatusOrdemServico(UUID id, UUID idOrdemServico, StatusOrdemServico statusAnterior,
			StatusOrdemServico statusNovo, LocalDateTime dataTransicao, UUID idUsuario, String usernameUsuario) {
		super(id != null ? id : UUID.randomUUID());
		this.idOrdemServico = validarIdentificador(idOrdemServico,
				"O identificador da ordem de servico e obrigatorio.");
		this.statusAnterior = validarStatus(statusAnterior, "O status anterior da ordem de servico e obrigatorio.");
		this.statusNovo = validarStatus(statusNovo, "O novo status da ordem de servico e obrigatorio.");
		if (this.statusAnterior == this.statusNovo) {
			throw new IllegalArgumentException("O status novo deve ser diferente do status anterior.");
		}
		this.dataTransicao = dataTransicao != null ? dataTransicao : LocalDateTime.now();
		this.idUsuario = validarIdentificador(idUsuario, "O identificador do usuario responsavel e obrigatorio.");
		this.usernameUsuario = validarUsername(usernameUsuario);
	}

	@Default
	public HistoricoStatusOrdemServico(UUID id, UUID idOrdemServico, StatusOrdemServico statusAnterior,
			StatusOrdemServico statusNovo, LocalDateTime dataTransicao, UUID idUsuario, String usernameUsuario,
			LocalDateTime dataCriacao, LocalDateTime dataUltimaAtualizacao, LocalDateTime dataRemocao) {
		super(id, dataCriacao, dataUltimaAtualizacao, dataRemocao);
		this.idOrdemServico = validarIdentificador(idOrdemServico,
				"O identificador da ordem de servico e obrigatorio.");
		this.statusAnterior = validarStatus(statusAnterior, "O status anterior da ordem de servico e obrigatorio.");
		this.statusNovo = validarStatus(statusNovo, "O novo status da ordem de servico e obrigatorio.");
		if (this.statusAnterior == this.statusNovo) {
			throw new IllegalArgumentException("O status novo deve ser diferente do status anterior.");
		}
		this.dataTransicao = dataTransicao != null ? dataTransicao : LocalDateTime.now();
		this.idUsuario = validarIdentificador(idUsuario, "O identificador do usuario responsavel e obrigatorio.");
		this.usernameUsuario = validarUsername(usernameUsuario);
	}

	public UUID getIdOrdemServico() {
		return idOrdemServico;
	}

	public StatusOrdemServico getStatusAnterior() {
		return statusAnterior;
	}

	public StatusOrdemServico getStatusNovo() {
		return statusNovo;
	}

	public LocalDateTime getDataTransicao() {
		return dataTransicao;
	}

	public UUID getIdUsuario() {
		return idUsuario;
	}

	public String getUsernameUsuario() {
		return usernameUsuario;
	}

	private UUID validarIdentificador(UUID identificador, String mensagem) {
		if (identificador == null) {
			throw new IllegalArgumentException(mensagem);
		}
		return identificador;
	}

	private StatusOrdemServico validarStatus(StatusOrdemServico status, String mensagem) {
		if (status == null) {
			throw new IllegalArgumentException(mensagem);
		}
		return status;
	}

	private String validarUsername(String username) {
		if (username == null || username.isBlank()) {
			throw new IllegalArgumentException("O username do usuario responsavel e obrigatorio.");
		}
		return username.trim();
	}

}

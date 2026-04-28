package com.postech.workshop_service.domain.entities;

import com.postech.workshop_service.application.exceptions.RegraDeNegocioException;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidade raiz que representa uma ordem de servico da oficina.
 */
@Getter
public class OrdemServico extends EntidadeBase {

	private final UUID idCliente;

	private final UUID idVeiculo;

	private StatusOrdemServico status;

	/**
	 * Cria uma nova ordem de servico com os vinculos obrigatorios.
	 * @param id identificador tecnico da ordem de servico.
	 * @param idCliente identificador do cliente vinculado.
	 * @param idVeiculo identificador do veiculo vinculado.
	 */
	public OrdemServico(UUID id, UUID idCliente, UUID idVeiculo) {
		super(id != null ? id : UUID.randomUUID());
		this.idCliente = validarIdentificador(idCliente, "O identificador do cliente e obrigatorio.");
		this.idVeiculo = validarIdentificador(idVeiculo, "O identificador do veiculo e obrigatorio.");
		this.status = StatusOrdemServico.RECEBIDA;
	}

	/**
	 * Reconstroi uma ordem de servico previamente persistida.
	 * @param id identificador tecnico da ordem de servico.
	 * @param idCliente identificador do cliente vinculado.
	 * @param idVeiculo identificador do veiculo vinculado.
	 * @param status estado atual da ordem de servico.
	 * @param dataCriacao data de criacao.
	 * @param dataUltimaAtualizacao data da ultima atualizacao.
	 * @param dataRemocao data da remocao logica.
	 */
	@Default
	public OrdemServico(UUID id, UUID idCliente, UUID idVeiculo, StatusOrdemServico status, LocalDateTime dataCriacao,
			LocalDateTime dataUltimaAtualizacao, LocalDateTime dataRemocao) {
		super(id, dataCriacao, dataUltimaAtualizacao, dataRemocao);
		this.idCliente = validarIdentificador(idCliente, "O identificador do cliente e obrigatorio.");
		this.idVeiculo = validarIdentificador(idVeiculo, "O identificador do veiculo e obrigatorio.");
		this.status = validarStatus(status);
	}

	/**
	 * Indica se a ordem de servico pode ser cancelada no estado atual.
	 * @return {@code true} quando o cancelamento e permitido.
	 */
	public boolean podeSerCancelada() {
		return this.status == StatusOrdemServico.RECEBIDA
				|| this.status == StatusOrdemServico.AGUARDANDO_APROVACAO_ORCAMENTO;
	}

	/**
	 * Cancela a ordem de servico quando o estado atual permite essa operacao.
	 * @throws RegraDeNegocioException quando a ordem de servico nao pode ser cancelada.
	 */
	public void cancelar() {
		if (!podeSerCancelada()) {
			throw new RegraDeNegocioException(
					"Nao e permitido cancelar uma ordem de servico com status " + this.status + ".");
		}
		this.status = StatusOrdemServico.CANCELADA;
		atualizarDataUltimaAtualizacao();
	}

	/**
	 * Avanca a ordem de servico para o estado de execucao.
	 * @throws RegraDeNegocioException quando a ordem de servico nao puder entrar em
	 * execucao.
	 */
	public void iniciarExecucao() {
		if (this.status != StatusOrdemServico.RECEBIDA
				&& this.status != StatusOrdemServico.AGUARDANDO_APROVACAO_ORCAMENTO) {
			throw new RegraDeNegocioException(
					"Nao e permitido iniciar a execucao de uma ordem de servico com status " + this.status + ".");
		}
		this.status = StatusOrdemServico.EM_EXECUCAO;
		atualizarDataUltimaAtualizacao();
	}

	private UUID validarIdentificador(UUID identificador, String mensagem) {
		if (identificador == null) {
			throw new IllegalArgumentException(mensagem);
		}
		return identificador;
	}

	private StatusOrdemServico validarStatus(StatusOrdemServico status) {
		if (status == null) {
			throw new IllegalArgumentException("O status da ordem de servico e obrigatorio.");
		}
		return status;
	}

}

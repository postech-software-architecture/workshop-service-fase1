package com.postech.workshop_service.domain.entities;

import com.postech.workshop_service.application.exceptions.RegraDeNegocioException;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Entidade raiz que representa uma ordem de servico da oficina.
 */
@Getter
public class OrdemServico extends EntidadeBase {

	private final UUID idCliente;

	private final UUID idVeiculo;

	private StatusOrdemServico status;

	private final List<ItemComposicaoTecnica> itensComposicao;

	private final String numero;

	private final String observacoes;

	/**
	 * Cria uma nova ordem de servico vazia, em composicao, sem numero definido. Usado
	 * internamente e em testes de dominio.
	 * @param id identificador tecnico; nulo gera UUID automatico.
	 * @param idCliente identificador do cliente vinculado.
	 * @param idVeiculo identificador do veiculo vinculado.
	 */
	public OrdemServico(UUID id, UUID idCliente, UUID idVeiculo) {
		super(id != null ? id : UUID.randomUUID());
		this.idCliente = validarIdentificador(idCliente, "O identificador do cliente e obrigatorio.");
		this.idVeiculo = validarIdentificador(idVeiculo, "O identificador do veiculo e obrigatorio.");
		this.status = StatusOrdemServico.EM_COMPOSICAO;
		this.itensComposicao = List.of();
		this.numero = null;
		this.observacoes = null;
	}

	/**
	 * Cria uma nova ordem de servico na recepcao do veiculo, com composicao tecnica ja
	 * definida e numero sequencial gerado.
	 * @param id identificador tecnico; nulo gera UUID automatico.
	 * @param idCliente identificador do cliente vinculado.
	 * @param idVeiculo identificador do veiculo vinculado.
	 * @param numero numero sequencial unico no formato OS-{ANO}-{NNNNN}.
	 * @param observacoes observacoes opcionais do atendente.
	 * @param itensComposicao itens de servicos e pecas ja levantados.
	 */
	public OrdemServico(UUID id, UUID idCliente, UUID idVeiculo, String numero, String observacoes,
			Collection<ItemComposicaoTecnica> itensComposicao) {
		super(id != null ? id : UUID.randomUUID());
		this.idCliente = validarIdentificador(idCliente, "O identificador do cliente e obrigatorio.");
		this.idVeiculo = validarIdentificador(idVeiculo, "O identificador do veiculo e obrigatorio.");
		this.numero = validarNumero(numero);
		this.observacoes = sanitizarOpcional(observacoes);
		this.status = StatusOrdemServico.EM_COMPOSICAO;
		this.itensComposicao = validarItensComposicao(itensComposicao);
	}

	/**
	 * Reconstroi uma ordem de servico previamente persistida.
	 * @param id identificador tecnico da ordem de servico.
	 * @param idCliente identificador do cliente vinculado.
	 * @param idVeiculo identificador do veiculo vinculado.
	 * @param status estado atual da ordem de servico.
	 * @param itensComposicao itens da composicao tecnica.
	 * @param numero numero sequencial da OS.
	 * @param observacoes observacoes registradas na recepcao.
	 * @param dataCriacao data de criacao.
	 * @param dataUltimaAtualizacao data da ultima atualizacao.
	 * @param dataRemocao data da remocao logica.
	 */
	@Default
	public OrdemServico(UUID id, UUID idCliente, UUID idVeiculo, StatusOrdemServico status,
			Collection<ItemComposicaoTecnica> itensComposicao, String numero, String observacoes,
			LocalDateTime dataCriacao, LocalDateTime dataUltimaAtualizacao, LocalDateTime dataRemocao) {
		super(id, dataCriacao, dataUltimaAtualizacao, dataRemocao);
		this.idCliente = validarIdentificador(idCliente, "O identificador do cliente e obrigatorio.");
		this.idVeiculo = validarIdentificador(idVeiculo, "O identificador do veiculo e obrigatorio.");
		this.status = validarStatus(status);
		this.itensComposicao = validarItensComposicao(itensComposicao);
		this.numero = numero;
		this.observacoes = sanitizarOpcional(observacoes);
	}

	/**
	 * Encerra a composicao tecnica quando houver pelo menos um item cadastrado.
	 */
	public void encerrarComposicao() {
		if (this.status != StatusOrdemServico.EM_COMPOSICAO) {
			throw new RegraDeNegocioException(
					"Nao e permitido encerrar a composicao tecnica de uma ordem de servico com status " + this.status
							+ ".");
		}
		if (this.itensComposicao.isEmpty()) {
			throw new RegraDeNegocioException(
					"Nao e permitido encerrar a composicao tecnica de uma ordem de servico sem itens.");
		}
		this.status = StatusOrdemServico.AGUARDANDO_RESPOSTA_CLIENTE;
		atualizarDataUltimaAtualizacao();
	}

	/**
	 * Retorna a ordem de servico para a fase de composicao tecnica.
	 */
	public void voltarParaComposicao() {
		if (this.status != StatusOrdemServico.AGUARDANDO_RESPOSTA_CLIENTE) {
			throw new RegraDeNegocioException(
					"Nao e permitido voltar para composicao uma ordem de servico com status " + this.status + ".");
		}
		this.status = StatusOrdemServico.EM_COMPOSICAO;
		atualizarDataUltimaAtualizacao();
	}

	/**
	 * Marca a ordem como aguardando execucao apos aprovacao do orcamento.
	 */
	public void marcarComoAguardandoExecucao() {
		if (this.status != StatusOrdemServico.AGUARDANDO_RESPOSTA_CLIENTE) {
			throw new RegraDeNegocioException(
					"Nao e permitido marcar como aguardando execucao uma ordem de servico com status " + this.status
							+ ".");
		}
		this.status = StatusOrdemServico.AGUARDANDO_EXECUCAO;
		atualizarDataUltimaAtualizacao();
	}

	/**
	 * Cancela a ordem de servico quando o estado atual permite essa operacao.
	 * @throws RegraDeNegocioException quando a ordem de servico nao pode ser cancelada.
	 */
	public void cancelar() {
		if (this.status != StatusOrdemServico.AGUARDANDO_RESPOSTA_CLIENTE) {
			throw new RegraDeNegocioException(
					"Nao e permitido cancelar uma ordem de servico com status " + this.status + ".");
		}
		this.status = StatusOrdemServico.CANCELADA;
		atualizarDataUltimaAtualizacao();
	}

	/**
	 * Indica se a ordem possui ao menos um item de composicao tecnica.
	 * @return {@code true} quando houver itens cadastrados.
	 */
	public boolean possuiItensComposicao() {
		return !this.itensComposicao.isEmpty();
	}

	/**
	 * Indica se a ordem de servico pode ser cancelada no estado atual.
	 * @return {@code true} apenas quando o status for
	 * {@link StatusOrdemServico#AGUARDANDO_RESPOSTA_CLIENTE}.
	 */
	public boolean podeSerCancelada() {
		return this.status == StatusOrdemServico.AGUARDANDO_RESPOSTA_CLIENTE;
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

	private String validarNumero(String numero) {
		if (numero == null || numero.isBlank()) {
			throw new IllegalArgumentException("O numero da ordem de servico e obrigatorio.");
		}
		return numero.trim();
	}

	private String sanitizarOpcional(String valor) {
		if (valor == null) {
			return null;
		}
		String sanitizado = valor.trim().replaceAll("\\s+", " ");
		return sanitizado.isEmpty() ? null : sanitizado;
	}

	private List<ItemComposicaoTecnica> validarItensComposicao(Collection<ItemComposicaoTecnica> itensComposicao) {
		if (itensComposicao == null) {
			return List.of();
		}

		List<ItemComposicaoTecnica> itensValidados = new ArrayList<>();
		for (ItemComposicaoTecnica item : itensComposicao) {
			if (item == null) {
				throw new IllegalArgumentException("Nao e permitido informar item nulo na ordem de servico.");
			}
			itensValidados.add(item);
		}
		return List.copyOf(itensValidados);
	}

}

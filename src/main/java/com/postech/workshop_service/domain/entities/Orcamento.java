package com.postech.workshop_service.domain.entities;

import com.postech.workshop_service.application.exceptions.RegraDeNegocioException;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Entidade raiz que representa um orcamento vinculado a uma ordem de servico.
 */
@Getter
public class Orcamento extends EntidadeBase {

	private final UUID idOrdemServico;

	private final BigDecimal valor;

	private StatusOrcamento status;

	private final List<ItemOrcamento> itens;

	private final TipoOrcamento tipo;

	/**
	 * Cria um novo orcamento com os dados obrigatorios e status inicial criado.
	 * @param id identificador tecnico do orcamento.
	 * @param idOrdemServico identificador da ordem de servico vinculada.
	 * @param valor valor total do orcamento.
	 * @param itens itens cobrados no orcamento.
	 * @param tipo tipo do orcamento.
	 */
	public Orcamento(UUID id, UUID idOrdemServico, BigDecimal valor, Collection<ItemOrcamento> itens,
			TipoOrcamento tipo) {
		super(id != null ? id : UUID.randomUUID());
		this.idOrdemServico = validarIdentificador(idOrdemServico);
		this.valor = validarValor(valor);
		this.itens = validarItens(itens);
		this.tipo = validarTipo(tipo);
		this.status = StatusOrcamento.CRIADO;
	}

	/**
	 * Reconstroi um orcamento previamente persistido.
	 * @param id identificador tecnico do orcamento.
	 * @param idOrdemServico identificador da ordem de servico vinculada.
	 * @param valor valor total do orcamento.
	 * @param itens itens cobrados no orcamento.
	 * @param tipo tipo do orcamento.
	 * @param status estado atual do orcamento.
	 * @param dataCriacao data de criacao.
	 * @param dataUltimaAtualizacao data da ultima atualizacao.
	 * @param dataRemocao data da remocao logica.
	 */
	@Default
	public Orcamento(UUID id, UUID idOrdemServico, BigDecimal valor, Collection<ItemOrcamento> itens,
			TipoOrcamento tipo, StatusOrcamento status, LocalDateTime dataCriacao, LocalDateTime dataUltimaAtualizacao,
			LocalDateTime dataRemocao) {
		super(id, dataCriacao, dataUltimaAtualizacao, dataRemocao);
		this.idOrdemServico = validarIdentificador(idOrdemServico);
		this.valor = validarValor(valor);
		this.itens = validarItens(itens);
		this.tipo = validarTipo(tipo);
		this.status = validarStatus(status);
	}

	/**
	 * Envia o orcamento para aprovacao do cliente.
	 * @throws RegraDeNegocioException quando o orcamento nao estiver criado.
	 */
	public void enviarParaAprovacao() {
		if (this.status != StatusOrcamento.CRIADO) {
			throw new RegraDeNegocioException(
					"Nao e permitido enviar para aprovacao um orcamento com status " + this.status + ".");
		}
		this.status = StatusOrcamento.PENDENTE_APROVACAO;
		atualizarDataUltimaAtualizacao();
	}

	/**
	 * Aprova o orcamento e, quando original, avanca a ordem vinculada para execucao.
	 * @param ordemServico ordem de servico vinculada ao orcamento.
	 * @throws RegraDeNegocioException quando o orcamento nao estiver pendente de
	 * aprovacao.
	 */
	public void aprovar(OrdemServico ordemServico) {
		if (this.status != StatusOrcamento.PENDENTE_APROVACAO) {
			throw new RegraDeNegocioException("Nao e permitido aprovar um orcamento com status " + this.status + ".");
		}

		validarOrdemServicoVinculada(ordemServico);

		if (this.tipo == TipoOrcamento.SERVICO_ORIGINAL) {
			ordemServico.iniciarExecucao();
		}

		this.status = StatusOrcamento.APROVADO;
		atualizarDataUltimaAtualizacao();
	}

	/**
	 * Rejeita o orcamento pendente de aprovacao.
	 * @throws RegraDeNegocioException quando o orcamento nao estiver pendente de
	 * aprovacao.
	 */
	public void rejeitar() {
		if (this.status != StatusOrcamento.PENDENTE_APROVACAO) {
			throw new RegraDeNegocioException("Nao e permitido rejeitar um orcamento com status " + this.status + ".");
		}
		this.status = StatusOrcamento.REJEITADO;
		atualizarDataUltimaAtualizacao();
	}

	/**
	 * Cancela o orcamento e, quando aplicavel, tenta cancelar a ordem de servico
	 * vinculada.
	 * @param ordemServico ordem de servico vinculada ao orcamento.
	 * @throws RegraDeNegocioException quando o orcamento nao puder ser cancelado.
	 */
	public void cancelar(OrdemServico ordemServico) {
		if (!podeSerCancelado()) {
			throw new RegraDeNegocioException("Nao e permitido cancelar um orcamento com status " + this.status + ".");
		}

		validarOrdemServicoVinculada(ordemServico);

		this.status = StatusOrcamento.CANCELADO;
		atualizarDataUltimaAtualizacao();

		if (this.tipo == TipoOrcamento.SERVICO_ORIGINAL && ordemServico.podeSerCancelada()) {
			ordemServico.cancelar();
		}
	}

	private boolean podeSerCancelado() {
		return this.status == StatusOrcamento.CRIADO || this.status == StatusOrcamento.PENDENTE_APROVACAO
				|| this.status == StatusOrcamento.APROVADO;
	}

	private UUID validarIdentificador(UUID idOrdemServico) {
		if (idOrdemServico == null) {
			throw new IllegalArgumentException("O identificador da ordem de servico e obrigatorio.");
		}
		return idOrdemServico;
	}

	private BigDecimal validarValor(BigDecimal valor) {
		if (valor == null) {
			throw new IllegalArgumentException("O valor do orcamento e obrigatorio.");
		}
		return valor;
	}

	private List<ItemOrcamento> validarItens(Collection<ItemOrcamento> itens) {
		if (itens == null || itens.isEmpty()) {
			throw new IllegalArgumentException("O orcamento deve possuir ao menos um item.");
		}

		List<ItemOrcamento> itensValidados = new ArrayList<>();
		for (ItemOrcamento item : itens) {
			if (item == null) {
				throw new IllegalArgumentException("Nao e permitido informar item nulo no orcamento.");
			}
			itensValidados.add(item);
		}

		return List.copyOf(itensValidados);
	}

	private TipoOrcamento validarTipo(TipoOrcamento tipo) {
		if (tipo == null) {
			throw new IllegalArgumentException("O tipo do orcamento e obrigatorio.");
		}
		return tipo;
	}

	private StatusOrcamento validarStatus(StatusOrcamento status) {
		if (status == null) {
			throw new IllegalArgumentException("O status do orcamento e obrigatorio.");
		}
		return status;
	}

	private void validarOrdemServicoVinculada(OrdemServico ordemServico) {
		if (ordemServico == null) {
			throw new IllegalArgumentException("A ordem de servico vinculada e obrigatoria.");
		}
		if (!this.idOrdemServico.equals(ordemServico.getId())) {
			throw new IllegalArgumentException("A ordem de servico informada nao corresponde ao orcamento.");
		}
	}

}

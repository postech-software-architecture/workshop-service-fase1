package com.postech.workshop_service.domain.entities;

import com.postech.workshop_service.domain.valueobjects.TipoMovimentacao;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidade que representa uma movimentacao de estoque.
 *
 * <p>
 * Registra todas as alteracoes de quantidade em um estoque especifico, mantendo o
 * historico completo de entradas, saidas e ajustes.
 * </p>
 */
@Getter
public class MovimentacaoEstoque {

	private final UUID id;

	private final UUID estoqueId;

	private final TipoMovimentacao tipo;

	private final BigDecimal quantidade;

	private final BigDecimal quantidadeAnterior;

	private final BigDecimal quantidadePosterior;

	private final String motivo;

	private final LocalDateTime dataMovimentacao;

	private final LocalDateTime dataCriacao;

	/**
	 * Cria uma nova movimentacao de estoque.
	 * @param id identificador tecnico da movimentacao.
	 * @param estoqueId identificador do estoque.
	 * @param tipo tipo de movimentacao.
	 * @param quantidade quantidade movimentada.
	 * @param quantidadeAnterior quantidade antes da movimentacao.
	 * @param quantidadePosterior quantidade apos a movimentacao.
	 * @param motivo motivo/justificativa.
	 */
	public MovimentacaoEstoque(UUID id, UUID estoqueId, TipoMovimentacao tipo, BigDecimal quantidade,
			BigDecimal quantidadeAnterior, BigDecimal quantidadePosterior, String motivo) {
		this.id = id != null ? id : UUID.randomUUID();
		this.estoqueId = Objects.requireNonNull(estoqueId, "O identificador do estoque e obrigatorio.");
		this.tipo = Objects.requireNonNull(tipo, "O tipo de movimentacao e obrigatorio.");
		this.quantidade = Objects.requireNonNull(quantidade, "A quantidade movimentada e obrigatoria.");
		this.quantidadeAnterior = Objects.requireNonNull(quantidadeAnterior, "A quantidade anterior e obrigatoria.");
		this.quantidadePosterior = Objects.requireNonNull(quantidadePosterior, "A quantidade posterior e obrigatoria.");
		this.motivo = sanitizarMotivo(motivo, tipo);
		this.dataMovimentacao = LocalDateTime.now();
		this.dataCriacao = this.dataMovimentacao;
	}

	/**
	 * Reconstroi uma movimentacao previamente persistida.
	 * @param id identificador tecnico da movimentacao.
	 * @param estoqueId identificador do estoque.
	 * @param tipo tipo de movimentacao.
	 * @param quantidade quantidade movimentada.
	 * @param quantidadeAnterior quantidade antes da movimentacao.
	 * @param quantidadePosterior quantidade apos a movimentacao.
	 * @param motivo motivo/justificativa.
	 * @param dataMovimentacao data/hora da movimentacao.
	 * @param dataCriacao data de criacao do registro.
	 */
	@Default
	public MovimentacaoEstoque(UUID id, UUID estoqueId, TipoMovimentacao tipo, BigDecimal quantidade,
			BigDecimal quantidadeAnterior, BigDecimal quantidadePosterior, String motivo,
			LocalDateTime dataMovimentacao, LocalDateTime dataCriacao) {
		this.id = Objects.requireNonNull(id, "O identificador da movimentacao e obrigatorio.");
		this.estoqueId = Objects.requireNonNull(estoqueId, "O identificador do estoque e obrigatorio.");
		this.tipo = Objects.requireNonNull(tipo, "O tipo de movimentacao e obrigatorio.");
		this.quantidade = Objects.requireNonNull(quantidade, "A quantidade movimentada e obrigatoria.");
		this.quantidadeAnterior = Objects.requireNonNull(quantidadeAnterior, "A quantidade anterior e obrigatoria.");
		this.quantidadePosterior = Objects.requireNonNull(quantidadePosterior, "A quantidade posterior e obrigatoria.");
		this.motivo = sanitizarMotivo(motivo, tipo);
		this.dataMovimentacao = Objects.requireNonNull(dataMovimentacao, "A data da movimentacao e obrigatoria.");
		this.dataCriacao = Objects.requireNonNull(dataCriacao, "A data de criacao e obrigatoria.");
	}

	/**
	 * Verifica se a movimentacao e uma entrada.
	 * @return true se for entrada.
	 */
	public boolean isEntrada() {
		return tipo == TipoMovimentacao.ENTRADA;
	}

	/**
	 * Verifica se a movimentacao e uma saida.
	 * @return true se for saida.
	 */
	public boolean isSaida() {
		return tipo == TipoMovimentacao.SAIDA;
	}

	/**
	 * Verifica se a movimentacao e um ajuste.
	 * @return true se for ajuste.
	 */
	public boolean isAjuste() {
		return tipo == TipoMovimentacao.AJUSTE;
	}

	private String sanitizarMotivo(String motivo, TipoMovimentacao tipo) {
		String sanitizado = motivo != null ? motivo.trim().replaceAll("\\s+", " ") : null;
		if (sanitizado != null && sanitizado.isEmpty()) {
			sanitizado = null;
		}
		if (tipo == TipoMovimentacao.AJUSTE && sanitizado == null) {
			throw new IllegalArgumentException("O motivo e obrigatorio para ajustes de estoque.");
		}
		return sanitizado;
	}

}

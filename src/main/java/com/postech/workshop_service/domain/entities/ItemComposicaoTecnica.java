package com.postech.workshop_service.domain.entities;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Objeto de dominio que representa um item da composicao tecnica da ordem de servico.
 * Itens do tipo SERVICO possuem ciclo de vida proprio (PENDENTE / EM_EXECUCAO /
 * FINALIZADO) com datas individuais de inicio e finalizacao.
 */
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ItemComposicaoTecnica {

	@EqualsAndHashCode.Include
	private final UUID id;

	private final String descricao;

	private final BigDecimal valor;

	private final TipoItemComposicaoTecnica tipo;

	private final UUID idPecaInsumo;

	private final UUID idServico;

	private StatusItemExecucao statusExecucao;

	private LocalDateTime dataInicioExecucao;

	private LocalDateTime dataFinalizacao;

	/**
	 * Cria um item de composicao tecnica com os dados obrigatorios.
	 * @param descricao descricao do item.
	 * @param valor valor monetario do item.
	 * @param tipo classificacao do item.
	 */
	public ItemComposicaoTecnica(String descricao, BigDecimal valor, TipoItemComposicaoTecnica tipo) {
		this(descricao, valor, tipo, null, null);
	}

	/**
	 * Cria um item de composicao tecnica vinculado a uma peca do catalogo.
	 * @param descricao descricao do item.
	 * @param valor valor monetario do item.
	 * @param tipo classificacao do item.
	 * @param idPecaInsumo identificador da peca (obrigatorio para itens do tipo PECA).
	 */
	public ItemComposicaoTecnica(String descricao, BigDecimal valor, TipoItemComposicaoTecnica tipo,
			UUID idPecaInsumo) {
		this(descricao, valor, tipo, idPecaInsumo, null);
	}

	/**
	 * Cria um item de composicao tecnica vinculado a um servico ou peca do catalogo.
	 * Itens do tipo SERVICO iniciam com status PENDENTE.
	 * @param descricao descricao do item.
	 * @param valor valor monetario do item.
	 * @param tipo classificacao do item.
	 * @param idPecaInsumo identificador da peca (obrigatorio para itens do tipo PECA).
	 * @param idServico identificador do servico do catalogo (opcional, usado em itens do
	 * tipo SERVICO).
	 */
	public ItemComposicaoTecnica(String descricao, BigDecimal valor, TipoItemComposicaoTecnica tipo, UUID idPecaInsumo,
			UUID idServico) {
		this.id = UUID.randomUUID();
		this.descricao = validarDescricao(descricao);
		this.valor = validarValor(valor);
		this.tipo = validarTipo(tipo);
		this.idPecaInsumo = validarIdPecaInsumo(this.tipo, idPecaInsumo);
		this.idServico = idServico;
		this.statusExecucao = this.tipo == TipoItemComposicaoTecnica.SERVICO ? StatusItemExecucao.PENDENTE : null;
		this.dataInicioExecucao = null;
		this.dataFinalizacao = null;
	}

	/**
	 * Reconstroi um item de composicao tecnica previamente persistido.
	 * @param id identificador tecnico do item (obrigatorio).
	 * @param descricao descricao do item.
	 * @param valor valor monetario do item.
	 * @param tipo classificacao do item.
	 * @param idPecaInsumo identificador da peca (obrigatorio para itens do tipo PECA).
	 * @param idServico identificador do servico do catalogo (opcional).
	 * @param statusExecucao status atual de execucao (apenas para itens do tipo SERVICO).
	 * @param dataInicioExecucao data de inicio da execucao do item.
	 * @param dataFinalizacao data de finalizacao da execucao do item.
	 */
	@Default
	public ItemComposicaoTecnica(UUID id, String descricao, BigDecimal valor, TipoItemComposicaoTecnica tipo,
			UUID idPecaInsumo, UUID idServico, StatusItemExecucao statusExecucao, LocalDateTime dataInicioExecucao,
			LocalDateTime dataFinalizacao) {
		if (id == null) {
			throw new IllegalArgumentException("O identificador do item de composicao tecnica e obrigatorio.");
		}
		this.id = id;
		this.descricao = validarDescricao(descricao);
		this.valor = validarValor(valor);
		this.tipo = validarTipo(tipo);
		this.idPecaInsumo = validarIdPecaInsumo(this.tipo, idPecaInsumo);
		this.idServico = idServico;
		this.statusExecucao = statusExecucao;
		this.dataInicioExecucao = dataInicioExecucao;
		this.dataFinalizacao = dataFinalizacao;
	}

	/**
	 * Marca o inicio da execucao deste item de servico. Apenas itens do tipo SERVICO em
	 * status PENDENTE podem ter execucao iniciada.
	 */
	public void marcarInicioExecucao() {
		if (!isServico()) {
			throw new IllegalStateException("Apenas itens do tipo SERVICO podem ter execucao iniciada.");
		}
		if (this.statusExecucao != StatusItemExecucao.PENDENTE) {
			throw new IllegalStateException("O item de servico nao esta pendente para iniciar execucao.");
		}
		this.statusExecucao = StatusItemExecucao.EM_EXECUCAO;
		this.dataInicioExecucao = LocalDateTime.now();
	}

	/**
	 * Marca a finalizacao da execucao deste item de servico. Apenas itens em status
	 * EM_EXECUCAO podem ser finalizados.
	 */
	public void marcarFinalizacao() {
		if (this.statusExecucao != StatusItemExecucao.EM_EXECUCAO) {
			throw new IllegalStateException("O item de servico nao esta em execucao para ser finalizado.");
		}
		this.statusExecucao = StatusItemExecucao.FINALIZADO;
		this.dataFinalizacao = LocalDateTime.now();
	}

	/**
	 * Indica se este item e do tipo SERVICO.
	 * @return {@code true} quando o tipo for SERVICO.
	 */
	public boolean isServico() {
		return this.tipo == TipoItemComposicaoTecnica.SERVICO;
	}

	/**
	 * Indica se este item esta com execucao em andamento.
	 * @return {@code true} quando o status for EM_EXECUCAO.
	 */
	public boolean estaEmExecucao() {
		return this.statusExecucao == StatusItemExecucao.EM_EXECUCAO;
	}

	/**
	 * Indica se este item ja teve a execucao finalizada.
	 * @return {@code true} quando o status for FINALIZADO.
	 */
	public boolean estaFinalizado() {
		return this.statusExecucao == StatusItemExecucao.FINALIZADO;
	}

	private String validarDescricao(String descricao) {
		if (descricao == null) {
			throw new IllegalArgumentException("A descricao do item de composicao tecnica e obrigatoria.");
		}

		String descricaoSanitizada = descricao.trim().replaceAll("\\s+", " ");
		if (descricaoSanitizada.isEmpty()) {
			throw new IllegalArgumentException("A descricao do item de composicao tecnica e obrigatoria.");
		}
		return descricaoSanitizada;
	}

	private BigDecimal validarValor(BigDecimal valor) {
		if (valor == null) {
			throw new IllegalArgumentException("O valor do item de composicao tecnica e obrigatorio.");
		}
		return valor;
	}

	private TipoItemComposicaoTecnica validarTipo(TipoItemComposicaoTecnica tipo) {
		if (tipo == null) {
			throw new IllegalArgumentException("O tipo do item de composicao tecnica e obrigatorio.");
		}
		return tipo;
	}

	private UUID validarIdPecaInsumo(TipoItemComposicaoTecnica tipo, UUID idPecaInsumo) {
		if (tipo == TipoItemComposicaoTecnica.PECA && idPecaInsumo == null) {
			throw new IllegalArgumentException("O identificador da peca/insumo e obrigatorio para itens do tipo PECA.");
		}
		return idPecaInsumo;
	}

}

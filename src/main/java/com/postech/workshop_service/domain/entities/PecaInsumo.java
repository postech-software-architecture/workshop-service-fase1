package com.postech.workshop_service.domain.entities;

import com.postech.workshop_service.domain.valueobjects.TipoItem;
import com.postech.workshop_service.domain.valueobjects.UnidadeMedida;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidade raiz que representa uma peca ou insumo utilizado nos servicos da oficina.
 *
 * <p>
 * O controle de quantidade e delegado a entidade Estoque, permitindo multiplas
 * localizacoes por peca. A quantidade total e calculada pela soma de todos os estoques
 * ativos vinculados.
 * </p>
 */
@Getter
public class PecaInsumo extends EntidadeBase {

	private String sku;

	private String nome;

	private BigDecimal valorUnitario;

	private BigDecimal estoqueMinimo;

	private UnidadeMedida unidadeMedida;

	private TipoItem tipoItem;

	private String fornecedor;

	private String codigoBarras;

	private String marca;

	private String categoria;

	private String aplicacao;

	private String observacoes;

	private boolean ativo;

	private int versao;

	/**
	 * Cria uma nova peca com os dados obrigatorios.
	 * @param id identificador tecnico da peca.
	 * @param sku codigo SKU unico.
	 * @param nome nome da peca.
	 * @param valorUnitario valor unitario.
	 * @param estoqueMinimo nivel minimo para alerta.
	 * @param unidadeMedida unidade de medida.
	 */
	public PecaInsumo(UUID id, String sku, String nome, BigDecimal valorUnitario, BigDecimal estoqueMinimo,
			UnidadeMedida unidadeMedida, TipoItem tipoItem) {
		super(id != null ? id : UUID.randomUUID());
		this.ativo = true;
		this.versao = 0;
		aplicarDados(sku, nome, valorUnitario, estoqueMinimo, unidadeMedida, tipoItem, null, null, null, null, null,
				null);
	}

	/**
	 * Reconstroi uma peca previamente persistida.
	 * @param id identificador tecnico da peca.
	 * @param sku codigo SKU.
	 * @param nome nome da peca.
	 * @param valorUnitario valor unitario.
	 * @param estoqueMinimo nivel minimo para alerta.
	 * @param unidadeMedida unidade de medida.
	 * @param fornecedor fornecedor opcional.
	 * @param codigoBarras codigo de barras opcional.
	 * @param marca marca opcional.
	 * @param categoria categoria opcional.
	 * @param aplicacao aplicacao opcional.
	 * @param observacoes observacoes opcionais.
	 * @param ativo indicador de ativo.
	 * @param versao versao para optimistic locking.
	 * @param dataCriacao data de criacao.
	 * @param dataUltimaAtualizacao data da ultima atualizacao.
	 * @param dataRemocao data da remocao logica.
	 */
	@Default
	public PecaInsumo(UUID id, String sku, String nome, BigDecimal valorUnitario, BigDecimal estoqueMinimo,
			UnidadeMedida unidadeMedida, TipoItem tipoItem, String fornecedor, String codigoBarras, String marca,
			String categoria, String aplicacao, String observacoes, boolean ativo, int versao,
			LocalDateTime dataCriacao, LocalDateTime dataUltimaAtualizacao, LocalDateTime dataRemocao) {
		super(id, dataCriacao, dataUltimaAtualizacao, dataRemocao);
		this.ativo = ativo;
		this.versao = versao;
		aplicarDados(sku, nome, valorUnitario, estoqueMinimo, unidadeMedida, tipoItem, fornecedor, codigoBarras, marca,
				categoria, aplicacao, observacoes);
	}

	/**
	 * Atualiza os dados cadastrais da peca.
	 * @param nome novo nome.
	 * @param valorUnitario novo valor unitario.
	 * @param estoqueMinimo novo estoque minimo.
	 * @param unidadeMedida nova unidade de medida.
	 * @param fornecedor novo fornecedor.
	 * @param codigoBarras novo codigo de barras.
	 * @param marca nova marca.
	 * @param categoria nova categoria.
	 * @param aplicacao nova aplicacao.
	 * @param observacoes novas observacoes.
	 */
	public void atualizarDados(String nome, BigDecimal valorUnitario, BigDecimal estoqueMinimo,
			UnidadeMedida unidadeMedida, TipoItem tipoItem, String fornecedor, String codigoBarras, String marca,
			String categoria, String aplicacao, String observacoes) {
		this.nome = sanitizarObrigatorio(nome, "O nome da peca e obrigatorio.");
		this.valorUnitario = validarValorUnitario(valorUnitario);
		this.estoqueMinimo = validarEstoqueMinimo(estoqueMinimo);
		this.unidadeMedida = Objects.requireNonNull(unidadeMedida, "A unidade de medida e obrigatoria.");
		this.tipoItem = Objects.requireNonNull(tipoItem, "O tipo do item e obrigatorio.");
		this.fornecedor = sanitizarOpcional(fornecedor);
		this.codigoBarras = sanitizarOpcional(codigoBarras);
		this.marca = sanitizarOpcional(marca);
		this.categoria = sanitizarOpcional(categoria);
		this.aplicacao = sanitizarOpcional(aplicacao);
		this.observacoes = sanitizarOpcional(observacoes);
		atualizarDataUltimaAtualizacao();
	}

	/**
	 * Executa a remocao logica da peca.
	 */
	public void removerLogicamente() {
		if (!this.ativo) {
			return;
		}
		this.ativo = false;
		registrarRemocaoLogica();
	}

	private void aplicarDados(String sku, String nome, BigDecimal valorUnitario, BigDecimal estoqueMinimo,
			UnidadeMedida unidadeMedida, TipoItem tipoItem, String fornecedor, String codigoBarras, String marca,
			String categoria, String aplicacao, String observacoes) {
		this.sku = sanitizarObrigatorio(sku, "O SKU da peca e obrigatorio.");
		this.nome = sanitizarObrigatorio(nome, "O nome da peca e obrigatorio.");
		this.valorUnitario = validarValorUnitario(valorUnitario);
		this.estoqueMinimo = validarEstoqueMinimo(estoqueMinimo);
		this.unidadeMedida = Objects.requireNonNull(unidadeMedida, "A unidade de medida e obrigatoria.");
		this.tipoItem = Objects.requireNonNull(tipoItem, "O tipo do item e obrigatorio.");
		this.fornecedor = sanitizarOpcional(fornecedor);
		this.codigoBarras = sanitizarOpcional(codigoBarras);
		this.marca = sanitizarOpcional(marca);
		this.categoria = sanitizarOpcional(categoria);
		this.aplicacao = sanitizarOpcional(aplicacao);
		this.observacoes = sanitizarOpcional(observacoes);
	}

	private BigDecimal validarValorUnitario(BigDecimal valor) {
		if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalArgumentException("O valor unitario deve ser positivo e maior que zero.");
		}
		return valor;
	}

	private BigDecimal validarEstoqueMinimo(BigDecimal estoqueMinimo) {
		if (estoqueMinimo == null || estoqueMinimo.compareTo(BigDecimal.ZERO) < 0) {
			throw new IllegalArgumentException("O estoque minimo nao pode ser negativo.");
		}
		return estoqueMinimo;
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

}

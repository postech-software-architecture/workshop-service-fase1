package com.postech.workshop_service.domain.entities;

import com.postech.workshop_service.domain.enums.CategoriaServico;
import com.postech.workshop_service.domain.enums.NivelComplexidade;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidade raiz que representa um servico do catalogo da oficina.
 */
@Getter
public class Servico extends EntidadeBase {

	private String nome;

	private String descricao;

	private BigDecimal valor;

	private int tempoEstimadoMinutos;

	private CategoriaServico categoria;

	private NivelComplexidade nivelComplexidade;

	private Integer garantiaDias;

	private String observacoesTecnicas;

	private boolean ativo;

	/**
	 * Cria um novo servico com os dados operacionais obrigatorios.
	 * @param id identificador tecnico do servico; quando nulo, um UUID e gerado
	 * automaticamente.
	 * @param nome nome do servico.
	 * @param descricao descricao do servico.
	 * @param valor valor cobrado pelo servico.
	 * @param tempoEstimadoMinutos tempo estimado de execucao em minutos.
	 * @param categoria categoria do servico (nullable).
	 * @param nivelComplexidade nivel de complexidade do servico (nullable).
	 * @param garantiaDias quantidade de dias de garantia (nullable).
	 * @param observacoesTecnicas observacoes tecnicas opcionais.
	 */
	public Servico(UUID id, String nome, String descricao, BigDecimal valor, int tempoEstimadoMinutos,
			CategoriaServico categoria, NivelComplexidade nivelComplexidade, Integer garantiaDias,
			String observacoesTecnicas) {
		super(id != null ? id : UUID.randomUUID());
		this.ativo = true;
		aplicarDados(nome, descricao, valor, tempoEstimadoMinutos, categoria, nivelComplexidade, garantiaDias,
				observacoesTecnicas);
	}

	/**
	 * Reconstroi um servico previamente persistido.
	 * @param id identificador tecnico do servico.
	 * @param nome nome do servico.
	 * @param descricao descricao do servico.
	 * @param valor valor cobrado pelo servico.
	 * @param tempoEstimadoMinutos tempo estimado de execucao em minutos.
	 * @param categoria categoria do servico (nullable).
	 * @param nivelComplexidade nivel de complexidade do servico (nullable).
	 * @param garantiaDias quantidade de dias de garantia (nullable).
	 * @param observacoesTecnicas observacoes tecnicas opcionais.
	 * @param ativo indicador operacional do servico.
	 * @param dataCriacao data de criacao.
	 * @param dataUltimaAtualizacao data da ultima atualizacao.
	 * @param dataRemocao data da remocao logica.
	 */
	public Servico(UUID id, String nome, String descricao, BigDecimal valor, int tempoEstimadoMinutos,
			CategoriaServico categoria, NivelComplexidade nivelComplexidade, Integer garantiaDias,
			String observacoesTecnicas, boolean ativo, LocalDateTime dataCriacao, LocalDateTime dataUltimaAtualizacao,
			LocalDateTime dataRemocao) {
		super(id, dataCriacao, dataUltimaAtualizacao, dataRemocao);
		this.ativo = ativo;
		aplicarDados(nome, descricao, valor, tempoEstimadoMinutos, categoria, nivelComplexidade, garantiaDias,
				observacoesTecnicas);
	}

	/**
	 * Atualiza os dados cadastrais do servico.
	 * @param nome novo nome.
	 * @param descricao nova descricao.
	 * @param valor novo valor.
	 * @param tempoEstimadoMinutos novo tempo estimado em minutos.
	 * @param categoria nova categoria (nullable).
	 * @param nivelComplexidade novo nivel de complexidade (nullable).
	 * @param garantiaDias nova quantidade de dias de garantia (nullable).
	 * @param observacoesTecnicas novas observacoes tecnicas opcionais.
	 */
	public void atualizarDados(String nome, String descricao, BigDecimal valor, int tempoEstimadoMinutos,
			CategoriaServico categoria, NivelComplexidade nivelComplexidade, Integer garantiaDias,
			String observacoesTecnicas) {
		aplicarDados(nome, descricao, valor, tempoEstimadoMinutos, categoria, nivelComplexidade, garantiaDias,
				observacoesTecnicas);
		atualizarDataUltimaAtualizacao();
	}

	/**
	 * Executa a remocao logica do servico. A operacao e idempotente: chamadas sucessivas
	 * nao produzem efeito adicional.
	 */
	public void removerLogicamente() {
		if (!this.ativo) {
			return;
		}
		this.ativo = false;
		registrarRemocaoLogica();
	}

	/**
	 * Reativa logicamente o servico, restaurando-o ao catalogo ativo. A operacao e
	 * idempotente: chamadas sucessivas sobre um servico ja ativo nao produzem efeito.
	 */
	public void reativar() {
		if (this.ativo) {
			return;
		}
		this.ativo = true;
		reverterRemocaoLogica();
	}

	private void aplicarDados(String nome, String descricao, BigDecimal valor, int tempoEstimadoMinutos,
			CategoriaServico categoria, NivelComplexidade nivelComplexidade, Integer garantiaDias,
			String observacoesTecnicas) {
		this.nome = sanitizarObrigatorio(nome, "O nome do servico e obrigatorio.");
		this.descricao = sanitizarObrigatorio(descricao, "A descricao do servico e obrigatoria.");
		if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalArgumentException("O valor do servico deve ser maior que zero.");
		}
		if (tempoEstimadoMinutos <= 0) {
			throw new IllegalArgumentException("O tempo estimado do servico deve ser maior que zero.");
		}
		if (garantiaDias != null && garantiaDias <= 0) {
			throw new IllegalArgumentException("A garantia em dias deve ser maior que zero quando informada.");
		}
		this.valor = valor;
		this.tempoEstimadoMinutos = tempoEstimadoMinutos;
		this.categoria = categoria;
		this.nivelComplexidade = nivelComplexidade;
		this.garantiaDias = garantiaDias;
		this.observacoesTecnicas = sanitizarOpcional(observacoesTecnicas);
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

package com.postech.workshop_service.domain.entities;

import com.postech.workshop_service.domain.enums.CategoriaServico;
import com.postech.workshop_service.domain.enums.NivelComplexidade;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidade raiz que representa um serviço do catálogo da oficina.
 */
@Getter
public class Servico extends EntidadeBase {

	private String nome;

	private String descricao;

	private BigDecimal valor;

	private CategoriaServico categoria;

	private NivelComplexidade nivelComplexidade;

	private Integer garantiaDias;

	private String observacoesTecnicas;

	private boolean ativo;

	/**
	 * Cria um novo serviço com os dados operacionais obrigatórios.
	 * @param id identificador técnico do serviço; quando nulo, um UUID é gerado
	 * automaticamente.
	 * @param nome nome do serviço.
	 * @param descricao descrição do serviço.
	 * @param valor valor cobrado pelo serviço.
	 * @param categoria categoria do serviço (nullable).
	 * @param nivelComplexidade nível de complexidade do serviço (nullable).
	 * @param garantiaDias quantidade de dias de garantia (nullable).
	 * @param observacoesTecnicas observações técnicas opcionais.
	 */
	public Servico(UUID id, String nome, String descricao, BigDecimal valor, CategoriaServico categoria,
			NivelComplexidade nivelComplexidade, Integer garantiaDias, String observacoesTecnicas) {
		super(id != null ? id : UUID.randomUUID());
		this.ativo = true;
		aplicarDados(nome, descricao, valor, categoria, nivelComplexidade, garantiaDias, observacoesTecnicas);
	}

	/**
	 * Reconstrói um serviço previamente persistido.
	 * @param id identificador técnico do serviço.
	 * @param nome nome do serviço.
	 * @param descricao descrição do serviço.
	 * @param valor valor cobrado pelo serviço.
	 * @param categoria categoria do serviço (nullable).
	 * @param nivelComplexidade nível de complexidade do serviço (nullable).
	 * @param garantiaDias quantidade de dias de garantia (nullable).
	 * @param observacoesTecnicas observações técnicas opcionais.
	 * @param ativo indicador operacional do serviço.
	 * @param dataCriacao data de criação.
	 * @param dataUltimaAtualizacao data da última atualização.
	 * @param dataRemocao data da remoção lógica.
	 */
	public Servico(UUID id, String nome, String descricao, BigDecimal valor, CategoriaServico categoria,
			NivelComplexidade nivelComplexidade, Integer garantiaDias, String observacoesTecnicas, boolean ativo,
			LocalDateTime dataCriacao, LocalDateTime dataUltimaAtualizacao, LocalDateTime dataRemocao) {
		super(id, dataCriacao, dataUltimaAtualizacao, dataRemocao);
		this.ativo = ativo;
		aplicarDados(nome, descricao, valor, categoria, nivelComplexidade, garantiaDias, observacoesTecnicas);
	}

	/**
	 * Atualiza os dados cadastrais do serviço.
	 * @param nome novo nome.
	 * @param descricao nova descrição.
	 * @param valor novo valor.
	 * @param categoria nova categoria (nullable).
	 * @param nivelComplexidade novo nível de complexidade (nullable).
	 * @param garantiaDias nova quantidade de dias de garantia (nullable).
	 * @param observacoesTecnicas novas observações técnicas opcionais.
	 */
	public void atualizarDados(String nome, String descricao, BigDecimal valor, CategoriaServico categoria,
			NivelComplexidade nivelComplexidade, Integer garantiaDias, String observacoesTecnicas) {
		aplicarDados(nome, descricao, valor, categoria, nivelComplexidade, garantiaDias, observacoesTecnicas);
		atualizarDataUltimaAtualizacao();
	}

	/**
	 * Executa a remoção lógica do serviço. A operação é idempotente: chamadas sucessivas
	 * não produzem efeito adicional.
	 */
	public void removerLogicamente() {
		if (!this.ativo) {
			return;
		}
		this.ativo = false;
		registrarRemocaoLogica();
	}

	/**
	 * Reativa logicamente o serviço, restaurando-o ao catálogo ativo. A operação é
	 * idempotente: chamadas sucessivas sobre um serviço já ativo não produzem efeito.
	 */
	public void reativar() {
		if (this.ativo) {
			return;
		}
		this.ativo = true;
		reverterRemocaoLogica();
	}

	private void aplicarDados(String nome, String descricao, BigDecimal valor, CategoriaServico categoria,
			NivelComplexidade nivelComplexidade, Integer garantiaDias, String observacoesTecnicas) {
		this.nome = sanitizarObrigatorio(nome, "O nome do serviço é obrigatório.");
		this.descricao = sanitizarObrigatorio(descricao, "A descrição do serviço é obrigatória.");
		if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalArgumentException("O valor do serviço deve ser maior que zero.");
		}
		if (garantiaDias != null && garantiaDias <= 0) {
			throw new IllegalArgumentException("A garantia em dias deve ser maior que zero quando informada.");
		}
		this.valor = valor;
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

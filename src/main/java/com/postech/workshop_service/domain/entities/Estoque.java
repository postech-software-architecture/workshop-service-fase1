package com.postech.workshop_service.domain.entities;

import com.postech.workshop_service.domain.valueobjects.TipoMovimentacao;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidade que representa um estoque de uma peca em uma localizacao especifica.
 *
 * <p>
 * Permite que uma mesma peca tenha multiplas localizacoes de estoque, cada uma com sua
 * quantidade independente. A quantidade total de uma peca e a soma de todos os seus
 * estoques ativos.
 * </p>
 */
@Getter
public class Estoque {

	private final UUID id;

	private final UUID pecaInsumoId;

	private String localizacao;

	private BigDecimal quantidade;

	private boolean ativo;

	private int versao;

	private final LocalDateTime dataCriacao;

	private LocalDateTime dataUltimaAtualizacao;

	/**
	 * Cria um novo estoque para uma peca.
	 * @param id identificador tecnico do estoque.
	 * @param pecaInsumoId identificador da peca.
	 * @param localizacao localizacao fisica.
	 * @param quantidadeInicial quantidade inicial.
	 */
	public Estoque(UUID id, UUID pecaInsumoId, String localizacao, BigDecimal quantidadeInicial) {
		this.id = id != null ? id : UUID.randomUUID();
		this.dataCriacao = LocalDateTime.now();
		this.dataUltimaAtualizacao = this.dataCriacao;
		this.ativo = true;
		this.versao = 0;
		this.pecaInsumoId = Objects.requireNonNull(pecaInsumoId, "O identificador da peca e obrigatorio.");
		this.localizacao = sanitizarObrigatorio(localizacao, "A localizacao do estoque e obrigatoria.");
		this.quantidade = validarQuantidade(quantidadeInicial);
	}

	/**
	 * Reconstroi um estoque previamente persistido.
	 * @param id identificador tecnico do estoque.
	 * @param pecaInsumoId identificador da peca.
	 * @param localizacao localizacao fisica.
	 * @param quantidade quantidade atual.
	 * @param ativo indicador de ativo.
	 * @param versao versao para optimistic locking.
	 * @param dataCriacao data de criacao.
	 * @param dataUltimaAtualizacao data da ultima atualizacao.
	 */
	@Default
	public Estoque(UUID id, UUID pecaInsumoId, String localizacao, BigDecimal quantidade, boolean ativo, int versao,
			LocalDateTime dataCriacao, LocalDateTime dataUltimaAtualizacao) {
		this.id = Objects.requireNonNull(id, "O identificador do estoque e obrigatorio.");
		this.pecaInsumoId = Objects.requireNonNull(pecaInsumoId, "O identificador da peca e obrigatorio.");
		this.localizacao = sanitizarObrigatorio(localizacao, "A localizacao do estoque e obrigatoria.");
		this.quantidade = validarQuantidade(quantidade);
		this.ativo = ativo;
		this.versao = versao;
		this.dataCriacao = Objects.requireNonNull(dataCriacao, "A data de criacao e obrigatoria.");
		this.dataUltimaAtualizacao = Objects.requireNonNull(dataUltimaAtualizacao,
				"A data de ultima atualizacao e obrigatoria.");
	}

	/**
	 * Registra uma entrada de estoque.
	 * @param quantidadeEntrada quantidade a ser adicionada.
	 * @param motivo motivo da entrada.
	 * @return movimentacao registrada.
	 */
	public MovimentacaoEstoque registrarEntrada(BigDecimal quantidadeEntrada, String motivo) {
		validarQuantidadePositiva(quantidadeEntrada, "A quantidade de entrada deve ser positiva.");
		return criarMovimentacao(TipoMovimentacao.ENTRADA, quantidadeEntrada, motivo);
	}

	/**
	 * Registra uma saida de estoque.
	 * @param quantidadeSaida quantidade a ser removida.
	 * @param motivo motivo da saida.
	 * @return movimentacao registrada.
	 * @throws IllegalArgumentException se quantidade insuficiente.
	 */
	public MovimentacaoEstoque registrarSaida(BigDecimal quantidadeSaida, String motivo) {
		validarQuantidadePositiva(quantidadeSaida, "A quantidade de saida deve ser positiva.");
		if (quantidadeSaida.compareTo(this.quantidade) > 0) {
			throw new IllegalArgumentException("Quantidade insuficiente em estoque para esta saida.");
		}
		return criarMovimentacao(TipoMovimentacao.SAIDA, quantidadeSaida, motivo);
	}

	/**
	 * Reserva uma quantidade do estoque para uma OS pendente de aprovacao de orcamento. A
	 * quantidade reservada e subtraida do saldo disponivel imediatamente.
	 * @param quantidadeReserva quantidade a ser reservada.
	 * @param motivo identificacao da OS ou orcamento que originou a reserva.
	 * @return movimentacao registrada.
	 * @throws IllegalArgumentException se quantidade insuficiente.
	 */
	public MovimentacaoEstoque reservar(BigDecimal quantidadeReserva, String motivo) {
		validarQuantidadePositiva(quantidadeReserva, "A quantidade de reserva deve ser positiva.");
		if (quantidadeReserva.compareTo(this.quantidade) > 0) {
			throw new IllegalArgumentException("Quantidade insuficiente em estoque para esta reserva.");
		}
		return criarMovimentacao(TipoMovimentacao.RESERVA, quantidadeReserva, motivo);
	}

	/**
	 * Libera uma quantidade previamente reservada, devolvendo-a ao saldo disponivel.
	 * Chamado quando o orcamento e rejeitado ou cancelado.
	 * @param quantidadeLiberacao quantidade a ser devolvida ao estoque.
	 * @param motivo identificacao do orcamento que originou a liberacao.
	 * @return movimentacao registrada.
	 */
	public MovimentacaoEstoque liberarReserva(BigDecimal quantidadeLiberacao, String motivo) {
		validarQuantidadePositiva(quantidadeLiberacao, "A quantidade de liberacao deve ser positiva.");
		return criarMovimentacao(TipoMovimentacao.LIBERACAO, quantidadeLiberacao, motivo);
	}

	/**
	 * Registra um ajuste de estoque.
	 * @param novaQuantidade nova quantidade absoluta.
	 * @param motivo motivo do ajuste (obrigatorio).
	 * @return movimentacao registrada.
	 * @throws IllegalArgumentException se motivo nao informado.
	 */
	public MovimentacaoEstoque ajustar(BigDecimal novaQuantidade, String motivo) {
		if (motivo == null || motivo.trim().isEmpty()) {
			throw new IllegalArgumentException("O motivo e obrigatorio para ajustes de estoque.");
		}
		return criarMovimentacao(TipoMovimentacao.AJUSTE, novaQuantidade, motivo);
	}

	/**
	 * Executa a remocao logica do estoque.
	 */
	public void removerLogicamente() {
		if (!this.ativo) {
			return;
		}
		this.ativo = false;
		this.dataUltimaAtualizacao = LocalDateTime.now();
	}

	/**
	 * Atualiza a localizacao do estoque.
	 * @param novaLocalizacao nova localizacao.
	 */
	public void atualizarLocalizacao(String novaLocalizacao) {
		this.localizacao = sanitizarObrigatorio(novaLocalizacao, "A localizacao do estoque e obrigatoria.");
		this.dataUltimaAtualizacao = LocalDateTime.now();
	}

	private MovimentacaoEstoque criarMovimentacao(TipoMovimentacao tipo, BigDecimal quantidadeMovimentada,
			String motivo) {
		BigDecimal quantidadeAnterior = this.quantidade;

		switch (tipo) {
			case ENTRADA:
			case LIBERACAO:
				this.quantidade = this.quantidade.add(quantidadeMovimentada);
				break;
			case SAIDA:
			case RESERVA:
				this.quantidade = this.quantidade.subtract(quantidadeMovimentada);
				break;
			case AJUSTE:
				this.quantidade = validarQuantidade(quantidadeMovimentada);
				break;
		}

		this.dataUltimaAtualizacao = LocalDateTime.now();

		return new MovimentacaoEstoque(null, this.id, tipo, quantidadeMovimentada, quantidadeAnterior, this.quantidade,
				motivo);
	}

	private BigDecimal validarQuantidade(BigDecimal quantidade) {
		if (quantidade == null || quantidade.compareTo(BigDecimal.ZERO) < 0) {
			throw new IllegalArgumentException("A quantidade nao pode ser negativa.");
		}
		return quantidade;
	}

	private void validarQuantidadePositiva(BigDecimal quantidade, String mensagem) {
		if (quantidade == null || quantidade.compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalArgumentException(mensagem);
		}
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

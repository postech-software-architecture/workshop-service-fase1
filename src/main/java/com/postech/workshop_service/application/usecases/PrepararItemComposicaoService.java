package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.domain.entities.Estoque;
import com.postech.workshop_service.domain.entities.ItemComposicaoTecnica;
import com.postech.workshop_service.domain.entities.MovimentacaoEstoque;
import com.postech.workshop_service.domain.entities.OrdemServico;
import com.postech.workshop_service.domain.entities.PecaInsumo;
import com.postech.workshop_service.domain.entities.Servico;
import com.postech.workshop_service.domain.entities.TipoItemComposicaoTecnica;
import com.postech.workshop_service.domain.exceptions.RecursoNaoEncontradoException;
import com.postech.workshop_service.domain.exceptions.RegraDeNegocioException;
import com.postech.workshop_service.domain.repositories.EstoqueRepository;
import com.postech.workshop_service.domain.repositories.MovimentacaoEstoqueRepository;
import com.postech.workshop_service.domain.repositories.PecaInsumoRepository;
import com.postech.workshop_service.domain.repositories.ServicoRepository;
import com.postech.workshop_service.domain.valueobjects.TipoItem;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/** Centraliza a preparacao de itens de catalogo e a reserva de estoque para uma OS. */
@Service
public class PrepararItemComposicaoService {

	private final ServicoRepository servicoRepository;

	private final PecaInsumoRepository pecaInsumoRepository;

	private final EstoqueRepository estoqueRepository;

	private final MovimentacaoEstoqueRepository movimentacaoEstoqueRepository;

	public PrepararItemComposicaoService(ServicoRepository servicoRepository, PecaInsumoRepository pecaInsumoRepository,
			EstoqueRepository estoqueRepository, MovimentacaoEstoqueRepository movimentacaoEstoqueRepository) {
		this.servicoRepository = servicoRepository;
		this.pecaInsumoRepository = pecaInsumoRepository;
		this.estoqueRepository = estoqueRepository;
		this.movimentacaoEstoqueRepository = movimentacaoEstoqueRepository;
	}

	public ItemComposicaoTecnica prepararServico(UUID idServico, BigDecimal quantidade) {
		if (idServico == null) {
			throw new RegraDeNegocioException("O identificador do servico e obrigatorio.");
		}
		if (quantidade == null || quantidade.compareTo(BigDecimal.ZERO) <= 0) {
			throw new RegraDeNegocioException("A quantidade do servico deve ser maior que zero.");
		}
		Servico servico = servicoRepository.buscarPorId(idServico, false)
			.orElseThrow(() -> new RecursoNaoEncontradoException("Servico nao encontrado: " + idServico + "."));
		if (!servico.isAtivo()) {
			throw new RegraDeNegocioException("O servico '" + servico.getNome() + "' esta inativo.");
		}
		return new ItemComposicaoTecnica(servico.getNome(), servico.getValor().multiply(quantidade),
				TipoItemComposicaoTecnica.SERVICO, null, servico.getId());
	}

	public PecaPreparada prepararPeca(UUID idPeca, BigDecimal quantidade) {
		if (idPeca == null) {
			throw new RegraDeNegocioException("O identificador da peca e obrigatorio.");
		}
		if (quantidade == null || quantidade.compareTo(BigDecimal.ZERO) <= 0) {
			throw new RegraDeNegocioException("A quantidade da peca deve ser maior que zero.");
		}
		PecaInsumo peca = pecaInsumoRepository.buscarPorId(idPeca, false)
			.orElseThrow(() -> new RecursoNaoEncontradoException("Peca nao encontrada: " + idPeca + "."));
		if (!peca.isAtivo()) {
			throw new RegraDeNegocioException("A peca '" + peca.getNome() + "' esta inativa.");
		}
		BigDecimal disponivel = estoqueRepository.calcularQuantidadeTotal(peca.getId());
		if (disponivel.compareTo(quantidade) < 0) {
			throw new RegraDeNegocioException("Estoque insuficiente para '" + peca.getNome() + "'. Disponivel: "
					+ disponivel + ", solicitado: " + quantidade + ".");
		}
		TipoItemComposicaoTecnica tipo = peca.getTipoItem() == TipoItem.INSUMO ? TipoItemComposicaoTecnica.INSUMO
				: TipoItemComposicaoTecnica.PECA;
		ItemComposicaoTecnica item = new ItemComposicaoTecnica(peca.getNome(),
				peca.getValorUnitario().multiply(quantidade), tipo, peca.getId());
		return new PecaPreparada(peca, quantidade, item);
	}

	public void reservarPeca(PecaPreparada preparada, OrdemServico ordem) {
		String motivo = "Reserva para OS " + ordem.getNumero();
		BigDecimal quantidadeRestante = preparada.quantidade();
		List<Estoque> estoques = estoqueRepository
			.listarPorPecaOrdenadoPorQuantidadeDisponivel(preparada.peca().getId(), false)
			.stream()
			.filter(e -> e.getQuantidade().compareTo(BigDecimal.ZERO) > 0)
			.toList();
		for (Estoque estoque : estoques) {
			if (quantidadeRestante.compareTo(BigDecimal.ZERO) <= 0) {
				break;
			}
			BigDecimal quantidadeReserva = quantidadeRestante.min(estoque.getQuantidade());
			MovimentacaoEstoque movimentacao = estoque.reservar(quantidadeReserva, motivo, ordem.getId(), null);
			estoqueRepository.salvar(estoque);
			movimentacaoEstoqueRepository.salvar(movimentacao);
			quantidadeRestante = quantidadeRestante.subtract(quantidadeReserva);
		}
		if (quantidadeRestante.compareTo(BigDecimal.ZERO) > 0) {
			throw new RegraDeNegocioException("Estoque insuficiente para reservar a quantidade solicitada de '"
					+ preparada.peca().getNome() + "'.");
		}
	}

	public record PecaPreparada(PecaInsumo peca, BigDecimal quantidade, ItemComposicaoTecnica item) {
	}

}

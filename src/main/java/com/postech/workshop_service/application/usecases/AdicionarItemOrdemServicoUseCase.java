package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.domain.exceptions.RecursoNaoEncontradoException;
import com.postech.workshop_service.domain.exceptions.RegraDeNegocioException;
import com.postech.workshop_service.domain.entities.Estoque;
import com.postech.workshop_service.domain.entities.ItemComposicaoTecnica;
import com.postech.workshop_service.domain.entities.MovimentacaoEstoque;
import com.postech.workshop_service.domain.entities.OrdemServico;
import com.postech.workshop_service.domain.entities.PecaInsumo;
import com.postech.workshop_service.domain.entities.Servico;
import com.postech.workshop_service.domain.entities.TipoItemComposicaoTecnica;
import com.postech.workshop_service.domain.repositories.EstoqueRepository;
import com.postech.workshop_service.domain.repositories.MovimentacaoEstoqueRepository;
import com.postech.workshop_service.domain.repositories.OrdemServicoRepository;
import com.postech.workshop_service.domain.repositories.PecaInsumoRepository;
import com.postech.workshop_service.domain.repositories.ServicoRepository;
import com.postech.workshop_service.domain.valueobjects.TipoItem;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Caso de uso responsavel por adicionar um item (servico ou peca) a composicao tecnica de
 * uma ordem de servico em EM_COMPOSICAO.
 */
@Service
public class AdicionarItemOrdemServicoUseCase {

	private final OrdemServicoRepository ordemServicoRepository;

	private final ServicoRepository servicoRepository;

	private final PecaInsumoRepository pecaInsumoRepository;

	private final EstoqueRepository estoqueRepository;

	private final MovimentacaoEstoqueRepository movimentacaoEstoqueRepository;

	public AdicionarItemOrdemServicoUseCase(OrdemServicoRepository ordemServicoRepository,
			ServicoRepository servicoRepository, PecaInsumoRepository pecaInsumoRepository,
			EstoqueRepository estoqueRepository, MovimentacaoEstoqueRepository movimentacaoEstoqueRepository) {
		this.ordemServicoRepository = ordemServicoRepository;
		this.servicoRepository = servicoRepository;
		this.pecaInsumoRepository = pecaInsumoRepository;
		this.estoqueRepository = estoqueRepository;
		this.movimentacaoEstoqueRepository = movimentacaoEstoqueRepository;
	}

	@Transactional
	public OrdemServico executarServico(UUID idOrdemServico, UUID idServico, BigDecimal quantidade) {
		OrdemServico ordemServico = ordemServicoRepository.buscarPorId(idOrdemServico)
			.orElseThrow(() -> new RecursoNaoEncontradoException("Ordem de servico nao encontrada."));
		Servico servico = servicoRepository.buscarPorId(idServico, false)
			.orElseThrow(() -> new RecursoNaoEncontradoException("Servico nao encontrado: " + idServico + "."));
		if (!servico.isAtivo()) {
			throw new RegraDeNegocioException("O servico '" + servico.getNome() + "' esta inativo.");
		}
		BigDecimal qtd = quantidade != null ? quantidade : BigDecimal.ONE;
		BigDecimal valorTotal = servico.getValor().multiply(qtd);
		ItemComposicaoTecnica item = new ItemComposicaoTecnica(servico.getNome(), valorTotal,
				TipoItemComposicaoTecnica.SERVICO, null, servico.getId());
		ordemServico.adicionarItem(item);
		return ordemServicoRepository.salvar(ordemServico);
	}

	@Transactional
	public OrdemServico executarPeca(UUID idOrdemServico, UUID idPeca, BigDecimal quantidade) {
		OrdemServico ordemServico = ordemServicoRepository.buscarPorId(idOrdemServico)
			.orElseThrow(() -> new RecursoNaoEncontradoException("Ordem de servico nao encontrada."));
		if (quantidade == null || quantidade.compareTo(BigDecimal.ZERO) <= 0) {
			throw new RegraDeNegocioException("A quantidade da peca deve ser maior que zero.");
		}
		PecaInsumo peca = pecaInsumoRepository.buscarPorId(idPeca, false)
			.orElseThrow(() -> new RecursoNaoEncontradoException("Peca nao encontrada: " + idPeca + "."));
		if (!peca.isAtivo()) {
			throw new RegraDeNegocioException("A peca '" + peca.getNome() + "' esta inativa.");
		}
		BigDecimal valorTotal = peca.getValorUnitario().multiply(quantidade);
		TipoItemComposicaoTecnica tipoItem = peca.getTipoItem() == TipoItem.INSUMO ? TipoItemComposicaoTecnica.INSUMO
				: TipoItemComposicaoTecnica.PECA;
		ItemComposicaoTecnica item = new ItemComposicaoTecnica(peca.getNome(), valorTotal, tipoItem, peca.getId());
		ordemServico.adicionarItem(item);
		OrdemServico ordemSalva = ordemServicoRepository.salvar(ordemServico);
		reservarEstoque(peca, quantidade, ordemSalva);
		return ordemSalva;
	}

	private void reservarEstoque(PecaInsumo peca, BigDecimal quantidade, OrdemServico ordem) {
		String motivo = "Reserva para OS " + ordem.getNumero();
		BigDecimal quantidadeRestante = quantidade;
		List<Estoque> estoques = estoqueRepository.listarPorPecaOrdenadoPorQuantidadeDisponivel(peca.getId(), false)
			.stream()
			.filter(e -> e.getQuantidade().compareTo(BigDecimal.ZERO) > 0)
			.toList();
		for (Estoque estoque : estoques) {
			if (quantidadeRestante.compareTo(BigDecimal.ZERO) <= 0) {
				break;
			}
			BigDecimal quantidadeReserva = quantidadeRestante.min(estoque.getQuantidade());
			MovimentacaoEstoque mov = estoque.reservar(quantidadeReserva, motivo, ordem.getId(), null);
			estoqueRepository.salvar(estoque);
			movimentacaoEstoqueRepository.salvar(mov);
			quantidadeRestante = quantidadeRestante.subtract(quantidadeReserva);
		}
		if (quantidadeRestante.compareTo(BigDecimal.ZERO) > 0) {
			throw new RegraDeNegocioException(
					"Estoque insuficiente para reservar a quantidade solicitada de '" + peca.getNome() + "'.");
		}
	}

}

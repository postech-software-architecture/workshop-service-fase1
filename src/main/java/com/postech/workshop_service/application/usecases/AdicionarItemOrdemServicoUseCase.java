package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.application.exceptions.RecursoNaoEncontradoException;
import com.postech.workshop_service.application.exceptions.RegraDeNegocioException;
import com.postech.workshop_service.domain.entities.ItemComposicaoTecnica;
import com.postech.workshop_service.domain.entities.OrdemServico;
import com.postech.workshop_service.domain.entities.PecaInsumo;
import com.postech.workshop_service.domain.entities.Servico;
import com.postech.workshop_service.domain.entities.TipoItemComposicaoTecnica;
import com.postech.workshop_service.domain.repositories.OrdemServicoRepository;
import com.postech.workshop_service.domain.repositories.PecaInsumoRepository;
import com.postech.workshop_service.domain.repositories.ServicoRepository;
import com.postech.workshop_service.domain.valueobjects.TipoItem;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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

	public AdicionarItemOrdemServicoUseCase(OrdemServicoRepository ordemServicoRepository,
			ServicoRepository servicoRepository, PecaInsumoRepository pecaInsumoRepository) {
		this.ordemServicoRepository = ordemServicoRepository;
		this.servicoRepository = servicoRepository;
		this.pecaInsumoRepository = pecaInsumoRepository;
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
		return ordemServicoRepository.salvar(ordemServico);
	}

}

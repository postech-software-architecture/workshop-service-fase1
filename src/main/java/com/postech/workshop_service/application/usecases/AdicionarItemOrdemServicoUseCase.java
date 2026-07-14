package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.domain.exceptions.RecursoNaoEncontradoException;
import com.postech.workshop_service.domain.exceptions.RegraDeNegocioException;
import com.postech.workshop_service.domain.entities.OrdemServico;
import com.postech.workshop_service.domain.repositories.OrdemServicoRepository;
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

	private final PrepararItemComposicaoService prepararItemService;

	public AdicionarItemOrdemServicoUseCase(OrdemServicoRepository ordemServicoRepository,
			PrepararItemComposicaoService prepararItemService) {
		this.ordemServicoRepository = ordemServicoRepository;
		this.prepararItemService = prepararItemService;
	}

	@Transactional
	public OrdemServico executarServico(UUID idOrdemServico, UUID idServico, BigDecimal quantidade) {
		OrdemServico ordemServico = ordemServicoRepository.buscarPorId(idOrdemServico)
			.orElseThrow(() -> new RecursoNaoEncontradoException("Ordem de servico nao encontrada."));
		BigDecimal qtd = quantidade != null ? quantidade : BigDecimal.ONE;
		ordemServico.adicionarItem(prepararItemService.prepararServico(idServico, qtd));
		return ordemServicoRepository.salvar(ordemServico);
	}

	@Transactional
	public OrdemServico executarPeca(UUID idOrdemServico, UUID idPeca, BigDecimal quantidade) {
		OrdemServico ordemServico = ordemServicoRepository.buscarPorId(idOrdemServico)
			.orElseThrow(() -> new RecursoNaoEncontradoException("Ordem de servico nao encontrada."));
		PrepararItemComposicaoService.PecaPreparada preparada = prepararItemService.prepararPeca(idPeca, quantidade);
		ordemServico.adicionarItem(preparada.item());
		OrdemServico ordemSalva = ordemServicoRepository.salvar(ordemServico);
		prepararItemService.reservarPeca(preparada, ordemSalva);
		return ordemSalva;
	}

}

package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.domain.entities.Estoque;
import com.postech.workshop_service.domain.entities.MovimentacaoEstoque;
import com.postech.workshop_service.domain.entities.OrdemServico;
import com.postech.workshop_service.domain.repositories.EstoqueRepository;
import com.postech.workshop_service.domain.repositories.MovimentacaoEstoqueRepository;
import com.postech.workshop_service.domain.valueobjects.TipoMovimentacao;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Colaborador de aplicacao responsavel por liberar as reservas de estoque de uma ordem de
 * servico.
 *
 * <p>
 * Centraliza a logica antes duplicada entre {@link RejeitarOrcamentoUseCase} e
 * {@link CancelarOrcamentoUseCase}. Quando ja houve baixa efetiva (SAIDA) das pecas, nao
 * ha reserva a liberar e a operacao e ignorada.
 * </p>
 */
@Service
public class LiberarReservasEstoqueService {

	private final EstoqueRepository estoqueRepository;

	private final MovimentacaoEstoqueRepository movimentacaoEstoqueRepository;

	public LiberarReservasEstoqueService(EstoqueRepository estoqueRepository,
			MovimentacaoEstoqueRepository movimentacaoEstoqueRepository) {
		this.estoqueRepository = estoqueRepository;
		this.movimentacaoEstoqueRepository = movimentacaoEstoqueRepository;
	}

	/**
	 * Libera as reservas de estoque associadas a ordem de servico informada.
	 * @param ordemServico ordem de servico cujas reservas serao liberadas.
	 * @param motivo descricao registrada em cada movimentacao de liberacao.
	 */
	public void executar(OrdemServico ordemServico, String motivo) {
		List<MovimentacaoEstoque> movimentacoes = movimentacaoEstoqueRepository
			.listarPorOrdemServico(ordemServico.getId());
		boolean houveBaixa = movimentacoes.stream().anyMatch(m -> m.getTipo() == TipoMovimentacao.SAIDA);
		if (houveBaixa) {
			return;
		}
		for (MovimentacaoEstoque reservaOriginal : movimentacoes.stream()
			.filter(mov -> mov.getTipo() == TipoMovimentacao.RESERVA)
			.toList()) {
			Estoque estoque = estoqueRepository.buscarPorId(reservaOriginal.getEstoqueId(), true).orElse(null);
			if (estoque == null) {
				continue;
			}
			MovimentacaoEstoque liberacao = estoque.liberarReserva(reservaOriginal.getQuantidade(), motivo,
					ordemServico.getId(), reservaOriginal.getOrcamentoId());
			estoqueRepository.salvar(estoque);
			movimentacaoEstoqueRepository.salvar(liberacao);
		}
	}

}

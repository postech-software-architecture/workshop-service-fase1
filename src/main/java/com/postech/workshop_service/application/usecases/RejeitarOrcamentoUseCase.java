package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.application.exceptions.RecursoNaoEncontradoException;
import com.postech.workshop_service.application.exceptions.RegraDeNegocioException;
import com.postech.workshop_service.domain.entities.Estoque;
import com.postech.workshop_service.domain.entities.ItemComposicaoTecnica;
import com.postech.workshop_service.domain.entities.MovimentacaoEstoque;
import com.postech.workshop_service.domain.entities.Orcamento;
import com.postech.workshop_service.domain.entities.OrdemServico;
import com.postech.workshop_service.domain.entities.StatusOrdemServico;
import com.postech.workshop_service.domain.entities.TipoItemComposicaoTecnica;
import com.postech.workshop_service.domain.repositories.EstoqueRepository;
import com.postech.workshop_service.domain.repositories.MovimentacaoEstoqueRepository;
import com.postech.workshop_service.domain.repositories.OrcamentoRepository;
import com.postech.workshop_service.domain.repositories.OrdemServicoRepository;
import com.postech.workshop_service.domain.valueobjects.TipoMovimentacao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Caso de uso responsavel por registrar a rejeicao de um orcamento.
 */
@Service
public class RejeitarOrcamentoUseCase {

	private static final Logger log = LoggerFactory.getLogger(RejeitarOrcamentoUseCase.class);

	private final OrcamentoRepository orcamentoRepository;

	private final OrdemServicoRepository ordemServicoRepository;

	private final EstoqueRepository estoqueRepository;

	private final MovimentacaoEstoqueRepository movimentacaoEstoqueRepository;

	private final MecanicoNotificationService mecanicoNotificationService;

	/**
	 * Construtor para injecao das dependencias do caso de uso.
	 * @param orcamentoRepository repositorio de orcamentos.
	 * @param ordemServicoRepository repositorio de ordens.
	 * @param estoqueRepository repositorio de estoques.
	 * @param movimentacaoEstoqueRepository repositorio de movimentacoes.
	 * @param mecanicoNotificationService service de notificacao do mecanico.
	 */
	public RejeitarOrcamentoUseCase(OrcamentoRepository orcamentoRepository,
			OrdemServicoRepository ordemServicoRepository, EstoqueRepository estoqueRepository,
			MovimentacaoEstoqueRepository movimentacaoEstoqueRepository,
			MecanicoNotificationService mecanicoNotificationService) {
		this.orcamentoRepository = orcamentoRepository;
		this.ordemServicoRepository = ordemServicoRepository;
		this.estoqueRepository = estoqueRepository;
		this.movimentacaoEstoqueRepository = movimentacaoEstoqueRepository;
		this.mecanicoNotificationService = mecanicoNotificationService;
	}

	/**
	 * Registra a rejeicao de um orcamento pendente.
	 * @param idOrcamento identificador do orcamento.
	 * @return orcamento rejeitado.
	 */
	@Transactional
	public Orcamento executar(UUID idOrcamento) {
		Orcamento orcamento = orcamentoRepository.buscarPorId(idOrcamento)
			.orElseThrow(() -> new RecursoNaoEncontradoException("Orcamento nao encontrado."));
		OrdemServico ordemServico = ordemServicoRepository.buscarPorId(orcamento.getIdOrdemServico())
			.orElseThrow(() -> new RecursoNaoEncontradoException("Ordem de servico nao encontrada."));

		if (ordemServico.getStatus() != StatusOrdemServico.AGUARDANDO_RESPOSTA_CLIENTE) {
			throw new RegraDeNegocioException(
					"A ordem de servico deve estar aguardando resposta do cliente para rejeitar o orcamento.");
		}

		orcamento.rejeitar();
		ordemServico.voltarParaComposicao();

		liberarReservasDeEstoque(ordemServico);

		ordemServicoRepository.salvar(ordemServico);
		Orcamento orcamentoPersistido = orcamentoRepository.salvar(orcamento);
		try {
			mecanicoNotificationService.notificarAtualizacaoOrcamento(ordemServico, orcamentoPersistido);
		}
		catch (RuntimeException ex) {
			log.warn("Falha ao notificar mecanico sobre rejeicao do orcamento da OS {}: {}", ordemServico.getNumero(),
					ex.getMessage());
		}
		return orcamentoPersistido;
	}

	private void liberarReservasDeEstoque(OrdemServico ordemServico) {
		String motivoReservaOriginal = "Reserva para OS " + ordemServico.getNumero();
		String motivoLiberacao = "Liberacao de reserva — orcamento rejeitado OS " + ordemServico.getNumero();
		for (ItemComposicaoTecnica item : ordemServico.getItensComposicao()) {
			if (item.getIdPecaInsumo() == null) {
				continue;
			}
			if (item.getTipo() != TipoItemComposicaoTecnica.PECA
					&& item.getTipo() != TipoItemComposicaoTecnica.INSUMO) {
				continue;
			}
			MovimentacaoEstoque reservaOriginal = movimentacaoEstoqueRepository
				.listarPorPeca(item.getIdPecaInsumo(), TipoMovimentacao.RESERVA, null, null)
				.stream()
				.filter(mov -> motivoReservaOriginal.equals(mov.getMotivo()))
				.findFirst()
				.orElse(null);
			if (reservaOriginal == null) {
				continue;
			}
			Estoque estoque = estoqueRepository.buscarPorId(reservaOriginal.getEstoqueId(), true).orElse(null);
			if (estoque == null) {
				continue;
			}
			MovimentacaoEstoque liberacao = estoque.liberarReserva(reservaOriginal.getQuantidade(), motivoLiberacao);
			estoqueRepository.salvar(estoque);
			movimentacaoEstoqueRepository.salvar(liberacao);
		}
	}

}

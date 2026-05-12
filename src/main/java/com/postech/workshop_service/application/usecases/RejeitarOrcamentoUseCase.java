package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.application.exceptions.RecursoNaoEncontradoException;
import com.postech.workshop_service.application.exceptions.RegraDeNegocioException;
import com.postech.workshop_service.domain.entities.Estoque;
import com.postech.workshop_service.domain.entities.MovimentacaoEstoque;
import com.postech.workshop_service.domain.entities.Orcamento;
import com.postech.workshop_service.domain.entities.OrdemServico;
import com.postech.workshop_service.domain.entities.StatusOrdemServico;
import com.postech.workshop_service.domain.repositories.EstoqueRepository;
import com.postech.workshop_service.domain.repositories.MovimentacaoEstoqueRepository;
import com.postech.workshop_service.domain.repositories.OrcamentoRepository;
import com.postech.workshop_service.domain.repositories.OrdemServicoRepository;
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

	private final RegistrarHistoricoStatusOrdemServicoUseCase registrarHistoricoUseCase;

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
			MecanicoNotificationService mecanicoNotificationService,
			RegistrarHistoricoStatusOrdemServicoUseCase registrarHistoricoUseCase) {
		this.orcamentoRepository = orcamentoRepository;
		this.ordemServicoRepository = ordemServicoRepository;
		this.estoqueRepository = estoqueRepository;
		this.movimentacaoEstoqueRepository = movimentacaoEstoqueRepository;
		this.mecanicoNotificationService = mecanicoNotificationService;
		this.registrarHistoricoUseCase = registrarHistoricoUseCase;
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

		if (ordemServico.getStatus() != StatusOrdemServico.AGUARDANDO_APROVACAO) {
			throw new RegraDeNegocioException(
					"A ordem de servico deve estar aguardando resposta do cliente para rejeitar o orcamento.");
		}

		orcamento.rejeitar();
		StatusOrdemServico statusAnterior = ordemServico.getStatus();
		ordemServico.voltarParaComposicao();

		liberarReservasDeEstoque(ordemServico);

		ordemServicoRepository.salvar(ordemServico);
		registrarHistoricoUseCase.executar(ordemServico.getId(), statusAnterior, ordemServico.getStatus());
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
		String motivoLiberacao = "Liberacao de reserva - orcamento rejeitado OS " + ordemServico.getNumero();
		java.util.List<MovimentacaoEstoque> movimentacoes = movimentacaoEstoqueRepository
			.listarPorOrdemServico(ordemServico.getId());
		boolean houveBaixa = movimentacoes.stream()
			.anyMatch(m -> m.getTipo() == com.postech.workshop_service.domain.valueobjects.TipoMovimentacao.SAIDA);
		if (houveBaixa) {
			return;
		}
		for (MovimentacaoEstoque reservaOriginal : movimentacoes.stream()
			.filter(mov -> mov.getTipo() == com.postech.workshop_service.domain.valueobjects.TipoMovimentacao.RESERVA)
			.toList()) {
			Estoque estoque = estoqueRepository.buscarPorId(reservaOriginal.getEstoqueId(), true).orElse(null);
			if (estoque == null) {
				continue;
			}
			MovimentacaoEstoque liberacao = estoque.liberarReserva(reservaOriginal.getQuantidade(), motivoLiberacao,
					ordemServico.getId(), reservaOriginal.getOrcamentoId());
			estoqueRepository.salvar(estoque);
			movimentacaoEstoqueRepository.salvar(liberacao);
		}
	}

}

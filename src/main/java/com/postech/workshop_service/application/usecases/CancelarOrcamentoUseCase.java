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
 * Caso de uso responsavel por cancelar um orcamento pendente e a ordem associada.
 */
@Service
public class CancelarOrcamentoUseCase {

	private static final Logger log = LoggerFactory.getLogger(CancelarOrcamentoUseCase.class);

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
	public CancelarOrcamentoUseCase(OrcamentoRepository orcamentoRepository,
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
	 * Cancela um orcamento pendente e encerra a ordem vinculada.
	 * @param idOrcamento identificador do orcamento.
	 * @return orcamento cancelado.
	 */
	@Transactional
	public Orcamento executar(UUID idOrcamento) {
		Orcamento orcamento = orcamentoRepository.buscarPorId(idOrcamento)
			.orElseThrow(() -> new RecursoNaoEncontradoException("Orcamento nao encontrado."));
		OrdemServico ordemServico = ordemServicoRepository.buscarPorId(orcamento.getIdOrdemServico())
			.orElseThrow(() -> new RecursoNaoEncontradoException("Ordem de servico nao encontrada."));

		if (ordemServico.getStatus() != StatusOrdemServico.AGUARDANDO_APROVACAO) {
			throw new RegraDeNegocioException(
					"A ordem de servico deve estar aguardando resposta do cliente para cancelar o orcamento.");
		}

		StatusOrdemServico statusAnterior = ordemServico.getStatus();
		orcamento.cancelar(ordemServico);

		liberarReservasDeEstoque(ordemServico);

		ordemServicoRepository.salvar(ordemServico);
		registrarHistoricoUseCase.executar(ordemServico.getId(), statusAnterior, ordemServico.getStatus());
		Orcamento orcamentoPersistido = orcamentoRepository.salvar(orcamento);
		try {
			mecanicoNotificationService.notificarAtualizacaoOrcamento(ordemServico, orcamentoPersistido);
		}
		catch (RuntimeException ex) {
			log.warn("Falha ao notificar mecanico sobre cancelamento do orcamento da OS {}: {}",
					ordemServico.getNumero(), ex.getMessage());
		}
		return orcamentoPersistido;
	}

	private void liberarReservasDeEstoque(OrdemServico ordemServico) {
		String motivoLiberacao = "Liberacao de reserva - orcamento cancelado OS " + ordemServico.getNumero();
		for (MovimentacaoEstoque reservaOriginal : movimentacaoEstoqueRepository
			.listarPorOrdemServico(ordemServico.getId())
			.stream()
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

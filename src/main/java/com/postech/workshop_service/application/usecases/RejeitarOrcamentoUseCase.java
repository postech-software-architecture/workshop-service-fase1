package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.domain.exceptions.RecursoNaoEncontradoException;
import com.postech.workshop_service.domain.exceptions.RegraDeNegocioException;
import com.postech.workshop_service.domain.entities.Orcamento;
import com.postech.workshop_service.domain.entities.OrdemServico;
import com.postech.workshop_service.domain.entities.StatusOrdemServico;
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

	private final LiberarReservasEstoqueService liberarReservasEstoqueService;

	private final MecanicoNotificationService mecanicoNotificationService;

	private final RegistrarHistoricoStatusOrdemServicoUseCase registrarHistoricoUseCase;

	/**
	 * Construtor para injecao das dependencias do caso de uso.
	 * @param orcamentoRepository repositorio de orcamentos.
	 * @param ordemServicoRepository repositorio de ordens.
	 * @param liberarReservasEstoqueService colaborador de liberacao de reservas de
	 * estoque.
	 * @param mecanicoNotificationService service de notificacao do mecanico.
	 * @param registrarHistoricoUseCase caso de uso de registro de historico de status.
	 */
	public RejeitarOrcamentoUseCase(OrcamentoRepository orcamentoRepository,
			OrdemServicoRepository ordemServicoRepository, LiberarReservasEstoqueService liberarReservasEstoqueService,
			MecanicoNotificationService mecanicoNotificationService,
			RegistrarHistoricoStatusOrdemServicoUseCase registrarHistoricoUseCase) {
		this.orcamentoRepository = orcamentoRepository;
		this.ordemServicoRepository = ordemServicoRepository;
		this.liberarReservasEstoqueService = liberarReservasEstoqueService;
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

		liberarReservasEstoqueService.executar(ordemServico,
				"Liberacao de reserva - orcamento rejeitado OS " + ordemServico.getNumero());

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

}

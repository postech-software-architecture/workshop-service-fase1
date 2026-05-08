package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.application.exceptions.RecursoNaoEncontradoException;
import com.postech.workshop_service.application.exceptions.RegraDeNegocioException;
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
 * Caso de uso responsavel por registrar a aprovacao de um orcamento.
 */
@Service
public class AprovarOrcamentoUseCase {

	private static final Logger log = LoggerFactory.getLogger(AprovarOrcamentoUseCase.class);

	private final OrcamentoRepository orcamentoRepository;

	private final OrdemServicoRepository ordemServicoRepository;

	private final MecanicoNotificationService mecanicoNotificationService;

	private final RegistrarHistoricoStatusOrdemServicoUseCase registrarHistoricoUseCase;

	/**
	 * Construtor para injecao das dependencias do caso de uso.
	 * @param orcamentoRepository repositorio de orcamentos.
	 * @param ordemServicoRepository repositorio de ordens.
	 * @param mecanicoNotificationService service de notificacao do mecanico.
	 * @param registrarHistoricoUseCase caso de uso para registro de historico.
	 */
	public AprovarOrcamentoUseCase(OrcamentoRepository orcamentoRepository,
			OrdemServicoRepository ordemServicoRepository, MecanicoNotificationService mecanicoNotificationService,
			RegistrarHistoricoStatusOrdemServicoUseCase registrarHistoricoUseCase) {
		this.orcamentoRepository = orcamentoRepository;
		this.ordemServicoRepository = ordemServicoRepository;
		this.mecanicoNotificationService = mecanicoNotificationService;
		this.registrarHistoricoUseCase = registrarHistoricoUseCase;
	}

	/**
	 * Registra a aprovacao de um orcamento pendente.
	 * @param idOrcamento identificador do orcamento.
	 * @return orcamento aprovado.
	 */
	@Transactional
	public Orcamento executar(UUID idOrcamento) {
		Orcamento orcamento = orcamentoRepository.buscarPorId(idOrcamento)
			.orElseThrow(() -> new RecursoNaoEncontradoException("Orcamento nao encontrado."));
		OrdemServico ordemServico = buscarOrdemVinculada(orcamento);

		validarOrdemAguardandoResposta(ordemServico);
		validarOrcamentoPendente(orcamento);

		StatusOrdemServico statusAnterior = ordemServico.getStatus();
		orcamento.aprovar(ordemServico);

		ordemServicoRepository.salvar(ordemServico);
		registrarHistoricoUseCase.executar(ordemServico.getId(), statusAnterior, ordemServico.getStatus());
		Orcamento orcamentoPersistido = orcamentoRepository.salvar(orcamento);
		try {
			mecanicoNotificationService.notificarAtualizacaoOrcamento(ordemServico, orcamentoPersistido);
		}
		catch (RuntimeException ex) {
			log.warn("Falha ao notificar mecanico sobre aprovacao do orcamento da OS {}: {}", ordemServico.getNumero(),
					ex.getMessage());
		}
		return orcamentoPersistido;
	}

	private OrdemServico buscarOrdemVinculada(Orcamento orcamento) {
		return ordemServicoRepository.buscarPorId(orcamento.getIdOrdemServico())
			.orElseThrow(() -> new RecursoNaoEncontradoException("Ordem de servico nao encontrada."));
	}

	private void validarOrdemAguardandoResposta(OrdemServico ordemServico) {
		if (ordemServico.getStatus() != StatusOrdemServico.AGUARDANDO_RESPOSTA_CLIENTE) {
			throw new RegraDeNegocioException(
					"A ordem de servico deve estar aguardando resposta do cliente para aprovar o orcamento.");
		}
	}

	private void validarOrcamentoPendente(Orcamento orcamento) {
		if (orcamento.getStatus() != com.postech.workshop_service.domain.entities.StatusOrcamento.PENDENTE_APROVACAO) {
			throw new RegraDeNegocioException(
					"Nao e permitido aprovar um orcamento com status " + orcamento.getStatus() + ".");
		}
	}

}

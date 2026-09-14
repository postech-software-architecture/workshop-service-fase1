package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.domain.exceptions.RecursoNaoEncontradoException;
import com.postech.workshop_service.domain.entities.OrdemServico;
import com.postech.workshop_service.domain.entities.StatusOrdemServico;
import com.postech.workshop_service.domain.repositories.OrdemServicoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Caso de uso responsavel por finalizar a execucao tecnica da ordem.
 */
@Service
public class FinalizarExecucaoUseCase {

	private final OrdemServicoRepository ordemServicoRepository;

	private final RegistrarHistoricoStatusOrdemServicoUseCase registrarHistoricoUseCase;

	private final OrdemServicoMetrics ordemServicoMetrics;

	public FinalizarExecucaoUseCase(OrdemServicoRepository ordemServicoRepository,
			RegistrarHistoricoStatusOrdemServicoUseCase registrarHistoricoUseCase,
			OrdemServicoMetrics ordemServicoMetrics) {
		this.ordemServicoRepository = ordemServicoRepository;
		this.registrarHistoricoUseCase = registrarHistoricoUseCase;
		this.ordemServicoMetrics = ordemServicoMetrics;
	}

	@Transactional
	public OrdemServico executar(UUID idOrdemServico) {
		OrdemServico ordemServico = ordemServicoRepository.buscarPorId(idOrdemServico)
			.orElseThrow(() -> new RecursoNaoEncontradoException("Ordem de servico nao encontrada."));
		StatusOrdemServico statusAnterior = ordemServico.getStatus();
		try {
			ordemServico.finalizarExecucao();
			OrdemServico ordemSalva = ordemServicoRepository.salvar(ordemServico);
			registrarHistoricoUseCase.executar(ordemSalva.getId(), statusAnterior, ordemSalva.getStatus());
			return ordemSalva;
		}
		catch (RuntimeException ex) {
			if (ordemServicoMetrics != null) {
				ordemServicoMetrics.erroDeProcessamento("execucao", "finalizar");
			}
			throw ex;
		}
	}

}

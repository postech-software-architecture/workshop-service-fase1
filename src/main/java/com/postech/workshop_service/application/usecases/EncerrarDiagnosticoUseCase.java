package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.domain.exceptions.RecursoNaoEncontradoException;
import com.postech.workshop_service.domain.entities.OrdemServico;
import com.postech.workshop_service.domain.entities.StatusOrdemServico;
import com.postech.workshop_service.domain.repositories.OrdemServicoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Caso de uso responsavel por encerrar o diagnostico e avancar para composicao tecnica.
 */
@Service
public class EncerrarDiagnosticoUseCase {

	private final OrdemServicoRepository ordemServicoRepository;

	private final RegistrarHistoricoStatusOrdemServicoUseCase registrarHistoricoUseCase;

	public EncerrarDiagnosticoUseCase(OrdemServicoRepository ordemServicoRepository,
			RegistrarHistoricoStatusOrdemServicoUseCase registrarHistoricoUseCase) {
		this.ordemServicoRepository = ordemServicoRepository;
		this.registrarHistoricoUseCase = registrarHistoricoUseCase;
	}

	@Transactional
	public OrdemServico executar(UUID idOrdemServico) {
		OrdemServico ordemServico = ordemServicoRepository.buscarPorId(idOrdemServico)
			.orElseThrow(() -> new RecursoNaoEncontradoException("Ordem de servico nao encontrada."));
		StatusOrdemServico statusAnterior = ordemServico.getStatus();
		ordemServico.encerrarDiagnostico();
		OrdemServico ordemSalva = ordemServicoRepository.salvar(ordemServico);
		registrarHistoricoUseCase.executar(ordemSalva.getId(), statusAnterior, ordemSalva.getStatus());
		return ordemSalva;
	}

}

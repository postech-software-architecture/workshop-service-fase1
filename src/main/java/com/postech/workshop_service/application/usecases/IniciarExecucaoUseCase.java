package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.application.exceptions.RecursoNaoEncontradoException;
import com.postech.workshop_service.domain.entities.OrdemServico;
import com.postech.workshop_service.domain.entities.StatusOrdemServico;
import com.postech.workshop_service.domain.repositories.OrdemServicoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Caso de uso responsavel por iniciar a execucao tecnica de uma ordem aprovada.
 *
 * <p>
 * A baixa do estoque ja foi efetivada na aprovacao do orcamento; aqui apenas a OS muda de
 * status para EM_EXECUCAO.
 * </p>
 */
@Service
public class IniciarExecucaoUseCase {

	private final OrdemServicoRepository ordemServicoRepository;

	private final RegistrarHistoricoStatusOrdemServicoUseCase registrarHistoricoUseCase;

	public IniciarExecucaoUseCase(OrdemServicoRepository ordemServicoRepository,
			RegistrarHistoricoStatusOrdemServicoUseCase registrarHistoricoUseCase) {
		this.ordemServicoRepository = ordemServicoRepository;
		this.registrarHistoricoUseCase = registrarHistoricoUseCase;
	}

	@Transactional
	public OrdemServico executar(UUID idOrdemServico) {
		OrdemServico ordemServico = ordemServicoRepository.buscarPorId(idOrdemServico)
			.orElseThrow(() -> new RecursoNaoEncontradoException("Ordem de servico nao encontrada."));
		StatusOrdemServico statusAnterior = ordemServico.getStatus();
		ordemServico.iniciarExecucao();
		OrdemServico ordemSalva = ordemServicoRepository.salvar(ordemServico);
		registrarHistoricoUseCase.executar(ordemSalva.getId(), statusAnterior, ordemSalva.getStatus());
		return ordemSalva;
	}

}

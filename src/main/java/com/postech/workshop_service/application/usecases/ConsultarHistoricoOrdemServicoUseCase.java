package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.application.exceptions.RecursoNaoEncontradoException;
import com.postech.workshop_service.domain.entities.HistoricoStatusOrdemServico;
import com.postech.workshop_service.domain.repositories.HistoricoStatusOrdemServicoRepository;
import com.postech.workshop_service.domain.repositories.OrdemServicoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Consulta a linha do tempo de transicoes de status de uma ordem de servico.
 */
@Service
public class ConsultarHistoricoOrdemServicoUseCase {

	private final OrdemServicoRepository ordemServicoRepository;

	private final HistoricoStatusOrdemServicoRepository historicoRepository;

	public ConsultarHistoricoOrdemServicoUseCase(OrdemServicoRepository ordemServicoRepository,
			HistoricoStatusOrdemServicoRepository historicoRepository) {
		this.ordemServicoRepository = ordemServicoRepository;
		this.historicoRepository = historicoRepository;
	}

	@Transactional(readOnly = true)
	public List<HistoricoStatusOrdemServico> executar(UUID idOrdemServico) {
		ordemServicoRepository.buscarPorId(idOrdemServico)
			.orElseThrow(() -> new RecursoNaoEncontradoException("Ordem de servico nao encontrada."));
		return historicoRepository.listarPorOrdemServico(idOrdemServico);
	}

}

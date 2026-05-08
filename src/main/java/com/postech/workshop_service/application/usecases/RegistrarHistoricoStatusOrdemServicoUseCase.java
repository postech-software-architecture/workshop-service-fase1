package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.domain.entities.HistoricoStatusOrdemServico;
import com.postech.workshop_service.domain.entities.StatusOrdemServico;
import com.postech.workshop_service.domain.repositories.HistoricoStatusOrdemServicoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Registra uma transicao de status da ordem de servico com responsavel autenticado.
 */
@Service
public class RegistrarHistoricoStatusOrdemServicoUseCase {

	private final HistoricoStatusOrdemServicoRepository historicoRepository;

	private final BuscarResponsavelTransicaoUseCase buscarResponsavelTransicaoUseCase;

	public RegistrarHistoricoStatusOrdemServicoUseCase(HistoricoStatusOrdemServicoRepository historicoRepository,
			BuscarResponsavelTransicaoUseCase buscarResponsavelTransicaoUseCase) {
		this.historicoRepository = historicoRepository;
		this.buscarResponsavelTransicaoUseCase = buscarResponsavelTransicaoUseCase;
	}

	public HistoricoStatusOrdemServico executar(UUID idOrdemServico, StatusOrdemServico statusAnterior,
			StatusOrdemServico statusNovo) {
		ResponsavelTransicao responsavel = buscarResponsavelTransicaoUseCase.executar();
		HistoricoStatusOrdemServico historico = new HistoricoStatusOrdemServico(null, idOrdemServico, statusAnterior,
				statusNovo, LocalDateTime.now(), responsavel.idUsuario(), responsavel.username());
		return historicoRepository.salvar(historico);
	}

}

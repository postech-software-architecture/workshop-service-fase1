package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.domain.entities.HistoricoStatusOrdemServico;
import com.postech.workshop_service.domain.entities.OrdemServico;
import com.postech.workshop_service.domain.entities.StatusOrdemServico;
import com.postech.workshop_service.domain.repositories.HistoricoStatusOrdemServicoRepository;
import com.postech.workshop_service.domain.repositories.OrdemServicoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Registra uma transicao de status da ordem de servico com responsavel autenticado e
 * notifica o cliente sobre a mudanca. Centraliza o disparo de notificacao para todas as
 * transicoes de status.
 */
@Service
public class RegistrarHistoricoStatusOrdemServicoUseCase {

	private static final Logger LOGGER = LoggerFactory.getLogger(RegistrarHistoricoStatusOrdemServicoUseCase.class);

	private final HistoricoStatusOrdemServicoRepository historicoRepository;

	private final BuscarResponsavelTransicaoUseCase buscarResponsavelTransicaoUseCase;

	private final OrdemServicoRepository ordemServicoRepository;

	private final ClienteNotificationService clienteNotificationService;

	public RegistrarHistoricoStatusOrdemServicoUseCase(HistoricoStatusOrdemServicoRepository historicoRepository,
			BuscarResponsavelTransicaoUseCase buscarResponsavelTransicaoUseCase,
			OrdemServicoRepository ordemServicoRepository, ClienteNotificationService clienteNotificationService) {
		this.historicoRepository = historicoRepository;
		this.buscarResponsavelTransicaoUseCase = buscarResponsavelTransicaoUseCase;
		this.ordemServicoRepository = ordemServicoRepository;
		this.clienteNotificationService = clienteNotificationService;
	}

	public HistoricoStatusOrdemServico executar(UUID idOrdemServico, StatusOrdemServico statusAnterior,
			StatusOrdemServico statusNovo) {
		ResponsavelTransicao responsavel = buscarResponsavelTransicaoUseCase.executar();
		HistoricoStatusOrdemServico historico = new HistoricoStatusOrdemServico(null, idOrdemServico, statusAnterior,
				statusNovo, LocalDateTime.now(), responsavel.idUsuario(), responsavel.username());
		HistoricoStatusOrdemServico salvo = historicoRepository.salvar(historico);
		notificarCliente(idOrdemServico, statusAnterior, statusNovo);
		return salvo;
	}

	private void notificarCliente(UUID idOrdemServico, StatusOrdemServico statusAnterior,
			StatusOrdemServico statusNovo) {
		try {
			OrdemServico ordemServico = ordemServicoRepository.buscarPorId(idOrdemServico).orElse(null);
			if (ordemServico != null) {
				clienteNotificationService.notificarMudancaStatus(ordemServico, statusAnterior, statusNovo);
			}
		}
		catch (RuntimeException ex) {
			LOGGER.warn("Falha ao notificar cliente sobre mudanca de status da OS {}: {}", idOrdemServico,
					ex.getMessage());
		}
	}

}

package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.domain.entities.HistoricoStatusOrdemServico;
import com.postech.workshop_service.domain.entities.StatusOrdemServico;
import com.postech.workshop_service.domain.repositories.HistoricoStatusOrdemServicoRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Registra uma transicao de status da ordem de servico com responsavel auditavel e
 * publica um evento de mudanca de status. A notificacao ao cliente e feita de forma
 * assincrona apos o commit (ver {@code NotificacaoMudancaStatusListener}), para nao
 * acoplar um canal externo lento a transacao de negocio.
 */
@Service
public class RegistrarHistoricoStatusOrdemServicoUseCase {

	private final HistoricoStatusOrdemServicoRepository historicoRepository;

	private final BuscarResponsavelTransicaoUseCase buscarResponsavelTransicaoUseCase;

	private final ApplicationEventPublisher eventPublisher;

	public RegistrarHistoricoStatusOrdemServicoUseCase(HistoricoStatusOrdemServicoRepository historicoRepository,
			BuscarResponsavelTransicaoUseCase buscarResponsavelTransicaoUseCase,
			ApplicationEventPublisher eventPublisher) {
		this.historicoRepository = historicoRepository;
		this.buscarResponsavelTransicaoUseCase = buscarResponsavelTransicaoUseCase;
		this.eventPublisher = eventPublisher;
	}

	public HistoricoStatusOrdemServico executar(UUID idOrdemServico, StatusOrdemServico statusAnterior,
			StatusOrdemServico statusNovo) {
		ResponsavelTransicao responsavel = buscarResponsavelTransicaoUseCase.executar();
		HistoricoStatusOrdemServico historico = new HistoricoStatusOrdemServico(null, idOrdemServico, statusAnterior,
				statusNovo, LocalDateTime.now(), responsavel.idUsuario(), responsavel.username());
		HistoricoStatusOrdemServico salvo = historicoRepository.salvar(historico);
		// A notificacao acontece apos o commit e fora da thread (listener assincrono).
		eventPublisher.publishEvent(new MudancaStatusOrdemServicoEvent(idOrdemServico, statusAnterior, statusNovo));
		return salvo;
	}

}

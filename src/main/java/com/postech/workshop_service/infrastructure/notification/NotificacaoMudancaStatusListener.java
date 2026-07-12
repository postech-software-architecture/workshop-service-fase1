package com.postech.workshop_service.infrastructure.notification;

import com.postech.workshop_service.application.usecases.ClienteNotificationService;
import com.postech.workshop_service.application.usecases.MudancaStatusOrdemServicoEvent;
import com.postech.workshop_service.domain.entities.OrdemServico;
import com.postech.workshop_service.domain.repositories.OrdemServicoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Ouve {@link MudancaStatusOrdemServicoEvent} e notifica o cliente APOS o commit da
 * transacao ({@link TransactionPhase#AFTER_COMMIT}) e em thread separada ({@link Async}).
 *
 * <p>
 * Assim, um canal de notificacao lento ou indisponivel (ex.: SMTP) nunca segura a conexao
 * ou a transacao do banco, nem atrasa a resposta HTTP da transicao de status. Falhas de
 * notificacao sao logadas e nunca propagam.
 * </p>
 */
@Component
public class NotificacaoMudancaStatusListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(NotificacaoMudancaStatusListener.class);

	private final OrdemServicoRepository ordemServicoRepository;

	private final ClienteNotificationService clienteNotificationService;

	public NotificacaoMudancaStatusListener(OrdemServicoRepository ordemServicoRepository,
			ClienteNotificationService clienteNotificationService) {
		this.ordemServicoRepository = ordemServicoRepository;
		this.clienteNotificationService = clienteNotificationService;
	}

	@Async("notificacaoExecutor")
	// REQUIRES_NEW: o listener roda apos o commit da transacao original, entao precisa
	// abrir
	// a sua propria transacao (exigencia do @TransactionalEventListener AFTER_COMMIT).
	@Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void aoMudarStatus(MudancaStatusOrdemServicoEvent evento) {
		try {
			OrdemServico ordemServico = ordemServicoRepository.buscarPorId(evento.idOrdemServico()).orElse(null);
			if (ordemServico != null) {
				clienteNotificationService.notificarMudancaStatus(ordemServico, evento.anterior(), evento.novo());
			}
		}
		catch (RuntimeException ex) {
			LOGGER.warn("Falha ao notificar cliente sobre mudanca de status da OS {}", evento.idOrdemServico(), ex);
		}
	}

}

package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.domain.entities.Orcamento;
import com.postech.workshop_service.domain.entities.OrdemServico;
import com.postech.workshop_service.domain.entities.StatusOrdemServico;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Implementacao da notificacao de cliente baseada em log. Bean ativo por padrao (canal
 * {@code log}); em ambientes com {@code notificacao.canal=email} cede lugar ao
 * {@code EmailClienteNotificationService}.
 */
@Service
@ConditionalOnProperty(name = "notificacao.canal", havingValue = "log", matchIfMissing = true)
public class LogClienteNotificationService implements ClienteNotificationService {

	private static final Logger LOGGER = LoggerFactory.getLogger(LogClienteNotificationService.class);

	/**
	 * Registra em log o envio do orcamento ao cliente.
	 * @param ordemServico ordem de servico associada.
	 * @param orcamento orcamento enviado.
	 */
	@Override
	public void notificarOrcamentoPendente(OrdemServico ordemServico, Orcamento orcamento) {
		LOGGER.info("Notificacao simulada enviada ao cliente da ordem {} para o orcamento {}.", ordemServico.getId(),
				orcamento.getId());
	}

	/**
	 * Registra em log a mudanca de status da ordem de servico.
	 * @param ordemServico ordem de servico associada.
	 * @param anterior status anterior.
	 * @param novo novo status.
	 */
	@Override
	public void notificarMudancaStatus(OrdemServico ordemServico, StatusOrdemServico anterior,
			StatusOrdemServico novo) {
		LOGGER.info("Notificacao simulada ao cliente da OS {}: status {} -> {}.", ordemServico.getNumero(), anterior,
				novo);
	}

}

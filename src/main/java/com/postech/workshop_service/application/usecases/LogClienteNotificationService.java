package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.domain.entities.Orcamento;
import com.postech.workshop_service.domain.entities.OrdemServico;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Implementacao temporaria da notificacao de cliente baseada em log.
 */
@Service
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

}

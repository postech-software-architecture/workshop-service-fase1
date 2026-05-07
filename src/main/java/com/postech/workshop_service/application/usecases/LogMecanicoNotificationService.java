package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.domain.entities.Orcamento;
import com.postech.workshop_service.domain.entities.OrdemServico;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Implementacao temporaria da notificacao de mecanico baseada em log.
 */
@Service
public class LogMecanicoNotificationService implements MecanicoNotificationService {

	private static final Logger LOGGER = LoggerFactory.getLogger(LogMecanicoNotificationService.class);

	/**
	 * Registra em log a atualizacao de status do orcamento para o mecanico.
	 * @param ordemServico ordem de servico associada.
	 * @param orcamento orcamento atualizado.
	 */
	@Override
	public void notificarAtualizacaoOrcamento(OrdemServico ordemServico, Orcamento orcamento) {
		LOGGER.info("Notificacao simulada enviada ao mecanico da ordem {} para o orcamento {} com status {}.",
				ordemServico.getId(), orcamento.getId(), orcamento.getStatus());
	}

}

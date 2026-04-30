package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.domain.entities.Orcamento;
import com.postech.workshop_service.domain.entities.OrdemServico;

/**
 * Porta de notificacao do cliente para eventos do fluxo de orcamento.
 */
public interface ClienteNotificationService {

	/**
	 * Registra o envio do orcamento ao cliente.
	 * @param ordemServico ordem de servico associada.
	 * @param orcamento orcamento enviado.
	 */
	void notificarOrcamentoPendente(OrdemServico ordemServico, Orcamento orcamento);

}

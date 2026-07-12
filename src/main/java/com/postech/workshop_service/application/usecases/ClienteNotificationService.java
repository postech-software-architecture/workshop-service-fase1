package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.domain.entities.Orcamento;
import com.postech.workshop_service.domain.entities.OrdemServico;
import com.postech.workshop_service.domain.entities.StatusOrdemServico;

/**
 * Porta de notificacao do cliente para eventos do fluxo da ordem de servico.
 */
public interface ClienteNotificationService {

	/**
	 * Registra o envio do orcamento ao cliente.
	 * @param ordemServico ordem de servico associada.
	 * @param orcamento orcamento enviado.
	 */
	void notificarOrcamentoPendente(OrdemServico ordemServico, Orcamento orcamento);

	/**
	 * Notifica o cliente sobre a transicao de status da ordem de servico.
	 * @param ordemServico ordem de servico associada.
	 * @param anterior status anterior.
	 * @param novo novo status.
	 */
	void notificarMudancaStatus(OrdemServico ordemServico, StatusOrdemServico anterior, StatusOrdemServico novo);

}

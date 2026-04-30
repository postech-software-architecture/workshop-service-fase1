package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.domain.entities.Orcamento;
import com.postech.workshop_service.domain.entities.OrdemServico;

/**
 * Porta de notificacao do mecanico para atualizacoes do fluxo de orcamento.
 */
public interface MecanicoNotificationService {

	/**
	 * Registra uma atualizacao relevante de status do orcamento para o mecanico.
	 * @param ordemServico ordem de servico associada.
	 * @param orcamento orcamento atualizado.
	 */
	void notificarAtualizacaoOrcamento(OrdemServico ordemServico, Orcamento orcamento);

}

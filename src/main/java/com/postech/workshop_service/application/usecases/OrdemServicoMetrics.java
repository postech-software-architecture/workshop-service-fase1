package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.domain.entities.HistoricoStatusOrdemServico;

import java.util.List;

/** Porta de observabilidade dos eventos de negocio da ordem de servico. */
public interface OrdemServicoMetrics {

	void ordemServicoCriada();

	void transicaoRegistrada(HistoricoStatusOrdemServico historico,
			List<HistoricoStatusOrdemServico> historicoCompleto);

	void erroDeProcessamento(String stage, String operation);

	void erroDeIntegracao(String integration, String operation);

}

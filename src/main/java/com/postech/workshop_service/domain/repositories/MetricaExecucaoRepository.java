package com.postech.workshop_service.domain.repositories;

import com.postech.workshop_service.domain.valueobjects.TempoMedioGlobal;
import com.postech.workshop_service.domain.valueobjects.TempoMedioPorTipoServico;

import java.util.List;

/**
 * Contrato de acesso a metricas de tempo de execucao das ordens de servico.
 */
public interface MetricaExecucaoRepository {

	/**
	 * Calcula o tempo medio global de execucao das ordens finalizadas ou entregues.
	 * Considera apenas ordens com data_inicio_execucao e data_finalizacao preenchidos.
	 * @return agregado com media, minimo, maximo e total de ordens.
	 */
	TempoMedioGlobal consultarTempoMedioGlobal();

	/**
	 * Calcula o tempo medio de execucao agrupado pela descricao dos itens do tipo SERVICO
	 * presentes na composicao tecnica das ordens finalizadas ou entregues.
	 * @return lista de resultados ordenada pelo maior tempo medio.
	 */
	List<TempoMedioPorTipoServico> consultarTempoMedioPorTipoServico();

}

package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.domain.repositories.MetricaExecucaoRepository;
import com.postech.workshop_service.domain.valueobjects.TempoMedioGlobal;
import org.springframework.stereotype.Service;

/**
 * Caso de uso que retorna o tempo medio global de execucao das ordens de servico
 * finalizadas.
 */
@Service
public class ConsultarTempoMedioExecucaoUseCase {

	private final MetricaExecucaoRepository metricaExecucaoRepository;

	public ConsultarTempoMedioExecucaoUseCase(MetricaExecucaoRepository metricaExecucaoRepository) {
		this.metricaExecucaoRepository = metricaExecucaoRepository;
	}

	public TempoMedioGlobal executar() {
		return metricaExecucaoRepository.consultarTempoMedioGlobal();
	}

}

package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.domain.repositories.MetricaExecucaoRepository;
import com.postech.workshop_service.domain.valueobjects.TempoMedioPorTipoServico;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Caso de uso que retorna o tempo medio de execucao agrupado pela descricao dos servicos
 * presentes na composicao tecnica das ordens finalizadas.
 */
@Service
public class ConsultarTempoMedioPorTipoServicoUseCase {

	private final MetricaExecucaoRepository metricaExecucaoRepository;

	public ConsultarTempoMedioPorTipoServicoUseCase(MetricaExecucaoRepository metricaExecucaoRepository) {
		this.metricaExecucaoRepository = metricaExecucaoRepository;
	}

	public List<TempoMedioPorTipoServico> executar() {
		return metricaExecucaoRepository.consultarTempoMedioPorTipoServico();
	}

}

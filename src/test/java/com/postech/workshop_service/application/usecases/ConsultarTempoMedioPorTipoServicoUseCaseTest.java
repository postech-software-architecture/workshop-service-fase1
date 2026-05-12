package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.domain.repositories.MetricaExecucaoRepository;
import com.postech.workshop_service.domain.valueobjects.TempoMedioPorTipoServico;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsultarTempoMedioPorTipoServicoUseCaseTest {

	@Mock
	private MetricaExecucaoRepository metricaExecucaoRepository;

	@InjectMocks
	private ConsultarTempoMedioPorTipoServicoUseCase useCase;

	@Test
	void shouldReturnListGroupedByTipoServico() {
		List<TempoMedioPorTipoServico> esperado = List.of(new TempoMedioPorTipoServico("Revisao de freios", 5L, 180.0),
				new TempoMedioPorTipoServico("Troca de oleo e filtro", 8L, 45.0));
		when(metricaExecucaoRepository.consultarTempoMedioPorTipoServico()).thenReturn(esperado);

		List<TempoMedioPorTipoServico> resultado = useCase.executar();

		assertThat(resultado).hasSize(2);
		assertThat(resultado.get(0).descricaoServico()).isEqualTo("Revisao de freios");
		assertThat(resultado.get(0).totalExecucoes()).isEqualTo(5L);
		assertThat(resultado.get(0).tempoMedioMinutos()).isEqualTo(180.0);
	}

	@Test
	void shouldReturnEmptyListWhenNoData() {
		when(metricaExecucaoRepository.consultarTempoMedioPorTipoServico()).thenReturn(List.of());

		List<TempoMedioPorTipoServico> resultado = useCase.executar();

		assertThat(resultado).isEmpty();
	}

}

package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.domain.repositories.MetricaExecucaoRepository;
import com.postech.workshop_service.domain.valueobjects.TempoMedioGlobal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsultarTempoMedioExecucaoUseCaseTest {

	@Mock
	private MetricaExecucaoRepository metricaExecucaoRepository;

	@InjectMocks
	private ConsultarTempoMedioExecucaoUseCase useCase;

	@Test
	void shouldReturnTempoMedioGlobal() {
		TempoMedioGlobal esperado = new TempoMedioGlobal(10L, 120.5, 30.0, 480.0);
		when(metricaExecucaoRepository.consultarTempoMedioGlobal()).thenReturn(esperado);

		TempoMedioGlobal resultado = useCase.executar();

		assertThat(resultado.totalOrdens()).isEqualTo(10L);
		assertThat(resultado.tempoMedioMinutos()).isEqualTo(120.5);
		assertThat(resultado.tempoMinimoMinutos()).isEqualTo(30.0);
		assertThat(resultado.tempoMaximoMinutos()).isEqualTo(480.0);
	}

	@Test
	void shouldReturnZeroWhenNoOrders() {
		TempoMedioGlobal vazio = new TempoMedioGlobal(0L, 0.0, 0.0, 0.0);
		when(metricaExecucaoRepository.consultarTempoMedioGlobal()).thenReturn(vazio);

		TempoMedioGlobal resultado = useCase.executar();

		assertThat(resultado.totalOrdens()).isZero();
		assertThat(resultado.tempoMedioMinutos()).isZero();
	}

}

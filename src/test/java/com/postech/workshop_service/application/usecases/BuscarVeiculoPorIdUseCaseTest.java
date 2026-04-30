package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.domain.entities.Veiculo;
import com.postech.workshop_service.domain.repositories.VeiculoRepository;
import com.postech.workshop_service.domain.valueobjects.Placa;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BuscarVeiculoPorIdUseCaseTest {

	@Mock
	private VeiculoRepository veiculoRepository;

	@InjectMocks
	private BuscarVeiculoPorIdUseCase buscarVeiculoPorIdUseCase;

	@Test
	void shouldReturnVeiculoWhenFound() {
		UUID veiculoId = UUID.randomUUID();
		Veiculo veiculo = new Veiculo(veiculoId, new Placa("BRA1D23"), "Toyota", "Corolla", 2020, null, null,
				List.of(UUID.randomUUID()));
		when(veiculoRepository.buscarPorId(veiculoId, false)).thenReturn(Optional.of(veiculo));

		assertTrue(buscarVeiculoPorIdUseCase.executar(veiculoId, false).isPresent());
	}

}

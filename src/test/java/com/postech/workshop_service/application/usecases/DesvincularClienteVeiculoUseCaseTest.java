package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.application.exceptions.RegraDeNegocioException;
import com.postech.workshop_service.application.exceptions.RecursoNaoEncontradoException;
import com.postech.workshop_service.domain.entities.Veiculo;
import com.postech.workshop_service.domain.repositories.VeiculoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DesvincularClienteVeiculoUseCaseTest {

	@Mock
	private VeiculoRepository veiculoRepository;

	@InjectMocks
	private DesvincularClienteVeiculoUseCase desvincularClienteVeiculoUseCase;

	@Test
	void shouldUnlinkClienteFromVeiculo() {
		UUID clienteA = UUID.randomUUID();
		UUID clienteB = UUID.randomUUID();
		UUID veiculoId = UUID.randomUUID();
		LocalDateTime agora = LocalDateTime.now();
		Veiculo veiculo = new Veiculo(veiculoId, "BRA1D23", "Toyota", "Corolla", 2020, null, null,
				List.of(clienteA, clienteB), true, agora, agora, null);

		when(veiculoRepository.buscarPorId(veiculoId, true)).thenReturn(Optional.of(veiculo));
		when(veiculoRepository.salvar(any(Veiculo.class))).thenAnswer(invocation -> invocation.getArgument(0));

		Veiculo atualizado = desvincularClienteVeiculoUseCase.executar(veiculoId, clienteB);

		assertEquals(List.of(clienteA), atualizado.getClientesVinculados().stream().toList());
	}

	@Test
	void shouldThrowWhenRemovingLastCliente() {
		UUID clienteId = UUID.randomUUID();
		UUID veiculoId = UUID.randomUUID();
		LocalDateTime agora = LocalDateTime.now();
		Veiculo veiculo = new Veiculo(veiculoId, "BRA1D23", "Toyota", "Corolla", 2020, null, null, List.of(clienteId),
				true, agora, agora, null);

		when(veiculoRepository.buscarPorId(veiculoId, true)).thenReturn(Optional.of(veiculo));

		assertThrows(RegraDeNegocioException.class,
				() -> desvincularClienteVeiculoUseCase.executar(veiculoId, clienteId));
	}

	@Test
	void shouldThrowWhenVeiculoDoesNotExist() {
		assertThrows(RecursoNaoEncontradoException.class,
				() -> desvincularClienteVeiculoUseCase.executar(UUID.randomUUID(), UUID.randomUUID()));
	}

}

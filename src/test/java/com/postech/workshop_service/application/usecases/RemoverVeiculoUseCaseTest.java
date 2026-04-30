package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.application.exceptions.RecursoNaoEncontradoException;
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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RemoverVeiculoUseCaseTest {

	@Mock
	private VeiculoRepository veiculoRepository;

	@InjectMocks
	private RemoverVeiculoUseCase removerVeiculoUseCase;

	@Test
	void shouldRemoveVeiculoLogically() {
		UUID id = UUID.randomUUID();
		Veiculo veiculo = new Veiculo(id, new Placa("BRA1D23"), "Toyota", "Corolla", 2020, null, null,
				List.of(UUID.randomUUID()));
		when(veiculoRepository.buscarPorId(id, true)).thenReturn(Optional.of(veiculo));
		when(veiculoRepository.salvar(any(Veiculo.class))).thenAnswer(invocation -> invocation.getArgument(0));

		removerVeiculoUseCase.executar(id);

		assertFalse(veiculo.isAtivo());
	}

	@Test
	void shouldThrowWhenVeiculoDoesNotExist() {
		assertThrows(RecursoNaoEncontradoException.class, () -> removerVeiculoUseCase.executar(UUID.randomUUID()));
	}

}

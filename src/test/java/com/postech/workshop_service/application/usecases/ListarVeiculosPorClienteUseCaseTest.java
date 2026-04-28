package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.application.exceptions.RecursoNaoEncontradoException;
import com.postech.workshop_service.domain.entities.Cliente;
import com.postech.workshop_service.domain.repositories.ClienteRepository;
import com.postech.workshop_service.domain.repositories.VeiculoRepository;
import com.postech.workshop_service.domain.valueobjects.Documento;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListarVeiculosPorClienteUseCaseTest {

	@Mock
	private VeiculoRepository veiculoRepository;

	@Mock
	private ClienteRepository clienteRepository;

	@InjectMocks
	private ListarVeiculosPorClienteUseCase listarVeiculosPorClienteUseCase;

	@Test
	void shouldThrowWhenClientDoesNotExist() {
		assertThrows(RecursoNaoEncontradoException.class,
				() -> listarVeiculosPorClienteUseCase.executar(UUID.randomUUID(), false));
	}

	@Test
	void shouldReturnVehiclesWhenClientExists() {
		UUID clienteId = UUID.randomUUID();
		when(clienteRepository.buscarPorId(clienteId, false)).thenReturn(
				Optional.of(new Cliente(clienteId, "Cliente", new Documento("98765432100"), "email@teste.com", null)));
		when(veiculoRepository.listarPorCliente(clienteId, false)).thenReturn(List.of());

		assertEquals(0, listarVeiculosPorClienteUseCase.executar(clienteId, false).size());
	}

}

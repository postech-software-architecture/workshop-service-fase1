package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.application.exceptions.RecursoNaoEncontradoException;
import com.postech.workshop_service.domain.entities.Cliente;
import com.postech.workshop_service.domain.repositories.ClienteRepository;
import com.postech.workshop_service.domain.repositories.PaginaResultado;
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
class ListarVeiculosUseCaseTest {

	@Mock
	private VeiculoRepository veiculoRepository;

	@Mock
	private ClienteRepository clienteRepository;

	@InjectMocks
	private ListarVeiculosUseCase listarVeiculosUseCase;

	@Test
	void shouldValidateClientFilterBeforeListing() {
		UUID clienteId = UUID.randomUUID();
		when(clienteRepository.buscarPorId(clienteId)).thenReturn(
				Optional.of(new Cliente(clienteId, "Cliente", new Documento("98765432100"), "email@teste.com", null)));
		when(veiculoRepository.listar(0, 20, null, clienteId, false))
			.thenReturn(new PaginaResultado<>(List.of(), 0, 0, 0, 20));

		PaginaResultado<?> resultado = listarVeiculosUseCase.executar(0, 20, null, clienteId, false);
		assertEquals(0, resultado.totalElementos());
	}

	@Test
	void shouldThrowWhenClientFilterDoesNotExist() {
		assertThrows(RecursoNaoEncontradoException.class,
				() -> listarVeiculosUseCase.executar(0, 20, null, UUID.randomUUID(), false));
	}

}

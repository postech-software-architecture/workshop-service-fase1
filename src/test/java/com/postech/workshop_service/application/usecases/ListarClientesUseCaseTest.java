package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.domain.entities.Cliente;
import com.postech.workshop_service.domain.repositories.ClienteRepository;
import com.postech.workshop_service.domain.valueobjects.Documento;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListarClientesUseCaseTest {

	@Mock
	private ClienteRepository clienteRepository;

	@InjectMocks
	private ListarClientesUseCase useCase;

	@Test
	void shouldList() {
		List<Cliente> list = List.of(new Cliente(null, "A", new Documento("98765432100"), "a@a.com", null));
		when(clienteRepository.listar(0, 10, false)).thenReturn(list);

		List<Cliente> result = useCase.executar(0, 10, false);

		assertEquals(1, result.size());
	}

	@Test
	void shouldCount() {
		when(clienteRepository.contarTodos()).thenReturn(5L);
		assertEquals(5L, useCase.contarTotal());
	}

}

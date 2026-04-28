package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.domain.entities.Cliente;
import com.postech.workshop_service.domain.repositories.ClienteRepository;
import com.postech.workshop_service.domain.valueobjects.Documento;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BuscarClientePorIdUseCaseTest {

	@Mock
	private ClienteRepository clienteRepository;

	@InjectMocks
	private BuscarClientePorIdUseCase useCase;

	@Test
	void shouldFindById() {
		UUID id = UUID.randomUUID();
		Cliente cliente = new Cliente(id, "Nome", new Documento("98765432100"), "e@e.com", null);
		when(clienteRepository.buscarPorId(id)).thenReturn(Optional.of(cliente));

		Optional<Cliente> result = useCase.executar(id);

		assertTrue(result.isPresent());
		assertEquals("Nome", result.get().getNome());
	}

}
